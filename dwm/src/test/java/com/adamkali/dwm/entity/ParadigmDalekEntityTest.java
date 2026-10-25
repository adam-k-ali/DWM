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

class ParadigmDalekEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.PARADIGM_DALEK == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.PARADIGM_DALEK_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsMonster() {
        assertNotNull(DWMEntityTypes.PARADIGM_DALEK);
        assertEquals(MobCategory.MONSTER, DWMEntityTypes.PARADIGM_DALEK.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "paradigm_dalek"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.PARADIGM_DALEK)
        );
    }

    @Test
    void attributesComeFromTheParadigmDefinition() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.PARADIGM_DALEK));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.PARADIGM_DALEK);
        assertEquals(50.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(8.0, supplier.getValue(Attributes.ARMOR), 0.001);
        assertEquals(6.0, supplier.getValue(Attributes.ATTACK_DAMAGE), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.PARADIGM_DALEK_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "paradigm_dalek_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.PARADIGM_DALEK_SPAWN_EGG)
        );
    }

    @Test
    void casingIsLargerThanTheClassicDalek() {
        assertEquals(1.0F, DWMEntityTypes.PARADIGM_DALEK.getWidth(), 0.001F);
        assertEquals(2.4F, DWMEntityTypes.PARADIGM_DALEK.getHeight(), 0.001F);
        assertTrue(DWMEntityTypes.PARADIGM_DALEK.getWidth() > DWMEntityTypes.DALEK.getWidth());
        assertTrue(DWMEntityTypes.PARADIGM_DALEK.getHeight() > DWMEntityTypes.DALEK.getHeight());
        assertTrue(DalekChassis.PARADIGM.gunHeight() > DalekChassis.CLASSIC_1963.gunHeight());
        assertTrue(DalekChassis.PARADIGM.gunHeight() > DalekChassis.TIME_WAR.gunHeight());
    }

    @Test
    void castesUseTheParadigmChassis() {
        assertEquals(DalekVariant.PARADIGM_DRONE, DalekFamily.PARADIGM.defaultVariant());
        assertEquals(
                DalekVariant.PARADIGM_DRONE,
                DalekVariant.byId(DalekVariant.CLASSIC_1963.getSerializedName(), DalekFamily.PARADIGM)
        );
        assertEquals(DalekVariant.PARADIGM_SUPREME, DalekVariant.byId("paradigm_supreme", DalekFamily.PARADIGM));
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/dalek/paradigm_eternal.png"),
                DalekVariant.PARADIGM_ETERNAL.textureLocation()
        );
    }

    @Test
    void pickCoversAllCastesWithDronesCommonAndSupremeRare() {
        Map<DalekVariant, Integer> counts = new EnumMap<>(DalekVariant.class);
        RandomSource random = RandomSource.create(42L);
        for (int i = 0; i < 2000; i++) {
            counts.merge(DalekVariant.pick(random, DalekFamily.PARADIGM), 1, Integer::sum);
        }
        assertEquals(5, counts.size());
        for (DalekVariant variant : counts.keySet()) {
            assertEquals(DalekChassis.PARADIGM, variant.chassis());
        }
        int drones = counts.get(DalekVariant.PARADIGM_DRONE);
        int supreme = counts.get(DalekVariant.PARADIGM_SUPREME);
        for (Map.Entry<DalekVariant, Integer> entry : counts.entrySet()) {
            if (entry.getKey() != DalekVariant.PARADIGM_DRONE) {
                assertTrue(drones > entry.getValue(), "Drone should outnumber " + entry.getKey());
            }
            if (entry.getKey() != DalekVariant.PARADIGM_SUPREME) {
                assertTrue(supreme < entry.getValue(), "Supreme should be rarer than " + entry.getKey());
            }
        }
    }
}
