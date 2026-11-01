package com.adamkali.dwm.world.structure;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DalekTrenchStructureTest {
    private static final List<BoundingBox> PIECES = List.of(
            new BoundingBox(0, 60, 0, 10, 64, 10),
            new BoundingBox(0, 60, 11, 4, 64, 25));

    @Test
    void dryTrenchInsideItsBiomeIsAccepted() {
        assertTrue(DalekTrenchStructure.isSiteValid(PIECES, (x, z) -> 66, (x, z) -> 66, (x, y, z) -> true));
    }

    @Test
    void emptyTrenchHasNothingToReject() {
        assertTrue(DalekTrenchStructure.isSiteValid(List.of(), (x, z) -> 66, (x, z) -> 40, (x, y, z) -> false));
    }

    @Test
    void anySampledColumnUnderWaterRejectsEvenOneBlockDeep() {
        // Only the far corner of the second piece is flooded, one block deep.
        assertFalse(DalekTrenchStructure.isSiteValid(
                PIECES, (x, z) -> 66, (x, z) -> x == 4 && z == 25 ? 65 : 66, (x, y, z) -> true));
    }

    @Test
    void aCentreColumnUnderWaterRejects() {
        assertFalse(DalekTrenchStructure.isSiteValid(
                PIECES, (x, z) -> 66, (x, z) -> x == 5 && z == 5 ? 50 : 66, (x, y, z) -> true));
    }

    @Test
    void aPieceLeavingTheBiomeRejects() {
        assertFalse(DalekTrenchStructure.isSiteValid(PIECES, (x, z) -> 66, (x, z) -> 66, (x, y, z) -> z <= 20));
    }

    @Test
    void biomeIsSampledAtTheBoxCentreHeight() {
        assertTrue(DalekTrenchStructure.isSiteValid(PIECES, (x, z) -> 66, (x, z) -> 66, (x, y, z) -> y == 62));
    }
}
