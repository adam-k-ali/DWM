package com.adamkali.dwm.model.armor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaSuitModelTest {
    @Test
    void layerSetUsesEvaSuitId() {
        assertEquals("eva_suit", EvaSuitModel.LAYER_SET.head().model().getPath());
        assertEquals("helmet", EvaSuitModel.LAYER_SET.head().layer());
        assertEquals("chestplate", EvaSuitModel.LAYER_SET.chest().layer());
        assertEquals("leggings", EvaSuitModel.LAYER_SET.legs().layer());
        assertEquals("boots", EvaSuitModel.LAYER_SET.feet().layer());
    }

    @Test
    void helmetBakesSealedShellVisorAndNeckRing() {
        ModelPart root = EvaSuitModel.createHelmetLayer().bakeRoot();
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
        ModelPart root = EvaSuitModel.createChestLayer().bakeRoot();
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
        ModelPart root = EvaSuitModel.createLeggingsLayer().bakeRoot();
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
        ModelPart root = EvaSuitModel.createBootsLayer().bakeRoot();
        assertHumanoidPartsPresent(root);
        assertFalse(root.getChild("right_leg").isEmpty());
        assertFalse(root.getChild("left_leg").isEmpty());
        assertTrue(root.getChild("head").isEmpty());
        assertTrue(root.getChild("body").isEmpty());
        assertTrue(root.getChild("right_arm").isEmpty());
        assertTrue(root.getChild("left_arm").isEmpty());
    }

    @Test
    void createArmorLayerSetReturnsFourLayers() {
        ArmorModelSet<LayerDefinition> set = EvaSuitModel.createArmorLayerSet();
        assertTrue(set.head().bakeRoot().getChild("head").hasChild("visor"));
        assertTrue(set.chest().bakeRoot().getChild("body").hasChild("backpack"));
        assertFalse(set.legs().bakeRoot().getChild("right_leg").isEmpty());
        assertFalse(set.feet().bakeRoot().getChild("left_leg").isEmpty());
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
