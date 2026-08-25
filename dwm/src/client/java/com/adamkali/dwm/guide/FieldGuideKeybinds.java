package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class FieldGuideKeybinds {
    private static final KeyMapping.Category DWM_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dwm")
    );
    private static KeyMapping openGuideKey;

    private FieldGuideKeybinds() {
    }

    public static void initialize() {
        openGuideKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.dwm.field_guide",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                DWM_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuideKey.consumeClick()) {
                openGuide(client);
            }
        });
    }

    public static void openGuide(Minecraft client) {
        if (client.player == null) {
            return;
        }
        if (client.gui.screen() == null) {
            FieldGuideScreens.openDirect(client);
        }
    }
}
