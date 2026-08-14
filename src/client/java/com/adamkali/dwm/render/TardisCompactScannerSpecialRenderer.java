package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisDecorShapes;
import com.adamkali.dwm.model.tileentity.TardisCompactScannerModel;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
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
 * Item special renderer for the Compact TARDIS scanner — reuses the BER EntityModel mesh
 * so GUI / ground / hand show the 3D prop instead of the flat entity atlas sprite.
 */
public class TardisCompactScannerSpecialRenderer implements NoDataSpecialModelRenderer {
    private final TardisCompactScannerModel model;
    private final TardisRenderState animState = new TardisRenderState();

    public TardisCompactScannerSpecialRenderer(TardisCompactScannerModel model) {
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
        submitNodeCollector.order(0).submitModel(
                this.model,
                this.animState,
                poseStack,
                TardisCompactScannerModel.TEXTURE_LOCATION,
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
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        emitExtents(output);
    }

    /**
     * Emits the eight corners of {@link TardisDecorShapes#COMPACT_SCANNER} shifted into
     * model/item space (origin at block center on X/Z). Pure helper for unit tests.
     */
    public static void emitExtents(Consumer<Vector3fc> output) {
        AABB box = TardisDecorShapes.COMPACT_SCANNER.bounds();
        float minX = (float) (box.minX - 0.5);
        float minY = (float) box.minY;
        float minZ = (float) (box.minZ - 0.5);
        float maxX = (float) (box.maxX - 0.5);
        float maxY = (float) box.maxY;
        float maxZ = (float) (box.maxZ - 0.5);
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
        public static final MapCodec<TardisCompactScannerSpecialRenderer.Unbaked> MAP_CODEC =
                MapCodec.unit(new TardisCompactScannerSpecialRenderer.Unbaked());

        @Override
        public MapCodec<TardisCompactScannerSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public TardisCompactScannerSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new TardisCompactScannerSpecialRenderer(
                    new TardisCompactScannerModel(
                            context.entityModelSet().bakeLayer(TardisCompactScannerModel.LAYER_LOCATION)));
        }
    }
}
