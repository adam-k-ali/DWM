package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Persisted, collision-free plot slots. */
class TardisPlotSlotsTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        clearCache();
        TardisPlotSlots.reset();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearCache();
        TardisPlotSlots.reset();
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void newModelsGetTheLowestFreeSpiralSlotsInOrder() {
        TardisDataModel first = TardisDataLoader.create();
        TardisDataModel second = TardisDataLoader.create();
        TardisDataModel third = TardisDataLoader.create();
        assertEquals(TardisPlotAllocator.spiralSlot(0), TardisPlotSlots.storedSlot(first));
        assertEquals(TardisPlotAllocator.spiralSlot(1), TardisPlotSlots.storedSlot(second));
        assertEquals(TardisPlotAllocator.spiralSlot(2), TardisPlotSlots.storedSlot(third));
        assertEquals(TardisDataModel.CURRENT_PLOT_VERSION, first.plotVersion);
        assertEquals(TardisPlotAllocator.plotOrigin(TardisPlotAllocator.spiralSlot(1)),
                TardisPlotAllocator.plotOrigin(second.uuid));
    }

    @Test
    void idsThatSharedALegacyPlotGetDistinctSlots() {
        // Same low 16 bits on both halves: under the old hash these two TARDISes shared one plot.
        UUID a = new UUID(0x1_0000_0005L, 0x2_0000_0007L);
        UUID b = new UUID(0x7_0000_0005L, 0x9_0000_0007L);
        assertEquals(TardisPlotAllocator.legacyPlotOrigin(a), TardisPlotAllocator.legacyPlotOrigin(b));
        TardisDataLoader.getOrCreate(a);
        TardisDataLoader.getOrCreate(b);
        assertNotEquals(TardisPlotAllocator.plotOrigin(a), TardisPlotAllocator.plotOrigin(b));
    }

    @Test
    void slotsAreStableAndSurviveAReload() throws Exception {
        TardisDataModel model = TardisDataLoader.create();
        PlotSlot slot = TardisPlotSlots.slotOf(model.uuid);
        assertEquals(slot, TardisPlotSlots.slotOf(model.uuid));
        // The slot is on disk straight away, not only after the next periodic save.
        clearCache();
        TardisDataModel reloaded = TardisDataLoader.get(model.uuid);
        assertEquals(slot, TardisPlotSlots.storedSlot(reloaded));
        assertEquals(slot, TardisPlotSlots.slotOf(model.uuid));
    }

    @Test
    void legacySavesWithoutASlotAreGivenOneThatAvoidsEveryStoredSlot() throws Exception {
        // Two models already on disk hold spiral slots 0 and 2; a legacy save without a slot takes 1, the gap.
        writeModel(UUID.randomUUID(), TardisPlotAllocator.spiralSlot(0));
        writeModel(UUID.randomUUID(), TardisPlotAllocator.spiralSlot(2));
        UUID legacy = writeModel(UUID.randomUUID(), null);
        assertNull(TardisPlotSlots.storedSlot(TardisDataLoader.get(legacy)));
        assertTrue(TardisDataLoader.get(legacy).isLegacyPlot());

        assertEquals(TardisPlotAllocator.spiralSlot(1), TardisPlotSlots.slotOf(legacy));
        assertEquals(TardisPlotAllocator.spiralSlot(1), TardisPlotSlots.storedSlot(TardisDataLoader.get(legacy)));
    }

    @Test
    void ownerAtRoundTripsAndNeverAllocates() {
        TardisDataModel model = TardisDataLoader.create();
        PlotSlot slot = TardisPlotSlots.slotOf(model.uuid);
        assertEquals(model.uuid, TardisPlotSlots.ownerAt(slot));
        assertNull(TardisPlotSlots.ownerAt(new PlotSlot(1234, -987)));
        BlockPosCheck.assertFoundAt(model, slot);
    }

    @Test
    void idsWithoutAModelHoldTheirSlotUntilAModelAdoptsIt() {
        UUID transientId = UUID.randomUUID();
        PlotSlot held = TardisPlotSlots.slotOf(transientId);
        // A new TARDIS does not get the held slot.
        TardisDataModel other = TardisDataLoader.create();
        assertNotEquals(held, TardisPlotSlots.storedSlot(other));
        // A model created later for that id keeps it.
        TardisDataModel adopted = TardisDataLoader.getOrCreate(transientId);
        assertEquals(held, TardisPlotSlots.storedSlot(adopted));
    }

    @Test
    void freshModelsNeverTakeAReservedSlot() {
        // Reserve the first three spiral slots for ids that have no model yet.
        Set<PlotSlot> reserved = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            reserved.add(TardisPlotSlots.slotOf(UUID.randomUUID()));
        }
        assertEquals(3, TardisPlotSlots.reservationCount());
        // Both model-creation paths that pick a fresh id allocate around the reservations.
        assertTrue(!reserved.contains(TardisPlotSlots.storedSlot(TardisDataLoader.create())));
        assertTrue(!reserved.contains(TardisPlotSlots.storedSlot(TardisDataLoader.createFoundUnfinished())));
        assertEquals(3, TardisPlotSlots.reservationCount());
    }

    @Test
    void aStoredSlotWinsOverAReservationAndDropsIt() throws Exception {
        UUID id = UUID.randomUUID();
        PlotSlot reserved = TardisPlotSlots.slotOf(id);
        assertEquals(1, TardisPlotSlots.reservationCount());
        // The model turns up on disk afterwards (for example written by another tool) with its own slot.
        PlotSlot onDisk = new PlotSlot(42, -17);
        assertNotEquals(reserved, onDisk);
        writeModel(id, onDisk);
        clearCache();
        assertEquals(onDisk, TardisPlotSlots.slotOf(id));
        assertEquals(0, TardisPlotSlots.reservationCount(), "the stale reservation is dropped");
        assertEquals(id, TardisPlotSlots.ownerAt(onDisk));
        assertNull(TardisPlotSlots.ownerAt(reserved));
    }

    @Test
    void resetForgetsReservationsSoANewWorldStartsClean() {
        UUID id = UUID.randomUUID();
        PlotSlot reserved = TardisPlotSlots.slotOf(id);
        assertEquals(id, TardisPlotSlots.ownerAt(reserved));
        TardisPlotSlots.reset();
        assertEquals(0, TardisPlotSlots.reservationCount());
        assertNull(TardisPlotSlots.ownerAt(reserved));
        // The slot is free again for the next TARDIS created.
        assertEquals(reserved, TardisPlotSlots.storedSlot(TardisDataLoader.create()));
    }

    @Test
    void manyModelsNeverShareASlot() {
        Set<PlotSlot> slots = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            assertTrue(slots.add(TardisPlotSlots.storedSlot(TardisDataLoader.create())));
        }
    }

    @Test
    void lowestFreeFailsLoudlyWhenEverySlotIsTaken() {
        Set<PlotSlot> everything = new HashSet<>() {
            @Override
            public boolean contains(Object o) {
                return true;
            }
        };
        // Walks all ~36M spiral indices before giving up.
        assertThrows(IllegalStateException.class, () -> TardisPlotSlots.lowestFree(everything));
    }

    private UUID writeModel(UUID id, PlotSlot slot) throws IOException {
        TardisDataModel model = new TardisDataModel();
        model.uuid = id;
        if (slot != null) {
            model.plotSlotX = slot.x();
            model.plotSlotZ = slot.z();
            model.plotVersion = TardisDataModel.CURRENT_PLOT_VERSION;
        }
        try (FileWriter writer = new FileWriter(new File(tempDir.toFile(), id + ".json"))) {
            new Gson().toJson(model, writer);
        }
        return id;
    }

    private static void clearCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }

    /** {@code findAtInteriorPos} resolves the window of a slot to its owner. */
    private static final class BlockPosCheck {
        static void assertFoundAt(TardisDataModel model, PlotSlot slot) {
            var anchor = TardisPlotAllocator.plotOrigin(slot);
            assertEquals(model.uuid, TardisDataLoader.findAtInteriorPos(anchor.offset(-4995, 3, 5004)).orElseThrow().uuid);
            assertTrue(TardisDataLoader.findAtInteriorPos(anchor.offset(-4996, 3, 0))
                    .map(found -> !found.uuid.equals(model.uuid)).orElse(true));
        }
    }
}
