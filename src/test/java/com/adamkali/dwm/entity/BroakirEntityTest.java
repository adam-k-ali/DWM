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

class BroakirEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.BROAKIR == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.BROAKIR_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsCreature() {
        assertNotNull(DWMEntityTypes.BROAKIR);
        assertEquals(MobCategory.CREATURE, DWMEntityTypes.BROAKIR.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "broakir"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.BROAKIR)
        );
    }

    @Test
    void attributesAreRegistered() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.BROAKIR));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.BROAKIR);
        assertEquals(15.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.25, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.BROAKIR_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "broakir_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.BROAKIR_SPAWN_EGG)
        );
    }
}
