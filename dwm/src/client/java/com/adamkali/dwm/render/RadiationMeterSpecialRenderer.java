package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.item.DWMDataComponents;
import com.adamkali.dwm.model.item.RadiationMeterModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Renders the meter body and its live, full-bright percentage display.
 */
public class RadiationMeterSpecialRenderer
        implements SpecialModelRenderer<RadiationMeterSpecialRenderer.Display> {
    static final int UNKNOWN = -1;
    static final int SAFE_COLOR = 0xFF55FF77;
    static final int CAUTION_COLOR = 0xFFFFFF55;
    static final int WARNING_COLOR = 0xFFFFAA33;
    static final int DANGER_COLOR = 0xFFFF4444;
    static final int UNKNOWN_COLOR = 0xFF718078;

    private static final Identifier DIGITS_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    DWMReference.MOD_ID,
                    "textures/entity/radiation_meter_digits.png");
    private static final float ATLAS_WIDTH = 64.0F;
    private static final float GLYPH_U_WIDTH = 4.0F / ATLAS_WIDTH;
    private static final float GLYPH_WIDTH = 0.10F;
    private static final float GLYPH_HEIGHT = 0.20F;
    private static final float GLYPH_GAP = 0.008F;
    private static final float SCREEN_CENTER_X = 0.0F;
    private static final float SCREEN_BOTTOM_Y = 0.62F;
    private static final float SCREEN_Z = -0.175F;

    private final RadiationMeterModel model;
    private final TardisRenderState modelState = new TardisRenderState();

    public RadiationMeterSpecialRenderer(RadiationMeterModel model) {
        this.model = model;
    }

    @Override
    public @Nullable Display extractArgument(@NonNull ItemStack stack) {
        return new Display(stack.getOrDefault(DWMDataComponents.RADIATION_LEVEL, UNKNOWN));
    }

    @Override
    public void submit(
            @Nullable Display display,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.rotateDegrees(Axis.YP, 180.0F);

        if (hasFoil) {
            submitNodeCollector.order(0).submitModel(
                    this.model,
                    this.modelState,
                    poseStack,
                    RenderTypes.entitySolidGlint(RadiationMeterModel.TEXTURE_LOCATION),
                    lightCoords,
                    overlayCoords,
                    outlineColor);
        } else {
            submitNodeCollector.order(0).submitModel(
                    this.model,
                    this.modelState,
                    poseStack,
                    RadiationMeterModel.TEXTURE_LOCATION,
                    lightCoords,
                    overlayCoords,
                    outlineColor);
        }

        Display resolved = display == null ? new Display(UNKNOWN) : display;
        submitDisplay(poseStack, submitNodeCollector, screenText(resolved.percent()), colorForPercent(resolved.percent()));
        poseStack.popPose();
    }

    private static void submitDisplay(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            String text,
            int color
    ) {
        float totalWidth = text.length() * GLYPH_WIDTH + (text.length() - 1) * GLYPH_GAP;
        // The item pose includes a 180° Y rotation, which mirrors X. Place glyphs
        // right-to-left in model space so they read left-to-right on the screen.
        float x = SCREEN_CENTER_X + totalWidth / 2.0F - GLYPH_WIDTH;
        for (int i = 0; i < text.length(); i++) {
            int glyph = glyphIndex(text.charAt(i));
            submitGlyph(poseStack, submitNodeCollector, x, glyph, color);
            x -= GLYPH_WIDTH + GLYPH_GAP;
        }
    }

    private static void submitGlyph(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            float x,
            int glyph,
            int color
    ) {
        float u0 = glyph * GLYPH_U_WIDTH;
        float u1 = u0 + GLYPH_U_WIDTH;
        float x1 = x + GLYPH_WIDTH;
        float y1 = SCREEN_BOTTOM_Y + GLYPH_HEIGHT;
        submitNodeCollector.order(1).submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucentEmissive(DIGITS_TEXTURE),
                (pose, consumer) -> emitGlyphQuad(
                        pose.pose(), consumer, x, SCREEN_BOTTOM_Y, x1, y1, SCREEN_Z, u0, u1, color));
    }

    private static void emitGlyphQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            float x0,
            float y0,
            float x1,
            float y1,
            float z,
            float u0,
            float u1,
            int color
    ) {
        // Counter-clockwise from -Z; U is flipped because the item pose mirrors X.
        float[] xs = glyphQuadXs(x0, x1);
        float[] ys = {y0, y0, y1, y1};
        float[] us = glyphQuadUs(u0, u1);
        float[] vs = {1.0F, 1.0F, 0.0F, 0.0F};
        for (int i = 0; i < 4; i++) {
            consumer.addVertex(matrix, xs[i], ys[i], z)
                    .setColor(color)
                    .setUv(us[i], vs[i])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightCoordsUtil.FULL_BRIGHT)
                    .setNormal(0.0F, 0.0F, -1.0F);
        }
    }

    static String screenText(int percent) {
        return percent < 0 ? "--%" : Math.max(0, Math.min(100, percent)) + "%";
    }

    static int colorForPercent(int percent) {
        if (percent < 0) {
            return UNKNOWN_COLOR;
        }
        if (percent < 25) {
            return SAFE_COLOR;
        }
        if (percent < 50) {
            return CAUTION_COLOR;
        }
        if (percent < 75) {
            return WARNING_COLOR;
        }
        return DANGER_COLOR;
    }

    static int glyphIndex(char glyph) {
        if (glyph >= '0' && glyph <= '9') {
            return glyph - '0';
        }
        return glyph == '-' ? 10 : 11;
    }

    static float[] glyphQuadXs(float x0, float x1) {
        return new float[] {x0, x1, x1, x0};
    }

    static float[] glyphQuadUs(float u0, float u1) {
        return new float[] {u1, u0, u0, u1};
    }

    static float firstGlyphX(int length) {
        float totalWidth = length * GLYPH_WIDTH + (length - 1) * GLYPH_GAP;
        return SCREEN_CENTER_X + totalWidth / 2.0F - GLYPH_WIDTH;
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> output) {
        emitExtents(output);
    }

    static void emitExtents(Consumer<Vector3fc> output) {
        float minX = 0.125F;
        float minY = 0.0F;
        float minZ = 0.3125F;
        float maxX = 0.875F;
        float maxY = 1.0625F;
        float maxZ = 0.6875F;
        output.accept(new Vector3f(minX, minY, minZ));
        output.accept(new Vector3f(minX, minY, maxZ));
        output.accept(new Vector3f(minX, maxY, minZ));
        output.accept(new Vector3f(minX, maxY, maxZ));
        output.accept(new Vector3f(maxX, minY, minZ));
        output.accept(new Vector3f(maxX, minY, maxZ));
        output.accept(new Vector3f(maxX, maxY, minZ));
        output.accept(new Vector3f(maxX, maxY, maxZ));
    }

    public record Display(int percent) {
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Display> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @NonNull MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public RadiationMeterSpecialRenderer bake(@NonNull BakingContext context) {
            return new RadiationMeterSpecialRenderer(
                    new RadiationMeterModel(
                            context.entityModelSet().bakeLayer(RadiationMeterModel.LAYER_LOCATION)));
        }
    }
}
