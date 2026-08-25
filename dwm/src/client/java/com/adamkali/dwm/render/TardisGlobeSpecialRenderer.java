package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisDecorShapes;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Item special renderer for the TARDIS globe — reuses the BER EntityModel mesh
 * so GUI / ground / hand show the 3D prop instead of the flat (missing) block atlas sprite.
 */
public class TardisGlobeSpecialRenderer implements NoDataSpecialModelRenderer {
    private final TardisGlobeModel model;
    private final TardisRenderState animState = new TardisRenderState();

    public TardisGlobeSpecialRenderer(TardisGlobeModel model) {
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
        // JSON item space: corner origin with XZ centered via +0.5; 180° Y so the
        // ring face (thin Z profile) presents to vanilla GUI [30, 225, 0].
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        submitNodeCollector.order(0).submitModel(
                this.model,
                this.animState,
                poseStack,
                TardisGlobeModel.TEXTURE_LOCATION,
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
     * Emits the eight corners of {@link TardisDecorShapes#GLOBE} in JSON
     * corner-origin item space so {@code base} display can use vanilla block-like
     * rotations. Must match {@link #submit} after the +0.5 XZ translate.
     * Pure helper for unit tests.
     */
    public static void emitExtents(Consumer<Vector3fc> output) {
        AABB box = TardisDecorShapes.GLOBE.bounds();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        output.accept(new Vector3f(minX, minY, minZ));
        output.accept(new Vector3f(minX, minY, maxZ));
        output.accept(new Vector3f(minX, maxY, minZ));
        output.accept(new Vector3f(minX, maxY, maxZ));
        output.accept(new Vector3f(maxX, minY, minZ));
        output.accept(new Vector3f(maxX, minY, maxZ));
        output.accept(new Vector3f(maxX, maxY, minZ));
        output.accept(new Vector3f(maxX, maxY, maxZ));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<TardisGlobeSpecialRenderer.Unbaked> MAP_CODEC =
                MapCodec.unit(new TardisGlobeSpecialRenderer.Unbaked());

        @Override
        public MapCodec<TardisGlobeSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public TardisGlobeSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new TardisGlobeSpecialRenderer(
                    new TardisGlobeModel(context.entityModelSet().bakeLayer(TardisGlobeModel.LAYER_LOCATION)));
        }
    }
}
