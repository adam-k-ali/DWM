package com.adamkali.dwm.model.armor;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Compact EVA suit. The slot meshes live in {@code models/entity/eva_suit_<slot>.json} (all parented on
 * {@code humanoid_armor_base}) and keep standard humanoid part names so player animation copies
 * cleanly; visor / backpack are children of the moving parent parts. The atlas texture of each slot
 * is the JSON {@code texture} (leggings use the {@code humanoid_leggings} sheet).
 */
public class EvaSuitModel extends HumanoidModel<HumanoidRenderState> {
    public static final ArmorModelSet<ModelLayerLocation> LAYER_SET = new ArmorModelSet<>(
            layer("eva_suit_helmet"),
            layer("eva_suit_chestplate"),
            layer("eva_suit_leggings"),
            layer("eva_suit_boots")
    );

    public EvaSuitModel(ModelPart root) {
        super(root);
    }

    private static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, id), "main");
    }
}
