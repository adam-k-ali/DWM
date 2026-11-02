package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CybermanEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.CYBUS_CYBERMAN == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.CYBUS_CYBERMAN_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    private static Map<String, EntityType<? extends CybermanEntity>> types() {
        return Map.of(
                "cybermondan", DWMEntityTypes.CYBERMONDAN,
                "cybus_cyberman", DWMEntityTypes.CYBUS_CYBERMAN,
                "cybermaster", DWMEntityTypes.CYBERMASTER,
                "weapons_grade_cyberman", DWMEntityTypes.WEAPONS_GRADE_CYBERMAN
        );
    }

    @Test
    void everyTypeIsAPlayerSizedMonster() {
        types().forEach((id, type) -> {
            assertNotNull(type, id);
            assertEquals(MobCategory.MONSTER, type.getCategory(), id);
            assertEquals(Identifier.fromNamespaceAndPath("dwm", id), BuiltInRegistries.ENTITY_TYPE.getKey(type));
            assertEquals(0.6F, type.getWidth(), 0.001F, id);
            assertEquals(1.95F, type.getHeight(), 0.001F, id);
        });
    }

    @Test
    void attributesComeFromTheDefinitions() {
        assertMaxHealthAndSpeed(DWMEntityTypes.CYBERMONDAN, 24.0, 0.2);
        assertMaxHealthAndSpeed(DWMEntityTypes.CYBUS_CYBERMAN, 30.0, 0.23);
        assertMaxHealthAndSpeed(DWMEntityTypes.CYBERMASTER, 30.0, 0.25);
        assertMaxHealthAndSpeed(DWMEntityTypes.WEAPONS_GRADE_CYBERMAN, 32.0, 0.28);
    }

    private static void assertMaxHealthAndSpeed(EntityType<? extends CybermanEntity> type, double health, double speed) {
        assertTrue(DefaultAttributes.hasSupplier(type));
        var supplier = DefaultAttributes.getSupplier(type);
        assertEquals(health, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(speed, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void everyTypeHasASpawnEgg() {
        Map<String, Item> eggs = Map.of(
                "cybermondan_spawn_egg", DWMItems.CYBERMONDAN_SPAWN_EGG,
                "cybus_cyberman_spawn_egg", DWMItems.CYBUS_CYBERMAN_SPAWN_EGG,
                "cybermaster_spawn_egg", DWMItems.CYBERMASTER_SPAWN_EGG,
                "weapons_grade_cyberman_spawn_egg", DWMItems.WEAPONS_GRADE_CYBERMAN_SPAWN_EGG
        );
        eggs.forEach((id, item) -> {
            assertNotNull(item, id);
            assertEquals(Identifier.fromNamespaceAndPath("dwm", id), BuiltInRegistries.ITEM.getKey(item));
        });
    }

    @Test
    void cybusGripSlowsBriefly() {
        var slowness = CybusElectricGrip.slowness();
        assertEquals(40, slowness.getDuration());
        assertEquals(1, slowness.getAmplifier());
    }
}
