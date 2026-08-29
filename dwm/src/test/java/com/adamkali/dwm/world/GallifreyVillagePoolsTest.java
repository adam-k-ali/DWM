package com.adamkali.dwm.world;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GallifreyVillagePoolsTest {
    @Test
    void poolAliasesRewritePlainsJigsawTargets() {
        Set<Identifier> sources = new HashSet<>(DWMVillagePools.plainsAliasSources());
        assertEquals(
                Set.of(
                        Identifier.withDefaultNamespace("village/plains/streets"),
                        Identifier.withDefaultNamespace("village/plains/houses"),
                        Identifier.withDefaultNamespace("village/plains/terminators"),
                        Identifier.withDefaultNamespace("village/plains/decor"),
                        Identifier.withDefaultNamespace("village/plains/trees")
                ),
                sources
        );
        assertFalse(sources.contains(Identifier.withDefaultNamespace("village/plains/villagers")));
        assertFalse(sources.contains(Identifier.withDefaultNamespace("village/common/iron_golem")));
    }
}
