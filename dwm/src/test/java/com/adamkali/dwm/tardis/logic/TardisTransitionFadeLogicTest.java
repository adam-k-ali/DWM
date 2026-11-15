package com.adamkali.dwm.tardis.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.world.level.Level;
import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TardisTransitionFadeLogicTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void enterAndExitAreTardisTransitions() {
        assertTrue(TardisTransitionFadeLogic.isTardisTransition(Level.OVERWORLD, TardisDimensions.TARDIS_WORLD_KEY));
        assertTrue(TardisTransitionFadeLogic.isTardisTransition(TardisDimensions.TARDIS_WORLD_KEY, Level.NETHER));
    }

    @Test
    void otherChangesAreNot() {
        assertFalse(TardisTransitionFadeLogic.isTardisTransition(Level.OVERWORLD, Level.NETHER));
        assertFalse(TardisTransitionFadeLogic.isTardisTransition(Level.OVERWORLD, Level.OVERWORLD));
        assertFalse(TardisTransitionFadeLogic.isTardisTransition(TardisDimensions.TARDIS_WORLD_KEY, TardisDimensions.TARDIS_WORLD_KEY));
        assertFalse(TardisTransitionFadeLogic.isTardisTransition(null, Level.OVERWORLD));
    }

    @Test
    void fadeIsMonotonicFromBlackToClear() {
        assertEquals(1.0F, TardisTransitionFadeLogic.fadeInAlpha(0));
        assertEquals(0.0F, TardisTransitionFadeLogic.fadeInAlpha(TardisTransitionFadeLogic.FADE_IN_MS));
        float prev = 1.0F;
        for (long t = 0; t <= TardisTransitionFadeLogic.FADE_IN_MS; t += 25) {
            float a = TardisTransitionFadeLogic.fadeInAlpha(t);
            assertTrue(a <= prev);
            prev = a;
        }
    }
}
