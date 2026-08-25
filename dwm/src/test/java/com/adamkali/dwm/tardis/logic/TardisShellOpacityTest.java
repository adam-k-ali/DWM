package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TardisShellOpacityTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void idleAndInFlightAreOpaque() {
        assertEquals(1.0f, TardisShellOpacity.alpha(TardisTravelPhase.IDLE, 0.0f));
        assertEquals(1.0f, TardisShellOpacity.alpha(TardisTravelPhase.IN_FLIGHT, 40.0f));
        assertEquals(1.0f, TardisShellOpacity.alpha(null, 10.0f));
    }

    @Test
    void materialisingFadesIn() {
        assertEquals(0.0f, TardisShellOpacity.alpha(TardisTravelPhase.MATERIALISING, 0.0f));
        assertEquals(
                0.5f,
                TardisShellOpacity.alpha(
                        TardisTravelPhase.MATERIALISING,
                        TardisTravelService.MATERIALISING_DURATION_TICKS / 2.0f
                )
        );
        assertEquals(
                1.0f,
                TardisShellOpacity.alpha(
                        TardisTravelPhase.MATERIALISING,
                        TardisTravelService.MATERIALISING_DURATION_TICKS
                )
        );
        assertEquals(1.0f, TardisShellOpacity.alpha(TardisTravelPhase.MATERIALISING, 10_000.0f));
    }

    @Test
    void dematerialisingFadesOutUntilShellRemoved() {
        assertEquals(1.0f, TardisShellOpacity.alpha(TardisTravelPhase.DEMATERIALISING, 0.0f));
        assertEquals(
                0.5f,
                TardisShellOpacity.alpha(
                        TardisTravelPhase.DEMATERIALISING,
                        TardisTravelService.DEMATERIALISING_SHELL_REMOVE_AT_TICK / 2.0f
                )
        );
        assertEquals(
                0.0f,
                TardisShellOpacity.alpha(
                        TardisTravelPhase.DEMATERIALISING,
                        TardisTravelService.DEMATERIALISING_SHELL_REMOVE_AT_TICK
                )
        );
        assertEquals(0.0f, TardisShellOpacity.alpha(TardisTravelPhase.DEMATERIALISING, 10_000.0f));
    }
}
