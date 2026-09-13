package com.adamkali.dwm.render;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.model.armor.ProtectiveSuitModel;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Renders the custom EVA protective-suit meshes in place of vanilla armour
 * layers, copying the parent humanoid pose so walk / crouch / attack animate.
 */
public final class ProtectiveSuitArmorRenderer implements ArmorRenderer {
    private final ArmorModelSet<ProtectiveSuitModel> models;

    public ProtectiveSuitArmorRenderer(EntityRendererProvider.Context context) {
        this.models = ArmorModelSet.bake(
                ProtectiveSuitModel.LAYER_SET,
                context.getModelSet(),
                ProtectiveSuitModel::new
        );
    }

    @Override
    public void render(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            ItemStack stack,
            HumanoidRenderState humanoidRenderState,
            EquipmentSlot slot,
            int light,
            HumanoidModel<HumanoidRenderState> contextModel
    ) {
        ProtectiveSuitModel model = this.models.get(slot);
        Identifier texture = slot == EquipmentSlot.LEGS
                ? ProtectiveSuitModel.LEGGINGS_TEXTURE
                : ProtectiveSuitModel.OUTER_TEXTURE;

        ArmorRenderer.submitTransformCopyingModel(
                contextModel,
                humanoidRenderState,
                model,
                humanoidRenderState,
                false,
                submitNodeCollector.order(0),
                poseStack,
                RenderTypes.armorCutoutNoCull(texture),
                light,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                humanoidRenderState.outlineColor,
                null
        );

        if (stack.hasFoil()) {
            ArmorRenderer.submitTransformCopyingModel(
                    contextModel,
                    humanoidRenderState,
                    model,
                    humanoidRenderState,
                    false,
                    submitNodeCollector.order(1),
                    poseStack,
                    RenderTypes.armorEntityGlint(),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    null,
                    humanoidRenderState.outlineColor,
                    null
            );
        }
    }

    @Override
    public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
        return false;
    }

    public static void register() {
        ArmorRenderer.register(
                ProtectiveSuitArmorRenderer::new,
                DWMItems.PROTECTIVE_SUIT_HELMET,
                DWMItems.PROTECTIVE_SUIT_CHESTPLATE,
                DWMItems.PROTECTIVE_SUIT_LEGGINGS,
                DWMItems.PROTECTIVE_SUIT_BOOTS
        );
    }
}
