package com.adamkali.dwm.world.radiation;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.world.DWMBiomeKeys;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RadiationExposureLogicTest {
    @BeforeAll
    static void boot() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void biomeTable_matchesDocumentedConstants() {
        assertEquals(0.15F, RadiationExposureLogic.ambientForBiome(DWMBiomeKeys.SKARO_THAL_PLATEAU));
        assertEquals(0.40F, RadiationExposureLogic.ambientForBiome(DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE));
        assertEquals(0.55F, RadiationExposureLogic.ambientForBiome(DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS));
        assertEquals(0.80F, RadiationExposureLogic.ambientForBiome(DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE));
        assertEquals(0.95F, RadiationExposureLogic.ambientForBiome(DWMBiomeKeys.SKARO_IRRADIATED_WASTES));
        assertEquals(0.0F, RadiationExposureLogic.ambientForBiome(null));
        assertEquals(0.0F, RadiationExposureLogic.ambientForBiome(DWMBiomeKeys.GALLIFREY_PLAINS));
    }

    @Test
    void mitigation_isLinearAndFullSetBlocksAmbient() {
        float ambient = RadiationExposureLogic.IRRADIATED_WASTES;
        assertEquals(ambient, RadiationExposureLogic.effectiveExposure(ambient, 0), 1.0e-4F);
        assertEquals(ambient - 0.25F, RadiationExposureLogic.effectiveExposure(ambient, 1), 1.0e-4F);
        assertEquals(ambient - 0.50F, RadiationExposureLogic.effectiveExposure(ambient, 2), 1.0e-4F);
        assertEquals(ambient - 0.75F, RadiationExposureLogic.effectiveExposure(ambient, 3), 1.0e-4F);
        assertEquals(0.0F, RadiationExposureLogic.effectiveExposure(ambient, 4), 1.0e-4F);
        assertEquals(0.0F, RadiationExposureLogic.effectiveExposure(RadiationExposureLogic.THAL_PLATEAU, 1), 1.0e-4F);
        assertFalse(RadiationExposureLogic.shouldApplyDamage(0.0F));
        assertTrue(RadiationExposureLogic.shouldApplyDamage(0.01F));
    }

    @Test
    void damageAmount_scalesWithEffectiveExposure() {
        assertEquals(0.0F, RadiationExposureLogic.damageAmount(0.0F));
        assertEquals(0.95F, RadiationExposureLogic.damageAmount(0.95F), 1.0e-4F);
        assertEquals(0.20F, RadiationExposureLogic.damageAmount(0.20F), 1.0e-4F);
    }

    @Test
    void meterPercent_roundsAmbientAndClamps() {
        assertEquals(15, RadiationExposureLogic.meterPercent(0.15F));
        assertEquals(95, RadiationExposureLogic.meterPercent(0.95F));
        assertEquals(0, RadiationExposureLogic.meterPercent(-1.0F));
        assertEquals(100, RadiationExposureLogic.meterPercent(1.5F));
        assertEquals(40, RadiationExposureLogic.meterPercent(0.404F));
    }

    @Test
    void suitPieceCount_requiresCorrectSlotItems() {
        assertEquals(0, RadiationExposureLogic.countSuitPieces(
                (Item) null, null, null, null
        ));
        assertEquals(1, RadiationExposureLogic.countSuitPieces(
                DWMItems.PROTECTIVE_SUIT_HELMET,
                null,
                null,
                null
        ));
        assertEquals(4, RadiationExposureLogic.countSuitPieces(
                DWMItems.PROTECTIVE_SUIT_HELMET,
                DWMItems.PROTECTIVE_SUIT_CHESTPLATE,
                DWMItems.PROTECTIVE_SUIT_LEGGINGS,
                DWMItems.PROTECTIVE_SUIT_BOOTS
        ));
        assertEquals(0, RadiationExposureLogic.countSuitPieces(
                Items.LEATHER_HELMET,
                Items.LEATHER_CHESTPLATE,
                Items.LEATHER_LEGGINGS,
                Items.LEATHER_BOOTS
        ));
        assertEquals(0, RadiationExposureLogic.countSuitPieces(
                DWMItems.PROTECTIVE_SUIT_BOOTS,
                DWMItems.PROTECTIVE_SUIT_HELMET,
                DWMItems.PROTECTIVE_SUIT_CHESTPLATE,
                DWMItems.PROTECTIVE_SUIT_LEGGINGS
        ));
    }

    @Test
    void namedConstants_areStable() {
        assertEquals(40, RadiationExposureLogic.TICK_INTERVAL);
        assertEquals(0.25F, RadiationExposureLogic.MITIGATION_PER_PIECE);
        assertEquals(1.0F, RadiationExposureLogic.DAMAGE_SCALE);
    }
}
