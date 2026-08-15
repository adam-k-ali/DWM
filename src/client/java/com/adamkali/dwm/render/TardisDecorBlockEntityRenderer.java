package com.adamkali.dwm.render;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisDecorEntityBlock;
import com.adamkali.dwm.block.entities.TardisDecorBlockEntity;
import com.adamkali.dwm.model.tileentity.TardisCompactScannerModel;
import com.adamkali.dwm.model.tileentity.TardisFullScannerModel;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import com.adamkali.dwm.render.state.TardisDecorBlockEntityRenderState;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/**
 * Static BER for globe and scanners. Mesh choice is driven by the placed block id.
 */
public class TardisDecorBlockEntityRenderer
        implements BlockEntityRenderer<TardisDecorBlockEntity, TardisDecorBlockEntityRenderState> {
    private final TardisGlobeModel globeModel;
    private final TardisCompactScannerModel compactScannerModel;
    private final TardisFullScannerModel fullScannerModel;

    public TardisDecorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.globeModel = new TardisGlobeModel(context.bakeLayer(TardisGlobeModel.LAYER_LOCATION));
        this.compactScannerModel = new TardisCompactScannerModel(context.bakeLayer(TardisCompactScannerModel.LAYER_LOCATION));
        this.fullScannerModel = new TardisFullScannerModel(context.bakeLayer(TardisFullScannerModel.LAYER_LOCATION));
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
    }

    @Override
    public void submit(
            TardisDecorBlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        Block block = state.block;
        EntityModel<TardisRenderState> model;
        Identifier texture;
        if (block == DWMBlocks.TARDIS_COMPACT_SCANNER) {
            model = compactScannerModel;
            texture = TardisCompactScannerModel.TEXTURE_LOCATION;
        } else if (block == DWMBlocks.TARDIS_FULL_SCANNER) {
            model = fullScannerModel;
            texture = TardisFullScannerModel.TEXTURE_LOCATION;
        } else {
            model = globeModel;
            texture = TardisGlobeModel.TEXTURE_LOCATION;
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
