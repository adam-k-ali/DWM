package com.adamkali.dwm.tardis.interior.migration;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The world side of the plot migration, kept behind an interface so the phase and resume logic in
 * {@link TardisPlotMigration} is unit-tested with a fake. {@link ServerMigrationWorld} is the real one.
 */
public interface MigrationWorld {
    /** Captures {@code tardisId}'s legacy 64-block window. */
    InteriorSnapshot capture(UUID tardisId);

    /** Empties {@code tardisId}'s legacy window (blocks and non-player entities). Repeatable. */
    void clearLegacy(UUID tardisId);

    /** Empties {@code tardisId}'s new 10000-block window of leftovers ({@link TardisPlotPreparer}). Repeatable. */
    void prepareNew(UUID tardisId);

    /** Pastes {@code snapshot} into {@code tardisId}'s new plot. Repeatable: pasting twice gives the same result. */
    void paste(UUID tardisId, InteriorSnapshot snapshot);

    /** Saves and flushes every chunk, so what has been done so far is on disk before the next phase is recorded. */
    void flush();

    /** UUIDs of every player with saved data, for moving players saved inside a legacy window on their next join. */
    Set<UUID> knownPlayers();

    /** Persists snapshots between phases (files next to the migration state). */
    interface SnapshotStore {
        void write(UUID tardisId, InteriorSnapshot snapshot);

        Optional<InteriorSnapshot> read(UUID tardisId);

        void delete(UUID tardisId);
    }
}
