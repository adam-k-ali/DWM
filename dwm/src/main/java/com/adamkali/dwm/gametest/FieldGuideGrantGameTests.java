package com.adamkali.dwm.gametest;

import com.adamkali.dwm.guide.FieldGuideGrant;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FieldGuideGrantGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void tryGive_onlyOnce(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        if (!FieldGuideGrant.tryGive(player)) {
            throw new AssertionError("Expected first grant to succeed");
        }
        if (countFieldGuides(player.getInventory()) != 1) {
            throw new AssertionError("Expected one Field Guide after first grant");
        }
        if (FieldGuideGrant.tryGive(player)) {
            throw new AssertionError("Expected second grant to be skipped");
        }
        if (countFieldGuides(player.getInventory()) != 1) {
            throw new AssertionError("Expected still one Field Guide after second grant");
        }
        context.succeed();
    }

    private static int countFieldGuides(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(DWMItems.FIELD_GUIDE)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
