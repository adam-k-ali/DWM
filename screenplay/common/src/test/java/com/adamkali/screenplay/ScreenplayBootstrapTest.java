package com.adamkali.screenplay;

import com.adamkali.screenplay.platform.ScreenplayPlatform;
import net.minecraft.client.Minecraft;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ScreenplayBootstrapTest {
    @AfterEach
    void clearProperties() {
        System.clearProperty(ScreenplayBootstrap.SCENARIO_PROPERTY);
        System.clearProperty(ScreenplayBootstrap.REPORT_PROPERTY);
        System.clearProperty(ScreenplayBootstrap.TIMEOUT_PROPERTY);
    }

    @Test
    void startNoopsWhenScenarioPropertyUnset() {
        AtomicBoolean registered = new AtomicBoolean(false);
        ScreenplayBootstrap.start(new RecordingPlatform(registered));
        assertFalse(registered.get());
    }

    @Test
    void startNoopsWhenScenarioPropertyBlank() {
        System.setProperty(ScreenplayBootstrap.SCENARIO_PROPERTY, "  ");
        AtomicBoolean registered = new AtomicBoolean(false);
        ScreenplayBootstrap.start(new RecordingPlatform(registered));
        assertFalse(registered.get());
    }

    private static final class RecordingPlatform implements ScreenplayPlatform {
        private final AtomicBoolean registered;

        private RecordingPlatform(AtomicBoolean registered) {
            this.registered = registered;
        }

        @Override
        public void registerEndClientTick(Consumer<Minecraft> tickHandler) {
            registered.set(true);
        }
    }
}
