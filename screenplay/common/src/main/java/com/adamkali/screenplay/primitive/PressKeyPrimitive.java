package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

import java.util.Locale;
import java.util.Map;

public final class PressKeyPrimitive implements ScenarioPrimitive {
    @Override
    public String name() {
        return "pressKey";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        Object scalar = arguments.get("key");
        if (scalar instanceof String key && !key.isBlank()) {
            resolveKey(key);
            return Map.of("key", key.trim().toLowerCase(Locale.ROOT));
        }
        if (arguments.size() == 1 && arguments.containsKey("text")) {
            Object value = arguments.get("text");
            if (value instanceof String key && !key.isBlank()) {
                resolveKey(key);
                return Map.of("key", key.trim().toLowerCase(Locale.ROOT));
            }
        }
        throw new ScenarioException(source + ": pressKey requires a non-empty string key");
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        if (context.client().player == null) {
            return false;
        }
        String keyName = (String) context.arguments().get("key");
        InputConstants.Key key = resolveKey(keyName);
        context.logger().info("Pressing key {}", key.getName());

        // F1 / key.toggleGui: hide HUD + first-person hand for stable screenshot
        // compares. Apply via Hud.toggle() so the change is immediate; KeyMapping
        // click alone waits for Gui.handleKeybinds consumeClick on a later tick.
        if ("f1".equals(keyName)) {
            if (!context.client().gui.hud.isHidden()) {
                context.client().gui.hud.toggle();
            }
            return true;
        }

        if (isEscape(key)) {
            Screen screen = context.screen();
            KeyEvent event = new KeyEvent(key.getValue(), key.getValue(), 0);
            if (screen != null && screen.keyPressed(event)) {
                return true;
            }
            context.client().pauseGame(false);
            return true;
        }

        Screen screen = context.screen();
        KeyEvent event = new KeyEvent(key.getValue(), key.getValue(), 0);
        if (screen != null && screen.keyPressed(event)) {
            return true;
        }

        KeyMapping.click(key);
        context.client().handleGlobalKeyPress(key, true);
        KeyMapping.set(key, false);
        return true;
    }

    private static boolean isEscape(InputConstants.Key key) {
        return InputConstants.getKey("key.keyboard.escape").equals(key);
    }

    public static InputConstants.Key resolveKey(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "escape", "esc" -> InputConstants.getKey("key.keyboard.escape");
            case "space" -> InputConstants.getKey("key.keyboard.space");
            case "f1" -> InputConstants.getKey("key.keyboard.f1");
            default -> {
                if (normalized.length() == 1 && normalized.charAt(0) >= 'a' && normalized.charAt(0) <= 'z') {
                    yield InputConstants.getKey("key.keyboard." + normalized);
                }
                throw new ScenarioException("Unsupported pressKey key '" + name
                        + "'. Supported examples: g, escape, space, f1.");
            }
        };
    }
}
