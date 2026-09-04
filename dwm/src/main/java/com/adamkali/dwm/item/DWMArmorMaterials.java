package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class DWMArmorMaterials {
    public static final int AZBANTIUM_BASE_DURABILITY = 33;
    public static final int PROTECTIVE_SUIT_BASE_DURABILITY = 5;

    public static final ResourceKey<EquipmentAsset> AZBANTIUM_EQUIPMENT = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "azbantium")
    );

    public static final ResourceKey<EquipmentAsset> PROTECTIVE_SUIT_EQUIPMENT = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "protective_suit")
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
    public static final ArmorMaterial PROTECTIVE_SUIT = new ArmorMaterial(
            PROTECTIVE_SUIT_BASE_DURABILITY,
            ArmorMaterials.makeDefense(1, 2, 3, 1, 3),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            DWMItemTags.REPAIRS_PROTECTIVE_SUIT,
            PROTECTIVE_SUIT_EQUIPMENT
    );

    private DWMArmorMaterials() {
    }
}
