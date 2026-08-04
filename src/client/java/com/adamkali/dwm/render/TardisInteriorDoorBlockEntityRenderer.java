package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.render.soto.TardisSotoRenderer;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

public class TardisInteriorDoorBlockEntityRenderer implements BlockEntityRenderer<TardisInteriorDoorBlockEntity> {
    /**
     * Entity cutout that skips the default {@code LEQUAL} depth phase.
     * {@link RenderPhase#ALWAYS_DEPTH_TEST} is a no-op begin/end for func 519, so the caller must
     * set {@code GL_ALWAYS} before {@link VertexConsumerProvider.Immediate#draw()} — otherwise
     * vanilla cutout re-applies LEQUAL and SOTO wins the depth test.
     */
    private static final Function<Identifier, RenderLayer> ENTITY_CUTOUT_ALWAYS_DEPTH = Util.memoize(
            texture -> {
                RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_CUTOUT_PROGRAM)
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                        .transparency(RenderPhase.NO_TRANSPARENCY)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .build(true);
                return RenderLayer.of(
                        "dwm_soto_door_overlay",
                        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                        VertexFormat.DrawMode.QUADS,
                        1536,
                        true,
                        false,
                        params);
            });

    private final TardisClassicInteriorDoorModel model;
    private final TardisSotoRenderer sotoRenderer;

    public TardisInteriorDoorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new TardisClassicInteriorDoorModel(
                context.getLayerModelPart(TardisClassicInteriorDoorModel.LAYER_LOCATION));
        this.sotoRenderer = new TardisSotoRenderer();
    }

    @Override
    public void render(
            TardisInteriorDoorBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        BlockState state = entity.getCachedState();
        Direction facing = state.get(TardisInteriorDoorBlock.FACING, Direction.NORTH);

        matrices.push();
        applyTransforms(matrices, facing);

        TardisRenderState renderState = new TardisRenderState();
        renderState.setDoorSwingProgress(entity.getDoorSwing());
        model.setAngles(renderState);

        VertexConsumer vertices = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutout(TardisClassicInteriorDoorModel.TEXTURE_LOCATION));

        if (TardisSotoRenderer.shouldRender(entity.getDoorSwing())) {
            // Shell → SOTO → doors so frames establish depth before the aperture clear,
            // and swung leaves still composite over the exterior preview.
            model.renderShell(matrices, vertices, light, overlay);
            flush(vertexConsumers);

            sotoRenderer.render(
                    matrices,
                    vertexConsumers,
                    tickDelta,
                    entity.getTardisId(),
                    entity.getPos(),
                    facing
            );

            // ALWAYS_DEPTH_TEST phase does not set GL_ALWAYS (no-op for 519); set it here and use
            // a layer that will not re-apply LEQUAL during Immediate.draw().
            vertices = vertexConsumers.getBuffer(
                    ENTITY_CUTOUT_ALWAYS_DEPTH.apply(TardisClassicInteriorDoorModel.TEXTURE_LOCATION));
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            try {
                // Full mesh: frames/jambs/leaves all composite over sealed SOTO pixels.
                model.render(matrices, vertices, light, overlay);
                flush(vertexConsumers);
            } finally {
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
            }
        } else {
            model.render(matrices, vertices, light, overlay);
        }
        matrices.pop();
    }

    @Override
    public int getRenderDistance() {
        // Exterior preview extends several blocks beyond the door aperture.
        return 128;
    }

    /**
     * Standard Blockbench tile-entity placement: pivot at top of door volume, X-180 flip,
     * then yaw for {@code facing}. Model pixel units render as 1/16 block.
     */
    static void applyTransforms(MatrixStack matrices, Direction facing) {
        matrices.translate(0.5, TardisInteriorDoorShapes.MODEL_HEIGHT_BLOCKS, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-Direction.getHorizontalDegreesOrThrow(facing)));
        // Center the ~3-block-wide mesh on the 3-wide bank (origin is bank start cell).
        matrices.translate(TardisInteriorDoorShapes.BANK_CENTER_OFFSET_BLOCKS, 0.0, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        matrices.translate(-TardisInteriorDoorShapes.MODEL_CENTER_X_PX / 16.0F, 0.0F, 0.0F);
    }

    private static void flush(VertexConsumerProvider vertexConsumers) {
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }
}
