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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekMutantEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.DALEK_MUTANT == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.DALEK_MUTANT_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsMonster() {
        assertNotNull(DWMEntityTypes.DALEK_MUTANT);
        assertEquals(MobCategory.MONSTER, DWMEntityTypes.DALEK_MUTANT.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalek_mutant"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.DALEK_MUTANT)
        );
    }

    @Test
    void attributesComeFromTheMutantDefinition() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.DALEK_MUTANT));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.DALEK_MUTANT);
        assertEquals(10.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(3.0, supplier.getValue(Attributes.ATTACK_DAMAGE), 0.001);
        assertEquals(0.2, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.DALEK_MUTANT_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalek_mutant_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.DALEK_MUTANT_SPAWN_EGG)
        );
    }

    @Test
    void mutantFitsInsideADalekCasing() {
        assertEquals(0.7F, DWMEntityTypes.DALEK_MUTANT.getWidth(), 0.001F);
        assertEquals(0.7F, DWMEntityTypes.DALEK_MUTANT.getHeight(), 0.001F);
        assertTrue(DWMEntityTypes.DALEK_MUTANT.getWidth() < DWMEntityTypes.DALEK.getWidth());
        assertTrue(DWMEntityTypes.DALEK_MUTANT.getHeight() < DWMEntityTypes.DALEK.getHeight());
    }

    @Test
    void emergenceRollUsesTheNamedChance() {
        assertEquals(0.25F, DalekMutantEmergence.EMERGE_CHANCE);
        assertTrue(DalekMutantEmergence.shouldEmerge(0.0F));
        assertTrue(DalekMutantEmergence.shouldEmerge(Math.nextDown(DalekMutantEmergence.EMERGE_CHANCE)));
        assertFalse(DalekMutantEmergence.shouldEmerge(DalekMutantEmergence.EMERGE_CHANCE));
        assertFalse(DalekMutantEmergence.shouldEmerge(Math.nextDown(1.0F)));
    }
}
