package com.adamkali.screenplay.primitive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Built-in primitives plus extras discovered via {@link ServiceLoader}.
 */
public final class ScenarioPrimitives {
    private static final Map<String, ScenarioPrimitive> BY_NAME = loadRegistry();

    private ScenarioPrimitives() {
    }

    private static Map<String, ScenarioPrimitive> loadRegistry() {
        Map<String, ScenarioPrimitive> registry = new HashMap<>(List.of(
                new LaunchGamePrimitive(),
                new DebugScreenPrimitive(),
                new AssertVisiblePrimitive(),
                new ClickPrimitive(),
                new CaptureScreenshotPrimitive(),
                new StartVanillaServerPrimitive(),
                new CreateWorldPrimitive(),
                new KeyboardInputPrimitive(),
                new PressKeyPrimitive(),
                new RunCommandPrimitive(),
                new WaitUntilPrimitive(),
                new WaitTicksPrimitive(),
                new OpenInventoryPrimitive(),
                new CloseScreenPrimitive(),
                new SetSneakingPrimitive(),
                new SelectHotbarPrimitive(),
                new LookAtPrimitive(),
                new UseItemPrimitive(),
                new InteractWithEntityPrimitive(),
                new WalkUntilPrimitive()
        ).stream().collect(Collectors.toMap(ScenarioPrimitive::name, Function.identity())));

        ServiceLoader.load(ScenarioPrimitive.class).forEach(primitive -> {
            ScenarioPrimitive previous = registry.put(primitive.name(), primitive);
            if (previous != null && previous.getClass() != primitive.getClass()) {
                throw new IllegalStateException("Duplicate Screenplay primitive '" + primitive.name()
                        + "': " + previous.getClass().getName() + " and " + primitive.getClass().getName());
            }
        });
        return Map.copyOf(registry);
    }

    public static ScenarioPrimitive find(String name) {
        return BY_NAME.get(name);
    }
}
