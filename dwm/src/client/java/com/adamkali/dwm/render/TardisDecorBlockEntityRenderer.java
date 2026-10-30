package com.adamkali.dwm.render;

import com.adamkali.dwm.model.json.EntityModelTextures;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisDecorEntityBlock;
import com.adamkali.dwm.block.entities.TardisDecorBlockEntity;
import com.adamkali.dwm.model.tileentity.TardisCompactScannerModel;
import com.adamkali.dwm.model.tileentity.TardisFullScannerModel;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import com.adamkali.dwm.block.entities.TardisGlobeSpin;
import com.adamkali.dwm.render.state.TardisDecorBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisGlobeRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/**
 * BER for globe and scanners. Mesh choice is driven by the placed block id; scanners are static,
 * the globe animates (ambient turn plus the {@link TardisGlobeSpin} started on use) and glows.
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

    public TardisDecorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
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
