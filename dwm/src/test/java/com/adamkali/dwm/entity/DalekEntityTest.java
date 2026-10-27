package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

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
        assertEquals(1.7F, DWMEntityTypes.DALEK.getHeight(), 0.001F);
    }

    @Test
    void variantsAreDefined() {
        assertEquals(9, DalekVariant.values().length);
        assertEquals("1963", DalekVariant.CLASSIC_1963.getSerializedName());
        assertEquals("time_war_bronze", DalekVariant.BRONZE_TIME_WAR.getSerializedName());
        assertEquals("time_war_bronze_dark", DalekVariant.BRONZE_TIME_WAR_DARK.getSerializedName());
        for (DalekVariant variant : DalekVariant.values()) {
            assertEquals(variant, DalekVariant.byId(variant.getSerializedName(), variant.family()));
            assertEquals(variant, DalekVariant.byOrdinal(variant.ordinal(), variant.family()));
        }
        assertEquals(DalekVariant.CLASSIC_1963, DalekVariant.byId("unknown", DalekFamily.CLASSIC));
        assertEquals(DalekVariant.CLASSIC_1963, DalekFamily.CLASSIC.defaultVariant());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/dalek/time_war_bronze.png"),
                DalekVariant.BRONZE_TIME_WAR.textureLocation()
        );
    }

    @Test
    void variantsFromAnotherFamilyFallBackToTheDefault() {
        assertEquals(
                DalekVariant.CLASSIC_1963,
                DalekVariant.byId(DalekVariant.PARADIGM_SUPREME.getSerializedName(), DalekFamily.CLASSIC)
        );
        assertEquals(
                DalekVariant.CLASSIC_1963,
                DalekVariant.byOrdinal(DalekVariant.PARADIGM_DRONE.ordinal(), DalekFamily.CLASSIC)
        );
        assertEquals(DalekVariant.CLASSIC_1963, DalekVariant.byOrdinal(-1, DalekFamily.CLASSIC));
        assertEquals(
                DalekVariant.BRONZE_TIME_WAR,
                DalekVariant.fitting(DalekVariant.BRONZE_TIME_WAR, DalekFamily.CLASSIC)
        );
    }

    @Test
    void classicFamilySpansThe1963AndTimeWarChassis() {
        assertEquals(DalekChassis.CLASSIC_1963, DalekVariant.CLASSIC_1963.chassis());
        assertEquals(DalekChassis.TIME_WAR, DalekVariant.BRONZE_TIME_WAR.chassis());
        assertEquals(DalekChassis.TIME_WAR, DalekVariant.BRONZE_TIME_WAR_DARK.chassis());
        assertEquals(DalekFamily.CLASSIC, DalekChassis.CLASSIC_1963.family());
        assertEquals(DalekFamily.CLASSIC, DalekChassis.TIME_WAR.family());
        assertEquals(DalekFamily.PARADIGM, DalekChassis.PARADIGM.family());
    }

    @Test
    void pickCoversAllClassicVariants() {
        Map<DalekVariant, Integer> counts = new EnumMap<>(DalekVariant.class);
        RandomSource random = RandomSource.create(42L);
        for (int i = 0; i < 500; i++) {
            counts.merge(DalekVariant.pick(random, DalekFamily.CLASSIC), 1, Integer::sum);
        }
        assertEquals(3, counts.size());
        for (DalekVariant variant : counts.keySet()) {
            assertEquals(DalekFamily.CLASSIC, variant.family());
        }
        int classic = counts.get(DalekVariant.CLASSIC_1963);
        assertTrue(classic > counts.get(DalekVariant.BRONZE_TIME_WAR));
        assertTrue(classic > counts.get(DalekVariant.BRONZE_TIME_WAR_DARK));
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
