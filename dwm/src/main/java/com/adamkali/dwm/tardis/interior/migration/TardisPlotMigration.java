package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.interior.TardisPlotSlots;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * One-off migration of interiors built on the legacy 64-block plots to the 10000-block plots, run on the server
 * thread when the world loads, before any player can be inside.
 *
 * <p>Every TARDIS whose data predates the new plots ({@link TardisDataModel#isLegacyPlot()}) has its whole legacy
 * window copied (every block with its block entity, every non-player entity with its UUID) to the same
 * anchor-relative place in its new plot, then the legacy window is emptied. ARS cell coordinates stay valid because
 * the ARS grid origin is anchor-relative.
 *
 * <p>The work runs in phases recorded in {@code plot_migration/state.json}:
 * <ol>
 *     <li>{@link Phase#SNAPSHOTTED}: every candidate is given its slot and captured to a snapshot file. All snapshots
 *     are taken before anything is cleared, since a new window can overlap another TARDIS's legacy window.</li>
 *     <li>{@link Phase#CLEARED}: every legacy window is emptied.</li>
 *     <li>{@link Phase#PASTED}: each new window is emptied of leftovers and its snapshot pasted; the model is marked
 *     current and prepared.</li>
 *     <li>{@link Phase#DONE}: snapshot files are deleted. The list of players to move on their next join stays.</li>
 * </ol>
 * A phase is only recorded after the world has been flushed to disk, and every step is repeatable, so a crash at any
 * point resumes from the last recorded phase without losing or duplicating anything.
 */
public final class TardisPlotMigration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String DIRECTORY = "plot_migration";
    static final String STATE_FILE = "state.json";

    public enum Phase {
        /** Nothing in progress. */
        NONE,
        SNAPSHOTTED,
        CLEARED,
        PASTED,
        DONE
    }

    /** The persisted migration state. */
    static final class State {
        Phase phase = Phase.NONE;
        List<UUID> tardisIds = new ArrayList<>();
        List<UUID> pendingPlayers = new ArrayList<>();
        /** Every TARDIS ever migrated, so players can be moved out of any migrated legacy window. */
        List<UUID> migrated = new ArrayList<>();

        Phase phase() {
            return phase == null ? Phase.NONE : phase;
        }

        List<UUID> tardisIds() {
            if (tardisIds == null) {
                tardisIds = new ArrayList<>();
            }
            return tardisIds;
        }

        List<UUID> pendingPlayers() {
            if (pendingPlayers == null) {
                pendingPlayers = new ArrayList<>();
            }
            return pendingPlayers;
        }

        List<UUID> migrated() {
            if (migrated == null) {
                migrated = new ArrayList<>();
            }
            return migrated;
        }
    }

    /** What one run did. */
    public record Report(int migrated, int emptyMarkedCurrent, Phase resumedFrom) {
        public static final Report NOTHING = new Report(0, 0, Phase.NONE);
    }

    private static @Nullable State cachedState;
    private static @Nullable Path cachedStateDirectory;

    private TardisPlotMigration() {
    }

    /** {@code <tardisSaveDirectory>/plot_migration}, or null when the save directory is unset. */
    public static @Nullable Path directory() {
        return TardisDataLoader.tardisSaveDirectory == null
                ? null
                : TardisDataLoader.tardisSaveDirectory.resolve(DIRECTORY);
    }

    /**
     * Runs (or resumes) the migration. Every legacy TARDIS model in {@link TardisDataLoader} is migrated; a world
     * with none returns {@link Report#NOTHING} without writing anything.
     */
    public static synchronized Report run(MigrationWorld world, MigrationWorld.SnapshotStore snapshots) {
        Path dir = directory();
        if (dir == null) {
            return Report.NOTHING;
        }
        State state = loadState(dir);
        Phase resumedFrom = state.phase();
        if (state.phase() == Phase.NONE || state.phase() == Phase.DONE) {
            TardisDataLoader.ensureAllLoaded();
            List<UUID> candidates = new ArrayList<>();
            for (TardisDataModel model : TardisDataLoader.cachedModels()) {
                if (model.uuid != null && model.isLegacyPlot()) {
                    candidates.add(model.uuid);
                }
            }
            if (candidates.isEmpty()) {
                return Report.NOTHING;
            }
            candidates.sort(Comparator.naturalOrder());
            Set<UUID> pending = new LinkedHashSet<>(state.pendingPlayers());
            pending.addAll(world.knownPlayers());
            state.phase = Phase.NONE;
            state.tardisIds = candidates;
            state.pendingPlayers = new ArrayList<>(pending);
            resumedFrom = Phase.NONE;
        }
        List<UUID> ids = List.copyOf(state.tardisIds());
        int empty = 0;

        if (state.phase() == Phase.NONE) {
            for (UUID id : ids) {
                // Slots in UUID order, before any capture, so allocation does not depend on what is found.
                TardisPlotSlots.slotOf(id);
            }
            for (UUID id : ids) {
                InteriorSnapshot snapshot = world.capture(id);
                if (snapshot.isEmpty()) {
                    snapshots.delete(id);
                } else {
                    snapshots.write(id, snapshot);
                }
                LOGGER.info("TARDIS {}: captured {} blocks ({} block entities) and {} entities from its legacy plot at {}",
                        id, snapshot.blocks().size(), snapshot.blockEntityCount(), snapshot.entities().size(),
                        TardisPlotAllocator.legacyPlotOrigin(id));
            }
            advance(dir, state, Phase.SNAPSHOTTED);
        }

        if (state.phase() == Phase.SNAPSHOTTED) {
            for (UUID id : ids) {
                world.clearLegacy(id);
            }
            world.flush();
            advance(dir, state, Phase.CLEARED);
        }

        if (state.phase() == Phase.CLEARED) {
            for (UUID id : ids) {
                Optional<InteriorSnapshot> snapshot = snapshots.read(id);
                TardisDataModel model = TardisDataLoader.get(id);
                if (snapshot.isPresent()) {
                    world.prepareNew(id);
                    world.paste(id, snapshot.get());
                    LOGGER.info("TARDIS {}: interior moved from {} to {}", id,
                            TardisPlotAllocator.legacyPlotOrigin(id), TardisPlotAllocator.plotOrigin(id));
                } else {
                    empty++;
                }
                if (model != null) {
                    model.plotVersion = TardisDataModel.CURRENT_PLOT_VERSION;
                    // An empty legacy plot leaves the new window to be prepared on first use.
                    model.plotPrepared = snapshot.isPresent() ? Boolean.TRUE : model.plotPrepared;
                    TardisDataLoader.persist(model);
                }
            }
            world.flush();
            for (UUID id : ids) {
                if (!state.migrated().contains(id)) {
                    state.migrated().add(id);
                }
            }
            advance(dir, state, Phase.PASTED);
        }

        if (state.phase() == Phase.PASTED) {
            for (UUID id : ids) {
                snapshots.delete(id);
            }
            advance(dir, state, Phase.DONE);
        }
        return new Report(ids.size() - empty, empty, resumedFrom);
    }

    /** The current phase on disk (for tests and logging). */
    public static synchronized Phase phase() {
        Path dir = directory();
        return dir == null ? Phase.NONE : loadState(dir).phase();
    }

    // ---- references outside the copied region --------------------------------------------------------------

    /** How far a migrated player at {@code pos} in {@code dwm:tardis} must move, or null when they stay put. */
    public static synchronized @Nullable Vec3i playerDelta(UUID playerId, BlockPos pos) {
        Path dir = directory();
        if (dir == null || playerId == null || pos == null) {
            return null;
        }
        State state = loadState(dir);
        if (!state.pendingPlayers().contains(playerId)) {
            return null;
        }
        return deltaForLegacyPosition(state, pos);
    }

    /** {@code playerId} has been handled; they are never moved again. */
    public static synchronized void playerHandled(UUID playerId) {
        Path dir = directory();
        if (dir == null || playerId == null) {
            return;
        }
        State state = loadState(dir);
        if (state.pendingPlayers().remove(playerId)) {
            saveState(dir, state);
        }
    }

    /** Whether {@code playerId} is still waiting to be checked on join. */
    public static synchronized boolean isPlayerPending(UUID playerId) {
        Path dir = directory();
        return dir != null && loadState(dir).pendingPlayers().contains(playerId);
    }

    /** The delta of the migrated TARDIS whose legacy window holds {@code pos}, or null. */
    static @Nullable Vec3i deltaForLegacyPosition(State state, BlockPos pos) {
        for (UUID id : state.migrated()) {
            BlockPos legacy = TardisPlotAllocator.legacyPlotOrigin(id);
            if (TardisPlotAllocator.isInsideLegacyWindow(pos, legacy)) {
                return TardisPlotAllocator.migrationDelta(id);
            }
        }
        return null;
    }

    // ---- state file ------------------------------------------------------------------------------------------

    private static void advance(Path dir, State state, Phase phase) {
        state.phase = phase;
        saveState(dir, state);
    }

    static State loadState(Path dir) {
        if (cachedState != null && dir.equals(cachedStateDirectory)) {
            return cachedState;
        }
        State state = new State();
        Path file = dir.resolve(STATE_FILE);
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                State loaded = GSON.fromJson(reader, State.class);
                if (loaded != null) {
                    state = loaded;
                }
            } catch (IOException | RuntimeException e) {
                throw new IllegalStateException("Unreadable TARDIS plot migration state " + file, e);
            }
        }
        cachedState = state;
        cachedStateDirectory = dir;
        return state;
    }

    private static void saveState(Path dir, State state) {
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(STATE_FILE);
            Path temp = dir.resolve(STATE_FILE + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp)) {
                GSON.toJson(state, writer);
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save TARDIS plot migration state in " + dir, e);
        }
        cachedState = state;
        cachedStateDirectory = dir;
    }

    /** Test helper: forgets the cached state so the next call re-reads the file. */
    public static synchronized void resetCacheForTest() {
        cachedState = null;
        cachedStateDirectory = null;
    }
}
