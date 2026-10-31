package com.adamkali.dwm.render;

import com.adamkali.dwm.model.json.EntityModelTextures;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisDecorEntityBlock;
import com.adamkali.dwm.block.entities.TardisDecorBlockEntity;
import com.adamkali.dwm.block.entities.TardisMonitorBlockEntity;
import com.adamkali.dwm.model.tileentity.MonitorScreenRect;
import com.adamkali.dwm.model.tileentity.TardisCompactScannerModel;
import com.adamkali.dwm.model.tileentity.TardisFullScannerModel;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import com.adamkali.dwm.block.entities.TardisGlobeSpin;
import com.adamkali.dwm.render.state.TardisDecorBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisGlobeRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * BER for globe and TARDIS monitors (scanners). Mesh choice is driven by the placed block id; the
 * globe animates (ambient turn plus the {@link TardisGlobeSpin} started on use) and glows, while
 * monitors also draw their synced status screen (glow quad, text, bars) at full brightness.
 */
public class TardisDecorBlockEntityRenderer
        implements BlockEntityRenderer<TardisDecorBlockEntity, TardisDecorBlockEntityRenderState> {
    private final TardisGlobeModel globeModel;
    private final TardisCompactScannerModel compactScannerModel;
    private final TardisFullScannerModel fullScannerModel;
    private final Identifier globeTexture;
    private final RenderType globeGlow = RenderTypes.eyes(TardisGlobeModel.GLOW_TEXTURE);

    /** One Minecraft day; the ambient animation hitches imperceptibly once per wrap. */
    public static final long GLOBE_AGE_PERIOD = 24000L;

    private final Font font;

    /** Block units per font pixel on monitor screens (signs use 1/96). */
    static final float TEXT_SCALE = 1.0F / 112.0F;
    static final float SCREEN_PADDING = 3.0F;
    static final float ROW_HEIGHT = 10.0F;
    static final float MIN_BAR_WIDTH = 18.0F;
    static final float BAR_HEIGHT = 5.0F;
    static final int GLOW_LINKED = 0x3830C8C0;
    static final int GLOW_UNLINKED = 0x30FF5040;
    static final int BAR_TRACK = 0x8040504E;
    static final int PAGE_INDICATOR_COLOR = 0xFF6F8F8C;
    /** Glow, bar and text offsets in front of the glass (pixels) so they never z-fight. */
    private static final float GLOW_OFFSET = 0.05F;
    private static final float TEXT_OFFSET = 0.1F;

    public TardisDecorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.globeModel = new TardisGlobeModel(context.bakeLayer(TardisGlobeModel.LAYER_LOCATION));
        this.compactScannerModel = new TardisCompactScannerModel(context.bakeLayer(TardisCompactScannerModel.LAYER_LOCATION));
        this.fullScannerModel = new TardisFullScannerModel(context.bakeLayer(TardisFullScannerModel.LAYER_LOCATION));
        this.globeTexture = EntityModelTextures.get(TardisGlobeModel.LAYER_LOCATION);
    }

    @Override
    public TardisDecorBlockEntityRenderState createRenderState() {
        return new TardisDecorBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
            TardisDecorBlockEntity entity,
            TardisDecorBlockEntityRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValueOrElse(TardisDecorEntityBlock.FACING, Direction.NORTH);
        state.block = entity.getBlockState().getBlock();
        Level level = entity.getLevel();
        if (state.block == DWMBlocks.TARDIS_GLOBE && level != null) {
            long gameTime = level.getGameTime();
            TardisGlobeSpin spin = entity.spin();
            state.globeAge = globeAge(gameTime, entity.getBlockPos(), partialTicks);
            state.globeSpin = spin.degrees(gameTime, partialTicks);
            state.globeEnergy = spin.energy(gameTime, partialTicks);
        }
        if (entity instanceof TardisMonitorBlockEntity monitor) {
            state.monitor = true;
            state.page = monitor.page();
            state.readout = monitor.readout();
        } else {
            state.monitor = false;
        }
    }

    /**
     * Ambient globe clock: game time offset by a per-position phase (so neighbouring globes don't turn
     * in lockstep), wrapped to {@link #GLOBE_AGE_PERIOD} to keep float precision. Pure helper for tests.
     */
    public static float globeAge(long gameTime, BlockPos pos, float partialTicks) {
        long phase = Math.floorMod(Mth.getSeed(pos), GLOBE_AGE_PERIOD);
        return Math.floorMod(gameTime + phase, GLOBE_AGE_PERIOD) + partialTicks;
    }

    @Override
    public void submit(
            TardisDecorBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        Block block = state.block;
        if (block == DWMBlocks.TARDIS_GLOBE) {
            submitGlobe(state, poseStack, submitNodeCollector);
            return;
        }
        EntityModel<TardisRenderState> model;
        Identifier texture;
        if (block == DWMBlocks.TARDIS_COMPACT_SCANNER) {
            model = compactScannerModel;
            texture = TardisCompactScannerModel.TEXTURE_LOCATION;
        } else if (block == DWMBlocks.TARDIS_FULL_SCANNER) {
            model = fullScannerModel;
            texture = TardisFullScannerModel.TEXTURE_LOCATION;
        } else {
            return;
        }

        TardisRenderState animState = new TardisRenderState();
        poseStack.pushPose();
        applyTransforms(poseStack, state.facing);
        submitNodeCollector.submitModel(
                model,
                animState,
                poseStack,
                RenderTypes.entityCutout(texture),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        if (state.monitor) {
            submitMonitorScreens(state, poseStack, submitNodeCollector);
        }
        poseStack.popPose();
    }

    private void submitGlobe(TardisDecorBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        // Submission is deferred, so each globe needs its own animation state.
        TardisGlobeRenderState globeState = new TardisGlobeRenderState();
        globeState.ageInTicks = state.globeAge;
        globeState.spinDegrees = state.globeSpin;
        globeState.spinEnergy = state.globeEnergy;
        poseStack.pushPose();
        applyTransforms(poseStack, state.facing);
        submitNodeCollector.submitModel(
                globeModel,
                globeState,
                poseStack,
                RenderTypes.entityCutout(globeTexture),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress);
        submitNodeCollector.order(1).submitModel(
                globeModel,
                globeState,
                poseStack,
                globeGlow,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                null);
        poseStack.popPose();
    }

    private void submitMonitorScreens(
            TardisDecorBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector
    ) {
        TardisMonitorReadout readout = state.readout;
        Component title = TardisMonitorScreenLayout.title(state.page, readout);
        int titleColor = TardisMonitorScreenLayout.titleColor(readout);
        List<TardisMonitorScreenLayout.Line> lines = TardisMonitorScreenLayout.lines(state.page, readout);
        Component pageIndicator = readout.linked()
                ? Component.literal((state.page.ordinal() + 1) + "/" + MonitorPage.values().length)
                : null;
        if (state.block == DWMBlocks.TARDIS_FULL_SCANNER) {
            Identifier texture = TardisFullScannerModel.TEXTURE_LOCATION;
            float u = TardisFullScannerModel.GLOW_U;
            float v = TardisFullScannerModel.GLOW_V;
            submitScreen(poseStack, collector, TardisFullScannerModel.SCREEN, texture, u, v, readout.linked(),
                    title, titleColor, pageIndicator, lines);
            submitScreen(poseStack, collector, TardisFullScannerModel.LEFT_WING, texture, u, v, readout.linked(),
                    null, 0, null, TardisMonitorScreenLayout.leftWing(readout));
            submitScreen(poseStack, collector, TardisFullScannerModel.RIGHT_WING, texture, u, v, readout.linked(),
                    null, 0, null, TardisMonitorScreenLayout.rightWing(readout));
        } else {
            submitScreen(poseStack, collector, TardisCompactScannerModel.SCREEN,
                    TardisCompactScannerModel.TEXTURE_LOCATION,
                    TardisCompactScannerModel.GLOW_U, TardisCompactScannerModel.GLOW_V, readout.linked(),
                    title, titleColor, pageIndicator, lines);
        }
    }

    /**
     * Draws one screen: an emissive glow over the glass, then rows of text (title row optional)
     * with bars, all at full brightness. Text space is font pixels, origin at the viewer's top-left
     * of the glass, Y down.
     */
    private void submitScreen(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            MonitorScreenRect rect,
            Identifier texture,
            float glowU,
            float glowV,
            boolean linked,
            @Nullable Component title,
            int titleColor,
            @Nullable Component pageIndicator,
            List<TardisMonitorScreenLayout.Line> lines
    ) {
        int glow = linked ? GLOW_LINKED : GLOW_UNLINKED;
        collector.order(1).submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(texture),
                (pose, consumer) -> emitGlowQuad(pose.pose(), consumer, rect, glowU, glowV, glow));

        poseStack.pushPose();
        poseStack.translate(rect.maxX() / 16.0F, rect.maxY() / 16.0F, (rect.frontZ() - TEXT_OFFSET) / 16.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        float width = textSpace(rect.width());
        float height = textSpace(rect.height());
        float y = SCREEN_PADDING;
        if (title != null) {
            float indicatorWidth = pageIndicator == null ? 0.0F : font.width(pageIndicator) + 2.0F;
            submitLine(poseStack, collector, title, titleColor, SCREEN_PADDING, y,
                    width - 2.0F * SCREEN_PADDING - indicatorWidth);
            if (pageIndicator != null) {
                submitLine(poseStack, collector, pageIndicator, PAGE_INDICATOR_COLOR,
                        width - SCREEN_PADDING - font.width(pageIndicator), y, indicatorWidth);
            }
            y += ROW_HEIGHT + 1.0F;
        }
        float innerWidth = width - 2.0F * SCREEN_PADDING;
        for (TardisMonitorScreenLayout.Line line : lines) {
            if (y + ROW_HEIGHT > height + 1.0F) {
                break;
            }
            float labelWidth = font.width(line.text());
            if (!line.hasBar()) {
                submitLine(poseStack, collector, line.text(), line.color(), SCREEN_PADDING, y, innerWidth);
                y += ROW_HEIGHT;
                continue;
            }
            float barX = labelWidth > 0.0F ? SCREEN_PADDING + labelWidth + 4.0F : SCREEN_PADDING;
            if (labelWidth > 0.0F && SCREEN_PADDING + innerWidth - barX < MIN_BAR_WIDTH) {
                // Label too long to share the row: label on its own row, bar underneath.
                submitLine(poseStack, collector, line.text(), line.color(), SCREEN_PADDING, y, innerWidth);
                y += ROW_HEIGHT;
                barX = SCREEN_PADDING;
                if (y + ROW_HEIGHT > height + 1.0F) {
                    break;
                }
            } else if (labelWidth > 0.0F) {
                submitLine(poseStack, collector, line.text(), line.color(), SCREEN_PADDING, y, innerWidth);
            }
            submitBar(poseStack, collector, texture, glowU, glowV,
                    barX, y + 1.0F, SCREEN_PADDING + innerWidth, line.bar(), line.barColor());
            y += ROW_HEIGHT;
        }
        poseStack.popPose();
    }

    static float textSpace(float pixels) {
        return pixels / 16.0F / TEXT_SCALE;
    }

    /** Draws one row of text, squeezing it horizontally when wider than {@code maxWidth}. */
    private void submitLine(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Component text,
            int color,
            float x,
            float y,
            float maxWidth
    ) {
        FormattedCharSequence sequence = text.getVisualOrderText();
        float squeeze = fitScale(font.width(sequence), maxWidth);
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0F);
        poseStack.scale(squeeze, 1.0F, 1.0F);
        collector.submitText(poseStack, 0.0F, 0.0F, sequence, false, Font.DisplayMode.POLYGON_OFFSET,
                LightCoordsUtil.FULL_BRIGHT, color, 0, 0);
        poseStack.popPose();
    }

    /** Horizontal squeeze (0–1] so a {@code width}-wide line fits in {@code maxWidth}. */
    static float fitScale(float width, float maxWidth) {
        if (width <= 0.0F || maxWidth <= 0.0F || width <= maxWidth) {
            return 1.0F;
        }
        return maxWidth / width;
    }

    private static void submitBar(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Identifier texture,
            float u,
            float v,
            float x0,
            float y0,
            float x1,
            float fraction,
            int color
    ) {
        float y1 = y0 + BAR_HEIGHT;
        float fill = x0 + (x1 - x0) * fraction;
        collector.order(2).submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(texture),
                (pose, consumer) -> {
                    emitTextSpaceQuad(pose.pose(), consumer, x0, y0, x1, y1, u, v, BAR_TRACK);
                    if (fill > x0) {
                        emitTextSpaceQuad(pose.pose(), consumer, x0, y0, fill, y1, u, v, color);
                    }
                });
    }

    /** Quad facing the viewer in block/model space (screen faces -Z; viewer's right is -X). */
    private static void emitGlowQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            MonitorScreenRect rect,
            float u,
            float v,
            int color
    ) {
        float minX = rect.minX() / 16.0F;
        float maxX = rect.maxX() / 16.0F;
        float minY = rect.minY() / 16.0F;
        float maxY = rect.maxY() / 16.0F;
        float z = (rect.frontZ() - GLOW_OFFSET) / 16.0F;
        // Counter-clockwise as seen from -Z.
        vertex(matrix, consumer, maxX, minY, z, u, v, color);
        vertex(matrix, consumer, minX, minY, z, u, v, color);
        vertex(matrix, consumer, minX, maxY, z, u, v, color);
        vertex(matrix, consumer, maxX, maxY, z, u, v, color);
    }

    /** Quad in text space (Y down), wound like font glyphs so it faces the same way as text. */
    private static void emitTextSpaceQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            float x0,
            float y0,
            float x1,
            float y1,
            float u,
            float v,
            int color
    ) {
        vertex(matrix, consumer, x0, y0, 0.0F, u, v, color);
        vertex(matrix, consumer, x0, y1, 0.0F, u, v, color);
        vertex(matrix, consumer, x1, y1, 0.0F, u, v, color);
        vertex(matrix, consumer, x1, y0, 0.0F, u, v, color);
    }

    private static void vertex(
            Matrix4f matrix,
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, -1.0F);
    }

    /**
     * Floor-standing Y-up Blockbench export: center on block, yaw by facing.
     * South-facing matches blockstate south = 0° for JSON decor props.
     * EntityModel parts are already in pixel units (÷16 by the renderer).
     */
    public static void applyTransforms(PoseStack matrices, Direction facing) {
        matrices.translate(0.5, 0.0, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(-Direction.getYRot(facing)));
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
