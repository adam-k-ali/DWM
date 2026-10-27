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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialWeaponsDalekEntityTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        if (DWMEntityTypes.SPECIAL_WEAPONS_DALEK == null) {
            DWMEntityTypes.initialize();
        }
        if (DWMItems.SPECIAL_WEAPONS_DALEK_SPAWN_EGG == null) {
            DWMItems.initialize();
        }
    }

    @Test
    void entityTypeIsRegisteredAsMonster() {
        assertNotNull(DWMEntityTypes.SPECIAL_WEAPONS_DALEK);
        assertEquals(MobCategory.MONSTER, DWMEntityTypes.SPECIAL_WEAPONS_DALEK.getCategory());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "special_weapons_dalek"),
                BuiltInRegistries.ENTITY_TYPE.getKey(DWMEntityTypes.SPECIAL_WEAPONS_DALEK)
        );
    }

    @Test
    void attributesComeFromTheSpecialWeaponsDefinition() {
        assertTrue(DefaultAttributes.hasSupplier(DWMEntityTypes.SPECIAL_WEAPONS_DALEK));
        var supplier = DefaultAttributes.getSupplier(DWMEntityTypes.SPECIAL_WEAPONS_DALEK);
        assertEquals(60.0, supplier.getValue(Attributes.MAX_HEALTH), 0.001);
        assertEquals(10.0, supplier.getValue(Attributes.ARMOR), 0.001);
        assertEquals(9.0, supplier.getValue(Attributes.ATTACK_DAMAGE), 0.001);
    }

    @Test
    void spawnEggIsRegistered() {
        assertNotNull(DWMItems.SPECIAL_WEAPONS_DALEK_SPAWN_EGG);
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "special_weapons_dalek_spawn_egg"),
                BuiltInRegistries.ITEM.getKey(DWMItems.SPECIAL_WEAPONS_DALEK_SPAWN_EGG)
        );
    }

    @Test
    void hitboxIsTallerThanTheClassicDalek() {
        assertEquals(0.94F, DWMEntityTypes.SPECIAL_WEAPONS_DALEK.getWidth(), 0.001F);
        assertEquals(2.15F, DWMEntityTypes.SPECIAL_WEAPONS_DALEK.getHeight(), 0.001F);
        assertTrue(DWMEntityTypes.SPECIAL_WEAPONS_DALEK.getHeight() > DWMEntityTypes.DALEK.getHeight());
        assertTrue(DalekChassis.SPECIAL_WEAPONS.gunHeight() > DalekChassis.CLASSIC_1963.gunHeight());
        assertTrue(DalekChassis.SPECIAL_WEAPONS.gunForward() > DalekChassis.PARADIGM.gunForward());
    }

    @Test
    void familyHasASingleVariantOnItsOwnChassis() {
        assertEquals(DalekVariant.SPECIAL_WEAPONS, DalekFamily.SPECIAL_WEAPONS.defaultVariant());
        assertEquals(DalekChassis.SPECIAL_WEAPONS, DalekVariant.SPECIAL_WEAPONS.chassis());
        assertEquals(DalekFamily.SPECIAL_WEAPONS, DalekChassis.SPECIAL_WEAPONS.family());
        assertEquals("special_weapons", DalekVariant.SPECIAL_WEAPONS.getSerializedName());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/dalek/special_weapons.png"),
                DalekVariant.SPECIAL_WEAPONS.textureLocation()
        );
        assertEquals(DalekVariant.SPECIAL_WEAPONS, DalekVariant.pick(RandomSource.create(1L), DalekFamily.SPECIAL_WEAPONS));
        assertEquals(DalekVariant.SPECIAL_WEAPONS, DalekVariant.byId("special_weapons", DalekFamily.SPECIAL_WEAPONS));
    }

    @Test
    void foreignVariantsFallBackToTheSpecialWeaponsSkin() {
        assertEquals(
                DalekVariant.SPECIAL_WEAPONS,
                DalekVariant.byId(DalekVariant.PARADIGM_SUPREME.getSerializedName(), DalekFamily.SPECIAL_WEAPONS)
        );
        assertEquals(
                DalekVariant.PARADIGM_DRONE,
                DalekVariant.byId("special_weapons", DalekFamily.PARADIGM)
        );
        assertEquals(
                DalekVariant.CLASSIC_1963,
                DalekVariant.byId("special_weapons", DalekFamily.CLASSIC)
        );
    }

    @Test
    void newVariantIsAppendedSoSavedOrdinalsStayValid() {
        DalekVariant[] values = DalekVariant.values();
        assertEquals(DalekVariant.SPECIAL_WEAPONS, values[values.length - 1]);
        assertEquals(DalekVariant.PARADIGM_SUPREME, values[values.length - 2]);
    }
}
