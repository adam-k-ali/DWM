package com.adamkali.dwm.render;

import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Item special renderer for the TARDIS exterior block — reuses the First Doctor police-box
 * EntityModel so GUI / ground / hand show the 3D shell instead of a flat item sprite.
 */
public class TardisBlockSpecialRenderer implements NoDataSpecialModelRenderer {
    /** Visual police-box footprint: 1×2×1 blocks (collision is a unit cube; mesh is taller). */
    private static final float EXTENT_MIN_X = 0.0f;
    private static final float EXTENT_MIN_Y = 0.0f;
    private static final float EXTENT_MIN_Z = 0.0f;
    private static final float EXTENT_MAX_X = 1.0f;
    private static final float EXTENT_MAX_Y = 2.0f;
    private static final float EXTENT_MAX_Z = 1.0f;

    private final FirstDoctorTardisModel model;
    private final TardisRenderState animState = new TardisRenderState();

    public TardisBlockSpecialRenderer(FirstDoctorTardisModel model) {
        this.model = model;
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
    ) {
        poseStack.pushPose();
        // BER exterior pose for rotation segment 0 (doors closed via empty anim state).
        TardisBlockEntityRenderer.applyExteriorTransforms(poseStack, 0.0f);
        submitNodeCollector.order(0).submitModel(
                this.model,
                this.animState,
                poseStack,
                FirstDoctorTardisModel.TEXTURE_LOCATION,
                lightCoords,
                overlayCoords,
                outlineColor,
                null);
        if (hasFoil) {
            submitNodeCollector.order(1).submitModel(
                    this.model,
                    this.animState,
                    poseStack,
                    RenderTypes.entityGlint(),
                    lightCoords,
                    overlayCoords,
                    outlineColor,
                    null);
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        emitExtents(output);
    }

    /**
     * Emits the eight corners of the visual police-box AABB (0…1 × 0…2 × 0…1) so
     * {@code base} display can use vanilla block-like rotations. Pure helper for unit tests.
     */
    public static void emitExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(EXTENT_MIN_X, EXTENT_MIN_Y, EXTENT_MIN_Z));
        output.accept(new Vector3f(EXTENT_MIN_X, EXTENT_MIN_Y, EXTENT_MAX_Z));
        output.accept(new Vector3f(EXTENT_MIN_X, EXTENT_MAX_Y, EXTENT_MIN_Z));
        output.accept(new Vector3f(EXTENT_MIN_X, EXTENT_MAX_Y, EXTENT_MAX_Z));
        output.accept(new Vector3f(EXTENT_MAX_X, EXTENT_MIN_Y, EXTENT_MIN_Z));
        output.accept(new Vector3f(EXTENT_MAX_X, EXTENT_MIN_Y, EXTENT_MAX_Z));
        output.accept(new Vector3f(EXTENT_MAX_X, EXTENT_MAX_Y, EXTENT_MIN_Z));
        output.accept(new Vector3f(EXTENT_MAX_X, EXTENT_MAX_Y, EXTENT_MAX_Z));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<TardisBlockSpecialRenderer.Unbaked> MAP_CODEC =
                MapCodec.unit(new TardisBlockSpecialRenderer.Unbaked());

        @Override
        public MapCodec<TardisBlockSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public TardisBlockSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new TardisBlockSpecialRenderer(
                    new FirstDoctorTardisModel(
                            context.entityModelSet().bakeLayer(FirstDoctorTardisModel.LAYER_LOCATION)));
        }
    }
}
