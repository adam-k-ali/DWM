package com.adamkali.dwm.scenariotest.primitive;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScenarioPrimitives {
    private static final Map<String, ScenarioPrimitive> BY_NAME = List.of(
            new LaunchGamePrimitive(),
            new DebugScreenPrimitive(),
            new AssertVisiblePrimitive(),
            new ClickPrimitive(),
            new CaptureScreenshotPrimitive(),
            new StartVanillaServerPrimitive(),
            new CreateWorldPrimitive(),
            new KeyboardInputPrimitive(),
            new RunCommandPrimitive(),
            new WaitUntilPrimitive(),
            new WaitTicksPrimitive(),
            new OpenInventoryPrimitive(),
            new CloseScreenPrimitive(),
            new SelectHotbarPrimitive(),
            new LookAtPrimitive(),
            new UseItemPrimitive()
    ).stream().collect(Collectors.toUnmodifiableMap(ScenarioPrimitive::name, Function.identity()));

    private ScenarioPrimitives() {
    }

    public static ScenarioPrimitive find(String name) {
        return BY_NAME.get(name);
    }
}
