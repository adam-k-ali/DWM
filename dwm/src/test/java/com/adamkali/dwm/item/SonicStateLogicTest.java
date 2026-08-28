package com.adamkali.dwm.item;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic coverage for sonic field-mode state. ItemStack component binding needs a full
 * game bootstrap (GameTests); these tests stay on {@link SonicState} + cycle helpers.
 */
class SonicStateLogicTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        DWMDataComponents.initialize();
    }

    @Test
    void craftedOpenOnly_unlocksOpenAlone() {
        SonicState state = SonicState.craftedOpenOnly();
        assertTrue(state.isUnlocked(SonicFieldMode.OPEN));
        assertFalse(state.isUnlocked(SonicFieldMode.SHATTER));
        assertFalse(state.isUnlocked(SonicFieldMode.PRIME));
        assertFalse(state.isUnlocked(SonicFieldMode.DISRUPT));
        assertFalse(state.isUnlocked(SonicFieldMode.SHEAR));
        assertFalse(state.isUnlocked(SonicFieldMode.SEAL));
        assertFalse(state.isUnlocked(SonicFieldMode.SCAN));
        assertFalse(state.isUnlocked(SonicFieldMode.PING));
        assertEquals(SonicFieldMode.OPEN, state.selected());
        assertFalse(state.tardisPaired());
    }

    @Test
    void fullyUnlocked_hasAllModes() {
        SonicState state = SonicState.fullyUnlocked();
        for (SonicFieldMode mode : SonicFieldMode.values()) {
            assertTrue(state.isUnlocked(mode), mode.name());
        }
        assertEquals(SonicFieldMode.OPEN, state.selected());
    }

    @Test
    void install_viaWithUnlocked_isIdempotentShape() {
        SonicState open = SonicState.craftedOpenOnly();
        SonicState withShatter = open.withUnlocked(SonicFieldMode.SHATTER);
        assertTrue(withShatter.isUnlocked(SonicFieldMode.SHATTER));
        assertEquals(SonicFieldMode.OPEN, withShatter.selected());
        assertEquals(withShatter, withShatter.withUnlocked(SonicFieldMode.SHATTER));
    }

    @Test
    void select_viaWithSelected() {
        SonicState state = SonicState.craftedOpenOnly().withUnlocked(SonicFieldMode.PRIME);
        SonicState selected = state.withSelected(SonicFieldMode.PRIME);
        assertEquals(SonicFieldMode.PRIME, selected.selected());
        assertTrue(selected.isUnlocked(SonicFieldMode.OPEN));
    }

    @Test
    void unlockedInCycleOrder_skipsLocked() {
        SonicState state = new SonicState(
                EnumSet.of(SonicFieldMode.OPEN, SonicFieldMode.PRIME),
                SonicFieldMode.OPEN,
                false
        );
        assertEquals(
                List.of(SonicFieldMode.OPEN, SonicFieldMode.PRIME),
                SonicStateLogic.unlockedInCycleOrder(state)
        );
    }

    @Test
    void peekCycle_skipsLockedAndDoesNotRequireStackMutation() {
        // Simulate Open + Prime (Shatter locked) starting at Open → next is Prime
        SonicState state = new SonicState(
                EnumSet.of(SonicFieldMode.OPEN, SonicFieldMode.PRIME),
                SonicFieldMode.OPEN,
                false
        );
        List<SonicFieldMode> unlocked = SonicStateLogic.unlockedInCycleOrder(state);
        assertEquals(2, unlocked.size());
        int index = unlocked.indexOf(state.selected());
        SonicFieldMode next = unlocked.get(Math.floorMod(index + 1, unlocked.size()));
        assertEquals(SonicFieldMode.PRIME, next);
        // Shatter is not in the unlocked list
        assertFalse(unlocked.contains(SonicFieldMode.SHATTER));
    }

    @Test
    void peekCycle_noopWhenOnlyOpenUnlocked() {
        SonicState state = SonicState.craftedOpenOnly();
        assertEquals(1, SonicStateLogic.unlockedInCycleOrder(state).size());
    }

    @Test
    void copySemantics_missingMeansFullyUnlocked() {
        // Missing component is modelled as fullyUnlocked for gameplay reads
        SonicState missingEquivalent = SonicState.fullyUnlocked();
        assertEquals(8, missingEquivalent.unlockedCount());
        assertEquals(SonicFieldMode.OPEN, missingEquivalent.selected());
    }

    @Test
    void codec_roundTrips() {
        SonicState original = new SonicState(
                EnumSet.of(SonicFieldMode.OPEN, SonicFieldMode.SHEAR),
                SonicFieldMode.SHEAR,
                true
        );
        var encoded = SonicState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        SonicState decoded = SonicState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }

    @Test
    void codec_craftedOpenOnlyRoundTrips() {
        SonicState original = SonicState.craftedOpenOnly();
        var encoded = SonicState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        SonicState decoded = SonicState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }

    @Test
    void pair_unlocksTardisModesTogether() {
        SonicState crafted = SonicState.craftedOpenOnly();
        assertTrue(SonicStateLogic.needsHandshake(crafted));
        SonicState paired = SonicStateLogic.pair(crafted);
        assertTrue(paired.tardisPaired());
        assertTrue(paired.isUnlocked(SonicFieldMode.SEAL));
        assertTrue(paired.isUnlocked(SonicFieldMode.SCAN));
        assertTrue(paired.isUnlocked(SonicFieldMode.PING));
        assertEquals(SonicFieldMode.OPEN, paired.selected());
        assertFalse(SonicStateLogic.needsHandshake(paired));
    }

    @Test
    void fullyUnlocked_skipsHandshakeBecauseTardisModesArePresent() {
        SonicState full = SonicState.fullyUnlocked();
        assertFalse(full.tardisPaired());
        assertFalse(SonicStateLogic.needsHandshake(full));
        assertEquals(8, full.unlockedCount());
    }

    @Test
    void pair_onFullyUnlocked_onlySetsTardisPaired() {
        SonicState paired = SonicStateLogic.pair(SonicState.fullyUnlocked());
        assertTrue(paired.tardisPaired());
        assertEquals(8, paired.unlockedCount());
    }
}
