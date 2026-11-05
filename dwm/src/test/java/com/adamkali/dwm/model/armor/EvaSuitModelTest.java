package com.adamkali.dwm.model.armor;

import com.adamkali.dwm.model.json.TestModelLoading;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaSuitModelTest {
    @Test
    void layerSetUsesEvaSuitId() {
        assertEquals("eva_suit_helmet", EvaSuitModel.LAYER_SET.head().model().getPath());
        assertEquals("eva_suit_chestplate", EvaSuitModel.LAYER_SET.chest().model().getPath());
        assertEquals("eva_suit_leggings", EvaSuitModel.LAYER_SET.legs().model().getPath());
        assertEquals("eva_suit_boots", EvaSuitModel.LAYER_SET.feet().model().getPath());
    }

    @Test
    void slotTexturesComeFromJson() {
        Identifier outer = Identifier.parse("dwm:textures/entity/equipment/humanoid/eva_suit.png");
        assertEquals(outer, TestModelLoading.texture(EvaSuitModel.LAYER_SET.head()));
        assertEquals(outer, TestModelLoading.texture(EvaSuitModel.LAYER_SET.chest()));
        assertEquals(Identifier.parse("dwm:textures/entity/equipment/humanoid_leggings/eva_suit.png"),
                TestModelLoading.texture(EvaSuitModel.LAYER_SET.legs()));
        assertEquals(outer, TestModelLoading.texture(EvaSuitModel.LAYER_SET.feet()));
    }

    @Test
    void helmetBakesSealedShellVisorAndNeckRing() {
        ModelPart root = TestModelLoading.bake(EvaSuitModel.LAYER_SET.head());
        assertHumanoidPartsPresent(root);
        ModelPart head = root.getChild("head");
        assertTrue(head.hasChild("helmet_shell"));
        assertTrue(head.hasChild("visor"));
        assertTrue(head.hasChild("neck_ring"));
        assertTrue(root.getChild("body").isEmpty());
        assertTrue(root.getChild("right_arm").isEmpty());
        assertTrue(root.getChild("left_arm").isEmpty());
    }

    @Test
    void chestBakesBackpackAndFullSleeves() {
        ModelPart root = TestModelLoading.bake(EvaSuitModel.LAYER_SET.chest());
        assertHumanoidPartsPresent(root);
        ModelPart body = root.getChild("body");
        assertFalse(body.isEmpty());
        assertTrue(body.hasChild("backpack"));
        assertFalse(root.getChild("right_arm").isEmpty());
        assertFalse(root.getChild("left_arm").isEmpty());
        assertTrue(root.getChild("head").isEmpty());
        assertTrue(root.getChild("right_leg").isEmpty());
        assertTrue(root.getChild("left_leg").isEmpty());
    }

    @Test
    void leggingsBakeWaistAndLegsOnly() {
        ModelPart root = TestModelLoading.bake(EvaSuitModel.LAYER_SET.legs());
        assertHumanoidPartsPresent(root);
        assertFalse(root.getChild("body").isEmpty());
        assertFalse(root.getChild("right_leg").isEmpty());
        assertFalse(root.getChild("left_leg").isEmpty());
        assertTrue(root.getChild("head").isEmpty());
        assertTrue(root.getChild("right_arm").isEmpty());
        assertTrue(root.getChild("left_arm").isEmpty());
    }

    @Test
    void bootsBakeLowerLegsOnly() {
        ModelPart root = TestModelLoading.bake(EvaSuitModel.LAYER_SET.feet());
        assertHumanoidPartsPresent(root);
        assertFalse(root.getChild("right_leg").isEmpty());
        assertFalse(root.getChild("left_leg").isEmpty());
        assertTrue(root.getChild("head").isEmpty());
        assertTrue(root.getChild("body").isEmpty());
        assertTrue(root.getChild("right_arm").isEmpty());
        assertTrue(root.getChild("left_arm").isEmpty());
    }

    private static void assertHumanoidPartsPresent(ModelPart root) {
        assertTrue(root.hasChild("head"));
        assertTrue(root.getChild("head").hasChild("hat"));
        assertTrue(root.hasChild("body"));
        assertTrue(root.hasChild("right_arm"));
        assertTrue(root.hasChild("left_arm"));
        assertTrue(root.hasChild("right_leg"));
        assertTrue(root.hasChild("left_leg"));
    }
}
