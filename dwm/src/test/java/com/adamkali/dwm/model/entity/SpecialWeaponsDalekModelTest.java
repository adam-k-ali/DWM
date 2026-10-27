package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelJson;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialWeaponsDalekModelTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void bodyLayerKeepsTheDalekPartNamesWithCannonAndPackInsteadOfPlunger() {
        ModelPart root = EntityModelJson.loadClasspath(SpecialWeaponsDalekModel.LAYER_LOCATION.model()).bakeRoot();
        assertTrue(root.hasChild("skirt"));
        assertTrue(root.hasChild("shoulders"));
        assertTrue(root.getChild("shoulders").hasChild("gun"));
        assertTrue(root.getChild("shoulders").hasChild("weapon_pack"));
        assertFalse(root.getChild("shoulders").hasChild("plunger"));
        assertTrue(root.hasChild("neck"));
        assertTrue(root.hasChild("head"));
        assertTrue(root.getChild("head").hasChild("eyestalk"));
        assertTrue(root.getChild("head").hasChild("light_left"));
        assertTrue(root.getChild("head").hasChild("light_right"));
    }

    @Test
    void layerLocationUsesSpecialWeaponsDalekId() {
        assertEquals("special_weapons_dalek", SpecialWeaponsDalekModel.LAYER_LOCATION.model().getPath());
        assertEquals("main", SpecialWeaponsDalekModel.LAYER_LOCATION.layer());
    }

    @Test
    void meshMatchesTheHitboxAndTowersOverTheClassicCasings() {
        float height = DalekMeshes.height(SpecialWeaponsDalekModel.LAYER_LOCATION);
        // Matches the 2.15-block hitbox to within a pixel.
        assertEquals(2.15F * 16.0F, height, 1.0F);
        assertTrue(height > DalekMeshes.height(DalekModel.LAYER_LOCATION));
        assertTrue(height > DalekMeshes.height(TimeWarDalekModel.LAYER_LOCATION));
    }
}
