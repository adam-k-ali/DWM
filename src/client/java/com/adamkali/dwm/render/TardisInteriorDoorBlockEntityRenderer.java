package com.adamkali.dwm.render;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.render.soto.TardisSotoRenderer;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.tardis.interior.TardisInteriorDoorShapes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

public class TardisInteriorDoorBlockEntityRenderer implements BlockEntityRenderer<TardisInteriorDoorBlockEntity> {
    /**
     * Entity cutout that skips the default {@code LEQUAL} depth phase.
     * {@link RenderStateShard#NO_DEPTH_TEST} is a no-op begin/end for func 519, so the caller must
     * set {@code GL_ALWAYS} before {@link MultiBufferSource.BufferSource#endBatch()} — otherwise
     * vanilla cutout re-applies LEQUAL and SOTO wins the depth test.
     */
    private static final Function<Identifier, RenderType> ENTITY_CUTOUT_ALWAYS_DEPTH = Util.memoize(
            texture -> {
                RenderType.CompositeState params = RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .createCompositeState(true);
                return RenderType.create(
                        "dwm_soto_door_overlay",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        1536,
                        true,
                        false,
                        params);
            });

    private final TardisClassicInteriorDoorModel model;
    private final TardisSotoRenderer sotoRenderer;

    public TardisInteriorDoorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TardisClassicInteriorDoorModel(
                context.bakeLayer(TardisClassicInteriorDoorModel.LAYER_LOCATION));
        this.sotoRenderer = new TardisSotoRenderer();
    }

    @Override
    public void render(
            TardisInteriorDoorBlockEntity entity,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay
    ) {
        BlockState state = entity.getBlockState();
        Direction facing = state.getValueOrElse(TardisInteriorDoorBlock.FACING, Direction.NORTH);

        matrices.pushPose();
        applyTransforms(matrices, facing);

        TardisRenderState renderState = new TardisRenderState();
        renderState.setDoorSwingProgress(entity.getDoorSwing());
        model.setupAnim(renderState);

        VertexConsumer vertices = vertexConsumers.getBuffer(
                RenderType.entityCutout(TardisClassicInteriorDoorModel.TEXTURE_LOCATION));

        if (TardisSotoRenderer.shouldRender(entity.getDoorSwing())) {
            // Shell → SOTO → doors so frames establish depth before the aperture clear,
            // and swung leaves still composite over the exterior preview.
            model.renderShell(matrices, vertices, light, overlay);
            flush(vertexConsumers);

            sotoRenderer.render(matrices,
                    vertexConsumers,
                    tickDelta,
                    entity.getTardisId(),
                    entity.getBlockPos(),
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
                model.renderToBuffer(matrices, vertices, light, overlay);
                flush(vertexConsumers);
            } finally {
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
            }
        } else {
            model.renderToBuffer(matrices, vertices, light, overlay);
        }
        matrices.popPose();
    }

    @Override
    public int getViewDistance() {
        // Exterior preview extends several blocks beyond the door aperture.
        return 128;
    }

    /**
     * Standard Blockbench tile-entity placement: pivot at top of door volume, X-180 flip,
     * then yaw for {@code facing}. Model pixel units render as 1/16 block.
     */
    static void applyTransforms(PoseStack matrices, Direction facing) {
        matrices.translate(0.5, TardisInteriorDoorShapes.MODEL_HEIGHT_BLOCKS, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees(-Direction.getYRot(facing)));
        // Center the ~3-block-wide mesh on the 3-wide bank (origin is bank start cell).
        matrices.translate(TardisInteriorDoorShapes.BANK_CENTER_OFFSET_BLOCKS, 0.0, 0.0);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0f));
        matrices.translate(-TardisInteriorDoorShapes.MODEL_CENTER_X_PX / 16.0F, 0.0F, 0.0F);
    }

    private static void flush(MultiBufferSource vertexConsumers) {
        if (vertexConsumers instanceof MultiBufferSource.BufferSource immediate) {
            immediate.endBatch();
        }
    }
}
