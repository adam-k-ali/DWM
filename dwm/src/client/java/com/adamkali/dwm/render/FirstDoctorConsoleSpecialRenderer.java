package com.adamkali.dwm.render;

import com.adamkali.dwm.model.tileentity.FirstDoctorConsoleModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Item special renderer for the First Doctor console — reuses the BER EntityModel mesh
 * so GUI / ground / hand show the 3D pedestal instead of a flat wall-sprite placeholder.
 */
public class FirstDoctorConsoleSpecialRenderer implements NoDataSpecialModelRenderer {
    /**
     * Matches {@code FirstDoctorConsoleBlock.COLLISION_SHAPE} (~1.6×1.25×1.6 pedestal).
     * Hardcoded so unit tests do not initialize the Block class.
     */
    private static final float EXTENT_MIN_X = -0.3f;
    private static final float EXTENT_MIN_Y = 0.0f;
    private static final float EXTENT_MIN_Z = -0.3f;
    private static final float EXTENT_MAX_X = 1.3f;
    private static final float EXTENT_MAX_Y = 1.25f;
    private static final float EXTENT_MAX_Z = 1.3f;

    private final FirstDoctorConsoleModel model;
    private final TardisRenderState animState = new TardisRenderState();

    public FirstDoctorConsoleSpecialRenderer(FirstDoctorConsoleModel model) {
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
        // Match BER placement for a north-facing console, then 180° Y so the hex
        // presents to vanilla GUI [30, 225, 0] like the compact scanner.
        FirstDoctorConsoleBlockEntityRenderer.applyTransforms(poseStack, Direction.NORTH);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        submitNodeCollector.order(0).submitModel(
                this.model,
                this.animState,
                poseStack,
                FirstDoctorConsoleModel.TEXTURE_LOCATION,
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
     * Emits the eight corners of the console pedestal AABB in JSON corner-origin item
     * space so {@code base} display can use vanilla block-like rotations. Must match
     * {@link #submit} after the BER +0.5 XZ translate.
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
        public static final MapCodec<FirstDoctorConsoleSpecialRenderer.Unbaked> MAP_CODEC =
                MapCodec.unit(new FirstDoctorConsoleSpecialRenderer.Unbaked());

        @Override
        public MapCodec<FirstDoctorConsoleSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public FirstDoctorConsoleSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new FirstDoctorConsoleSpecialRenderer(
                    new FirstDoctorConsoleModel(
                            context.entityModelSet().bakeLayer(FirstDoctorConsoleModel.LAYER_LOCATION)));
        }
    }
}
