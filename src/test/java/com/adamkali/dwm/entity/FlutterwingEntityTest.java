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

class FlutterwingEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.FLUTTERWING == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.FLUTTERWING_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsCreature() {
        assertNotNull(DWMEntityTypes.FLUTTERWING);
        assertEquals(MobCategory.CREATURE, DWMEntityTypes.FLUTTERWING.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "flutterwing"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.FLUTTERWING)
        );
    }

    @Test
    void attributesAreRegistered() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.FLUTTERWING));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.FLUTTERWING);
        assertEquals(10.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.6, supplier.getValue(Attributes.FLYING_SPEED), 0.001);
        assertEquals(0.3, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.FLUTTERWING_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "flutterwing_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.FLUTTERWING_SPAWN_EGG)
        );
    }

    @Test
    void dimensionsAreScaledDownByEightyPercent() {
        assertEquals(0.9F * FlutterwingEntity.SCALE, DWMEntityTypes.FLUTTERWING.getWidth(), 0.001F);
        assertEquals(1.5F * FlutterwingEntity.SCALE, DWMEntityTypes.FLUTTERWING.getHeight(), 0.001F);
        assertEquals(0.2F, FlutterwingEntity.SCALE, 0.001F);
    }

    @Test
    void fourSpeciesVariantsAreDefined() {
        assertEquals(4, FlutterwingVariant.values().length);
        assertEquals("blue_crystal", FlutterwingVariant.BLUE_CRYSTAL.getSerializedName());
        assertEquals("madrigal", FlutterwingVariant.MADRIGAL.getSerializedName());
        assertEquals("silverband", FlutterwingVariant.SILVERBAND.getSerializedName());
        assertEquals("wild_endeavour", FlutterwingVariant.WILD_ENDEAVOUR.getSerializedName());
        assertEquals(FlutterwingVariant.BLUE_CRYSTAL, FlutterwingVariant.byId("blue_crystal"));
        assertEquals(FlutterwingVariant.BLUE_CRYSTAL, FlutterwingVariant.byId("unknown"));
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/flutterwing/madrigal.png"),
                FlutterwingVariant.MADRIGAL.textureLocation()
        );
    }
}
