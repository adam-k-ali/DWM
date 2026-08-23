package com.adamkali.sightline.primitive;

import com.adamkali.sightline.ScenarioException;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;

import java.util.Map;

public final class KeyboardInputPrimitive implements ScenarioPrimitive {
    @Override
    public String name() {
        return "keyboardInput";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        for (String key : arguments.keySet()) {
            if (!"text".equals(key)) {
                throw new ScenarioException(source + ": keyboardInput does not accept '" + key + "'");
            }
        }
        Object value = arguments.get("text");
        if (!(value instanceof String string) || string.isBlank()) {
            throw new ScenarioException(source + ": keyboardInput requires a non-empty string 'text'");
        }
        return arguments;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Screen screen = context.screen();
        if (screen == null || !(screen.getFocused() instanceof EditBox editBox) || !editBox.canConsumeInput()) {
            return false;
        }
        String text = (String) context.arguments().get("text");
        context.logger().info("Typing {} characters into {} on {}",
                text.length(),
                editBox.getClass().getName(),
                screen.getClass().getName());
        for (int index = 0; index < text.length(); ) {
            int codepoint = text.codePointAt(index);
            if (!screen.charTyped(new CharacterEvent(codepoint))) {
                throw new ScenarioException("keyboardInput rejected codepoint at index " + index
                        + " in \"" + text + "\" from " + context.source());
            }
            index += Character.charCount(codepoint);
        }
        return true;
    }
}
