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

class DalekEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.DALEK == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.DALEK_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsMonster() {
        assertNotNull(DWMEntityTypes.DALEK);
        assertEquals(MobCategory.MONSTER, DWMEntityTypes.DALEK.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalek"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.DALEK)
        );
    }

    @Test
    void attributesAreRegistered() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.DALEK));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.DALEK);
        assertEquals(30.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(0.23, supplier.getValue(Attributes.MOVEMENT_SPEED), 0.001);
        assertEquals(0.4, supplier.getValue(Attributes.FLYING_SPEED), 0.001);
        assertEquals(24.0, supplier.getValue(Attributes.FOLLOW_RANGE), 0.001);
        assertEquals(4.0, supplier.getValue(Attributes.ATTACK_DAMAGE), 0.001);
        assertEquals(0.9, supplier.getValue(Attributes.KNOCKBACK_RESISTANCE), 0.001);
        assertEquals(6.0, supplier.getValue(Attributes.ARMOR), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.DALEK_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalek_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.DALEK_SPAWN_EGG)
        );
    }

    @Test
    void dimensionsMatchChassis() {
        assertEquals(0.8F, DWMEntityTypes.DALEK.getWidth(), 0.001F);
        assertEquals(1.95F, DWMEntityTypes.DALEK.getHeight(), 0.001F);
    }

    @Test
    void singleVariantIsDefined() {
        assertEquals(1, DalekVariant.values().length);
        assertEquals("1963", DalekVariant.CLASSIC_1963.getSerializedName());
        assertEquals(DalekVariant.CLASSIC_1963, DalekVariant.byId("1963"));
        assertEquals(DalekVariant.CLASSIC_1963, DalekVariant.byId("unknown"));
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/dalek/1963.png"),
                DalekVariant.CLASSIC_1963.textureLocation()
        );
    }

    @Test
    void laserTypeIsRegistered() {
        assertNotNull(DWMEntityTypes.DALEK_LASER);
        assertEquals(MobCategory.MISC, DWMEntityTypes.DALEK_LASER.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "dalek_laser"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.DALEK_LASER)
        );
    }
}
