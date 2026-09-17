package com.adamkali.dwm.item;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaSuitOverlayTest {
    @BeforeAll
    static void boot() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void cameraOverlayIdMapsToMiscTexture() {
        assertEquals(DWMReference.MOD_ID, DWMArmorMaterials.EVA_SUIT_CAMERA_OVERLAY.getNamespace());
        assertEquals("misc/eva_suit_overlay", DWMArmorMaterials.EVA_SUIT_CAMERA_OVERLAY.getPath());
    }

    @Test
    void helmetEquippableKeepsArmorWiringAndCameraOverlay() {
        Equippable equippable = DWMArmorMaterials.evaSuitHelmet();
        assertEquals(EquipmentSlot.HEAD, equippable.slot());
        assertEquals(DWMArmorMaterials.EVA_SUIT.equipSound(), equippable.equipSound());
        assertEquals(Optional.of(DWMArmorMaterials.EVA_SUIT.assetId()), equippable.assetId());
        assertEquals(
                Optional.of(DWMArmorMaterials.EVA_SUIT_CAMERA_OVERLAY),
                equippable.cameraOverlay()
        );
    }
}
