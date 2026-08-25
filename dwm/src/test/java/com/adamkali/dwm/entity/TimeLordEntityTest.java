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

class TimeLordEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.TIME_LORD == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.TIME_LORD_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsCreature() {
        assertNotNull(DWMEntityTypes.TIME_LORD);
        assertEquals(MobCategory.CREATURE, DWMEntityTypes.TIME_LORD.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "time_lord"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.TIME_LORD)
        );
    }

    @Test
    void attributesAreRegistered() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.TIME_LORD));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.TIME_LORD);
        assertEquals(20.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.5, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.TIME_LORD_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "time_lord_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.TIME_LORD_SPAWN_EGG)
        );
    }

    @Test
    void dimensionsMatchVillager() {
        assertEquals(0.6F, DWMEntityTypes.TIME_LORD.getWidth(), 0.001F);
        assertEquals(1.95F, DWMEntityTypes.TIME_LORD.getHeight(), 0.001F);
    }

    @Test
    void fourVariantsAreDefined() {
        assertEquals(4, TimeLordVariant.values().length);
        assertEquals("time_lord_1", TimeLordVariant.TIME_LORD_1.getSerializedName());
        assertEquals("time_lord_2", TimeLordVariant.TIME_LORD_2.getSerializedName());
        assertEquals("time_lord_3", TimeLordVariant.TIME_LORD_3.getSerializedName());
        assertEquals("time_lord_4", TimeLordVariant.TIME_LORD_4.getSerializedName());
        assertEquals(TimeLordVariant.TIME_LORD_1, TimeLordVariant.byId("time_lord_1"));
        assertEquals(TimeLordVariant.TIME_LORD_1, TimeLordVariant.byId("unknown"));
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/time_lord_3.png"),
                TimeLordVariant.TIME_LORD_3.textureLocation()
        );
    }
}
