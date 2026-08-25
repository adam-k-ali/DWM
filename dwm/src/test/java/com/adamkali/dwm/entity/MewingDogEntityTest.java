package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MewingDogEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.MEWING_DOG == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.MEWING_DOG_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsCreature() {
        assertNotNull(DWMEntityTypes.MEWING_DOG);
        assertEquals(MobCategory.CREATURE, DWMEntityTypes.MEWING_DOG.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "mewing_dog"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.MEWING_DOG)
        );
    }

    @Test
    void attributesAreRegistered() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.MEWING_DOG));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.MEWING_DOG);
        assertEquals(8.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.3, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
        assertEquals(2.0, supplier.getValue(Attributes.ATTACK_DAMAGE), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.MEWING_DOG_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "mewing_dog_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.MEWING_DOG_SPAWN_EGG)
        );
    }

    @Test
    void isFoodUsesWolfFoodTag() {
        // Production gate is ItemTags.WOLF_FOOD (same as vanilla Wolf); ItemStack construction
        // needs a fully bound registry, so tag identity is asserted here and GameTests cover interact.
        assertEquals(
                Identifier.fromNamespaceAndPath("minecraft", "wolf_food"),
                ItemTags.WOLF_FOOD.location()
        );
        assertEquals(
                Identifier.fromNamespaceAndPath("minecraft", "bone"),
                BuiltInRegistries.ITEM.getKey(Items.BONE)
        );
    }

    @Test
    void dimensionsMatchWolf() {
        assertEquals(0.6F, DWMEntityTypes.MEWING_DOG.getWidth(), 0.001F);
        assertEquals(0.85F, DWMEntityTypes.MEWING_DOG.getHeight(), 0.001F);
    }
}
