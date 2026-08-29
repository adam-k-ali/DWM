package com.adamkali.dwm.world;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GallifreyVillagePoolAliasTest {
    @Test
    void aliasesRewritePlainsPoolsAndLeaveVillagersUnaliased() {
        List<Identifier> sources = DWMVillagePools.plainsAliasSources();
        assertEquals(
                List.of(
                        Identifier.withDefaultNamespace("village/plains/streets"),
                        Identifier.withDefaultNamespace("village/plains/houses"),
                        Identifier.withDefaultNamespace("village/plains/terminators"),
                        Identifier.withDefaultNamespace("village/plains/decor"),
                        Identifier.withDefaultNamespace("village/plains/trees")
                ),
                sources
        );
        assertFalse(sources.contains(Identifier.withDefaultNamespace("village/plains/villagers")));
        assertFalse(sources.contains(Identifier.withDefaultNamespace("village/common/cats")));
        assertFalse(sources.contains(Identifier.withDefaultNamespace("village/common/iron_golem")));
    }
}
