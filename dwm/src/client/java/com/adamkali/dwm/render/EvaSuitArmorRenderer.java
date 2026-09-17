package com.adamkali.dwm.render;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.model.armor.EvaSuitModel;
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
 * Renders the custom EVA-suit meshes in place of vanilla armour
 * layers, copying the parent humanoid pose so walk / crouch / attack animate.
 */
public final class EvaSuitArmorRenderer implements ArmorRenderer {
    private final ArmorModelSet<EvaSuitModel> models;

    public EvaSuitArmorRenderer(EntityRendererProvider.Context context) {
        this.models = ArmorModelSet.bake(
                EvaSuitModel.LAYER_SET,
                context.getModelSet(),
                EvaSuitModel::new
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
        EvaSuitModel model = this.models.get(slot);
        Identifier texture = slot == EquipmentSlot.LEGS
                ? EvaSuitModel.LEGGINGS_TEXTURE
                : EvaSuitModel.OUTER_TEXTURE;

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
                EvaSuitArmorRenderer::new,
                DWMItems.EVA_SUIT_HELMET,
                DWMItems.EVA_SUIT_CHESTPLATE,
                DWMItems.EVA_SUIT_LEGGINGS,
                DWMItems.EVA_SUIT_BOOTS
        );
    }
}
