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

class K9EntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.K9 == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.K9_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsCreature() {
        assertNotNull(DWMEntityTypes.K9);
        assertEquals(MobCategory.CREATURE, DWMEntityTypes.K9.getCategory());
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "k9"), BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.K9));
    }

    @Test
    void attributesComeFromDefinition() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.K9));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.K9);
        assertEquals(30.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.35, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void itemsAreRegistered() {
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "k9_unit"), BuiltInRegistries.ITEM.getKey(DWMItems.K9_UNIT));
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "k9_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.K9_SPAWN_EGG)
        );
        assertEquals(1, DWMItems.K9_UNIT.getDefaultMaxStackSize());
    }

    @Test
    void hitboxIsLowAndBoxy() {
        assertEquals(0.6F, DWMEntityTypes.K9.getWidth(), 0.001F);
        assertEquals(0.7F, DWMEntityTypes.K9.getHeight(), 0.001F);
    }
}
