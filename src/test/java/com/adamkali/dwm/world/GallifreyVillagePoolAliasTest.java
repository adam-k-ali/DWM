package com.adamkali.dwm.world;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GallifreyVillagePoolAliasTest {
    @Test
    void pieceAliasesRewritePlainsPools() {
        assertEquals(
                List.of(
                        Identifier.withDefaultNamespace("village/plains/streets"),
                        Identifier.withDefaultNamespace("village/plains/houses"),
                        Identifier.withDefaultNamespace("village/plains/terminators"),
                        Identifier.withDefaultNamespace("village/plains/decor"),
                        Identifier.withDefaultNamespace("village/plains/trees")
                ),
                DWMVillagePools.plainsAliasSources()
        );
    }

    @Test
    void entityPoolsAliasToEmpty() {
        assertEquals(
                List.of(
                        Identifier.withDefaultNamespace("village/plains/villagers"),
                        Identifier.withDefaultNamespace("village/common/cats"),
                        Identifier.withDefaultNamespace("village/common/iron_golem"),
                        Identifier.withDefaultNamespace("village/common/animals")
                ),
                DWMVillagePools.emptyEntityAliasSources()
        );
        assertEquals(Identifier.withDefaultNamespace("empty"), DWMVillagePools.emptyEntityAliasTarget());
    }
}
