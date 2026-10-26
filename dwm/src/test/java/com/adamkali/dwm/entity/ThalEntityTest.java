package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThalEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.THAL == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.THAL_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsCreature() {
        assertNotNull(DWMEntityTypes.THAL);
        assertEquals(MobCategory.CREATURE, DWMEntityTypes.THAL.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "thal"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.THAL)
        );
    }

    @Test
    void attributesAreRegistered() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.THAL));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.THAL);
        assertEquals(20.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.3, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.THAL_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "thal_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.THAL_SPAWN_EGG)
        );
    }

    @Test
    void dimensionsMatchVillager() {
        assertEquals(0.6F, DWMEntityTypes.THAL.getWidth(), 0.001F);
        assertEquals(1.95F, DWMEntityTypes.THAL.getHeight(), 0.001F);
    }

    @Test
    void fourVariantsAreDefined() {
        assertEquals(4, ThalVariant.values().length);
        assertEquals("thal_1", ThalVariant.THAL_1.getSerializedName());
        assertEquals("thal_2", ThalVariant.THAL_2.getSerializedName());
        assertEquals("thal_3", ThalVariant.THAL_3.getSerializedName());
        assertEquals("thal_4", ThalVariant.THAL_4.getSerializedName());
        assertEquals(ThalVariant.THAL_1, ThalVariant.byId("thal_1"));
        assertEquals(ThalVariant.THAL_1, ThalVariant.byId("unknown"));
        assertEquals(ThalVariant.THAL_4, ThalVariant.byOrdinal(3));
        assertEquals(ThalVariant.THAL_1, ThalVariant.byOrdinal(-1));
        assertEquals(ThalVariant.THAL_1, ThalVariant.byOrdinal(4));
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/thal/thal_3.png"),
                ThalVariant.THAL_3.textureLocation()
        );
    }
}
