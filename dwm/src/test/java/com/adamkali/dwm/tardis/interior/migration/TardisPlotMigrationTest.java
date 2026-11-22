package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.PlotSlot;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.interior.TardisPlotSlots;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The phase, ordering and crash-resume rules of the plot migration, against a fake world. */
class TardisPlotMigrationTest {
    @TempDir
    Path tempDir;

    private final UUID player = UUID.fromString("00000000-0000-0000-0000-00000000beef");
    private final UUID otherPlayer = UUID.fromString("00000000-0000-0000-0000-00000000cafe");

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        clearModelCache();
        TardisPlotSlots.clearUnsavedForTest();
        TardisPlotMigration.resetCacheForTest();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearModelCache();
        TardisPlotSlots.clearUnsavedForTest();
        TardisPlotMigration.resetCacheForTest();
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void migratesEveryLegacyInteriorAndLeavesCurrentTardisesAlone() throws Exception {
        UUID a = writeLegacyModel("00000000-0000-0001-0000-000000000001");
        UUID b = writeLegacyModel("00000000-0000-0002-0000-000000000002");
        UUID empty = writeLegacyModel("00000000-0000-0003-0000-000000000003");
        TardisDataModel current = TardisDataLoader.create();
        FakeWorld world = new FakeWorld(Map.of(a, "console room + library", b, "console room"), Set.of(player));
        FakeStore store = new FakeStore();

        TardisPlotMigration.Report report = TardisPlotMigration.run(world, store);

        assertEquals(new TardisPlotMigration.Report(2, 1, TardisPlotMigration.Phase.NONE), report);
        assertEquals(TardisPlotMigration.Phase.DONE, TardisPlotMigration.phase());
        assertEquals(Map.of(a, "console room + library", b, "console room"), world.pasted);
        assertTrue(world.legacy.isEmpty());
        assertTrue(store.files.isEmpty(), "snapshots are deleted when done");
        for (UUID id : List.of(a, b, empty)) {
            TardisDataModel model = TardisDataLoader.get(id);
            assertFalse(model.isLegacyPlot(), id.toString());
            assertTrue(TardisPlotAllocator.isValidSlot(TardisPlotSlots.storedSlot(model)));
        }
        assertTrue(TardisDataLoader.get(a).isPlotPrepared());
        // An empty legacy plot is only marked current; its new window is prepared on first use.
        assertFalse(TardisDataLoader.get(empty).isPlotPrepared());
        assertFalse(world.ops.contains("capture " + current.uuid), "current TARDISes are never captured");
        assertFalse(world.ops.contains("paste " + empty));
    }

    @Test
    void everyInteriorIsCapturedBeforeAnyIsClearedAndClearedBeforeAnyIsPasted() throws Exception {
        UUID a = writeLegacyModel("00000000-0000-0001-0000-000000000001");
        UUID b = writeLegacyModel("00000000-0000-0002-0000-000000000002");
        FakeWorld world = new FakeWorld(Map.of(a, "A", b, "B"), Set.of());
        TardisPlotMigration.run(world, new FakeStore());
        int lastCapture = lastIndex(world.ops, "capture");
        int firstClear = firstIndex(world.ops, "clear");
        int lastClear = lastIndex(world.ops, "clear");
        int firstFlush = firstIndex(world.ops, "flush");
        int firstPrepare = firstIndex(world.ops, "prepare");
        int firstPaste = firstIndex(world.ops, "paste");
        assertTrue(lastCapture < firstClear, world.ops.toString());
        assertTrue(lastClear < firstFlush && firstFlush < firstPrepare, world.ops.toString());
        assertTrue(firstPrepare < firstPaste, "each new window is emptied before its paste: " + world.ops);
        assertEquals("flush", world.ops.get(world.ops.size() - 1));
    }

    @Test
    void aCrashAtAnyStepResumesToTheSameResult() throws Exception {
        UUID a = writeLegacyModel("00000000-0000-0001-0000-000000000001");
        UUID b = writeLegacyModel("00000000-0000-0002-0000-000000000002");
        Map<UUID, String> interiors = Map.of(a, "A", b, "B");
        // Count the steps of an uninterrupted run, in a scratch directory.
        int steps;
        {
            Path scratch = Files.createDirectories(tempDir.resolve("scratch"));
            TardisDataLoader.tardisSaveDirectory = scratch;
            writeLegacyModel(a.toString());
            writeLegacyModel(b.toString());
            FakeWorld probe = new FakeWorld(interiors, Set.of());
            TardisPlotMigration.run(probe, new FakeStore());
            steps = probe.ops.size();
        }
        assertTrue(steps >= 10, "ops " + steps);
        for (int crashAt = 0; crashAt < steps; crashAt++) {
            Path dir = Files.createDirectories(tempDir.resolve("crash" + crashAt));
            restart(dir);
            writeLegacyModel(a.toString());
            writeLegacyModel(b.toString());
            FakeWorld world = new FakeWorld(interiors, Set.of(player));
            FakeStore store = new FakeStore();
            world.crashAt = crashAt;
            assertThrows(Crash.class, () -> TardisPlotMigration.run(world, store), "crash at " + crashAt);

            restart(dir);
            world.crashAt = -1;
            TardisPlotMigration.run(world, store);

            assertEquals(TardisPlotMigration.Phase.DONE, TardisPlotMigration.phase(), "crash at " + crashAt);
            assertEquals(interiors, world.pasted, "crash at " + crashAt);
            assertTrue(world.legacy.isEmpty(), "crash at " + crashAt);
            assertTrue(store.files.isEmpty(), "crash at " + crashAt);
            assertFalse(TardisDataLoader.get(a).isLegacyPlot(), "crash at " + crashAt);
            assertFalse(TardisDataLoader.get(b).isLegacyPlot(), "crash at " + crashAt);
            assertTrue(TardisPlotMigration.isPlayerPending(player), "crash at " + crashAt);
        }
    }

    @Test
    void aSecondRunDoesNothing() throws Exception {
        UUID a = writeLegacyModel("00000000-0000-0001-0000-000000000001");
        FakeWorld world = new FakeWorld(Map.of(a, "A"), Set.of());
        TardisPlotMigration.run(world, new FakeStore());
        List<String> before = new ArrayList<>(world.ops);
        PlotSlot slot = TardisPlotSlots.storedSlot(TardisDataLoader.get(a));

        restart(tempDir);
        assertSame(TardisPlotMigration.Report.NOTHING, TardisPlotMigration.run(world, new FakeStore()));
        assertEquals(before, world.ops);
        assertEquals(slot, TardisPlotSlots.storedSlot(TardisDataLoader.get(a)));
    }

    @Test
    void aWorldWithNoLegacyTardisesWritesNothing() {
        TardisDataLoader.create();
        assertSame(TardisPlotMigration.Report.NOTHING, TardisPlotMigration.run(new FakeWorld(Map.of(), Set.of(player)), new FakeStore()));
        assertFalse(Files.exists(tempDir.resolve(TardisPlotMigration.DIRECTORY)));
        assertFalse(TardisPlotMigration.isPlayerPending(player));
    }

    @Test
    void playersSavedInsideAMigratedLegacyWindowMoveOnceOnTheirNextJoin() throws Exception {
        UUID a = writeLegacyModel("00000000-0000-0001-0000-000000000001");
        TardisPlotMigration.run(new FakeWorld(Map.of(a, "A"), Set.of(player)), new FakeStore());
        BlockPos legacy = TardisPlotAllocator.legacyPlotOrigin(a);
        Vec3i delta = TardisPlotAllocator.migrationDelta(a);

        assertTrue(TardisPlotMigration.isPlayerPending(player));
        assertFalse(TardisPlotMigration.isPlayerPending(otherPlayer), "players who first join later are never moved");
        assertEquals(delta, TardisPlotMigration.playerDelta(player, legacy.offset(5, 1, 8)));
        assertEquals(delta, TardisPlotMigration.playerDelta(player, legacy.offset(-27, 1, 36)));
        assertNull(TardisPlotMigration.playerDelta(player, legacy.offset(-28, 1, 0)), "outside the legacy window");
        assertNull(TardisPlotMigration.playerDelta(otherPlayer, legacy.offset(5, 1, 8)));

        TardisPlotMigration.playerHandled(player);
        restart(tempDir);
        assertFalse(TardisPlotMigration.isPlayerPending(player));
        assertNull(TardisPlotMigration.playerDelta(player, legacy.offset(5, 1, 8)));
    }

    // ---- helpers -------------------------------------------------------------------------------------------

    private void restart(Path dir) throws Exception {
        clearModelCache();
        TardisPlotSlots.clearUnsavedForTest();
        TardisPlotMigration.resetCacheForTest();
        TardisDataLoader.tardisSaveDirectory = dir;
    }

    private UUID writeLegacyModel(String id) throws IOException {
        UUID uuid = UUID.fromString(id);
        TardisDataModel model = new TardisDataModel();
        model.uuid = uuid;
        File file = new File(TardisDataLoader.tardisSaveDirectory.toFile(), uuid + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(model, writer);
        }
        return uuid;
    }

    private static int firstIndex(List<String> ops, String prefix) {
        for (int i = 0; i < ops.size(); i++) {
            if (ops.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    private static int lastIndex(List<String> ops, String prefix) {
        for (int i = ops.size() - 1; i >= 0; i--) {
            if (ops.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    private static void clearModelCache() throws Exception {
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        ((HashMap<?, ?>) field.get(null)).clear();
    }

    private static final class Crash extends RuntimeException {
    }

    /** Interiors are strings; capture reads {@link #legacy}, paste writes {@link #pasted}. Can crash before any op. */
    private static final class FakeWorld implements MigrationWorld {
        final Map<UUID, String> legacy;
        final Map<UUID, String> pasted = new HashMap<>();
        final List<String> ops = new ArrayList<>();
        final Set<UUID> players;
        int crashAt = -1;
        private int count;

        FakeWorld(Map<UUID, String> interiors, Set<UUID> players) {
            this.legacy = new HashMap<>(interiors);
            this.players = new LinkedHashSet<>(players);
        }

        private void step(String op) {
            if (count++ == crashAt) {
                throw new Crash();
            }
            ops.add(op);
        }

        @Override
        public InteriorSnapshot capture(UUID tardisId) {
            step("capture " + tardisId);
            String interior = legacy.get(tardisId);
            if (interior == null) {
                return new InteriorSnapshot(TardisPlotAllocator.legacyPlotOrigin(tardisId), List.of(), List.of());
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("interior", interior);
            return new InteriorSnapshot(TardisPlotAllocator.legacyPlotOrigin(tardisId), List.of(), List.of(tag));
        }

        @Override
        public void clearLegacy(UUID tardisId) {
            step("clear " + tardisId);
            legacy.remove(tardisId);
        }

        @Override
        public void prepareNew(UUID tardisId) {
            step("prepare " + tardisId);
            pasted.remove(tardisId);
        }

        @Override
        public void paste(UUID tardisId, InteriorSnapshot snapshot) {
            step("paste " + tardisId);
            pasted.put(tardisId, snapshot.entities().get(0).getStringOr("interior", "?"));
        }

        @Override
        public void flush() {
            step("flush");
        }

        @Override
        public Set<UUID> knownPlayers() {
            return players;
        }
    }

    /** In-memory snapshot files: they survive a simulated crash, like files on disk. */
    private static final class FakeStore implements MigrationWorld.SnapshotStore {
        final Map<UUID, InteriorSnapshot> files = new HashMap<>();

        @Override
        public void write(UUID tardisId, InteriorSnapshot snapshot) {
            files.put(tardisId, snapshot);
        }

        @Override
        public Optional<InteriorSnapshot> read(UUID tardisId) {
            return Optional.ofNullable(files.get(tardisId));
        }

        @Override
        public void delete(UUID tardisId) {
            files.remove(tardisId);
        }
    }
}
