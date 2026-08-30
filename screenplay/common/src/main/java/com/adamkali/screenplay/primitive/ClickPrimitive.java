package com.adamkali.screenplay.primitive;

import com.adamkali.screenplay.ScenarioException;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.Map;
import java.util.Optional;

public final class ClickPrimitive extends SelectorPrimitive {
    @Override
    public String name() {
        return "click";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        Map<String, Object> selector = requireSelector(arguments, source);
        if ("screen".equals(selector.get("type"))) {
            throw new ScenarioException(source + ": step 'click' cannot target type 'screen'");
        }
        return selector;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        Screen screen = context.screen();
        Optional<AbstractWidget> widget = context.widgetFinder().find(screen, context.arguments());
        if (widget.isEmpty() || !widget.get().active) {
            return false;
        }
        AbstractWidget target = widget.get();
        context.logger().info("Activating {} on {} (visible widgets: {})",
                target.getClass().getName(),
                screen == null ? "<none>" : screen.getClass().getName(),
                context.widgetFinder().visibleWidgets(screen));
        MouseButtonEvent event = new MouseButtonEvent(
                target.getX() + target.getWidth() / 2.0,
                target.getY() + target.getHeight() / 2.0,
                new MouseButtonInfo(0, 0)
        );
        // Click the matched widget directly. Screen.getChildAt() returns the first
        // overlapping active child, and Screen.mouseClicked() still reports success
        // when that child ignores the press — so a named button can "succeed" without
        // running its handler.
        boolean handled = target.mouseClicked(event, false);
        if (handled && screen != null && target.shouldTakeFocusAfterInteraction()) {
            screen.setFocused(target);
        }
        return handled;
    }
}
