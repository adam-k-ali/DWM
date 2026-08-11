package com.adamkali.dwm.tardis.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerLocatorLogicTest {
    @Test
    void filterExcluding_removesSelfAndSortsByName() {
        UUID self = UUID.randomUUID();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<PlayerLocatorLogic.PlayerEntry> input = List.of(
                new PlayerLocatorLogic.PlayerEntry(b, "Zoe"),
                new PlayerLocatorLogic.PlayerEntry(self, "Doctor"),
                new PlayerLocatorLogic.PlayerEntry(a, "Amy")
        );

        List<PlayerLocatorLogic.PlayerEntry> filtered = PlayerLocatorLogic.filterExcluding(input, self);

        assertEquals(2, filtered.size());
        assertEquals("Amy", filtered.get(0).name());
        assertEquals("Zoe", filtered.get(1).name());
        assertFalse(filtered.stream().anyMatch(e -> self.equals(e.uuid())));
    }

    @Test
    void filterExcluding_emptyOrNull() {
        assertTrue(PlayerLocatorLogic.filterExcluding(null, UUID.randomUUID()).isEmpty());
        assertTrue(PlayerLocatorLogic.filterExcluding(List.of(), UUID.randomUUID()).isEmpty());
    }

    @Test
    void resolve_and_isOnline_nullSafe() {
        assertTrue(PlayerLocatorLogic.resolve(null, UUID.randomUUID()).isEmpty());
        assertTrue(PlayerLocatorLogic.findOnline(null, UUID.randomUUID()).isEmpty());
        assertFalse(PlayerLocatorLogic.isOnline(null, UUID.randomUUID()));
        assertTrue(PlayerLocatorLogic.onlinePlayers(null, null).isEmpty());
    }
}
