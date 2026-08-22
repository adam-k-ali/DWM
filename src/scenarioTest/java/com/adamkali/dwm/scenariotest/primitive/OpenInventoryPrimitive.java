package com.adamkali.dwm.scenariotest.primitive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.Map;

public final class OpenInventoryPrimitive extends NoArgPrimitive {
    @Override
    public String name() {
        return "openInventory";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        return requireNoArguments(arguments, source);
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        if (isInventoryScreen(context.screen())) {
            return true;
        }
        Minecraft client = context.client();
        if (client.player == null) {
            return false;
        }
        context.logger().info("Opening inventory on {}", client.player.getName().getString());
        client.gui.setScreen(new InventoryScreen(client.player));
        return isInventoryScreen(context.screen());
    }

    private static boolean isInventoryScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }
}
