package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public final class DWMArmorMaterials {
    public static final int AZBANTIUM_BASE_DURABILITY = 33;
    public static final int EVA_SUIT_BASE_DURABILITY = 5;

    public static final ResourceKey<EquipmentAsset> AZBANTIUM_EQUIPMENT = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "azbantium")
    );

    public static final ResourceKey<EquipmentAsset> EVA_SUIT_EQUIPMENT = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "eva_suit")
    );

    /** First-person visor overlay; vanilla Hud prefixes this as {@code textures/<path>.png}. */
    public static final Identifier EVA_SUIT_CAMERA_OVERLAY = Identifier.fromNamespaceAndPath(
            DWMReference.MOD_ID,
            "misc/eva_suit_overlay"
    );

    public static final ArmorMaterial AZBANTIUM = new ArmorMaterial(
            AZBANTIUM_BASE_DURABILITY,
            ArmorMaterials.makeDefense(3, 6, 8, 3, 11),
            10,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0F,
            0.0F,
            DWMItemTags.REPAIRS_AZBANTIUM_EQUIPMENT,
            AZBANTIUM_EQUIPMENT
    );

    /** Leather-tier defense; radiation mitigation is handled by {@code RadiationExposureLogic}. */
    public static final ArmorMaterial EVA_SUIT = new ArmorMaterial(
            EVA_SUIT_BASE_DURABILITY,
            ArmorMaterials.makeDefense(1, 2, 3, 1, 3),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            DWMItemTags.REPAIRS_EVA_SUIT,
            EVA_SUIT_EQUIPMENT
    );

    /** Helmet Equippable with first-person visor overlay; other pieces use {@code humanoidArmor} defaults. */
    public static Equippable evaSuitHelmet() {
        return Equippable.builder(ArmorType.HELMET.getSlot())
                .setEquipSound(EVA_SUIT.equipSound())
                .setAsset(EVA_SUIT.assetId())
                .setCameraOverlay(EVA_SUIT_CAMERA_OVERLAY)
                .build();
    }

    private DWMArmorMaterials() {
    }
}
