package com.adamkali.dwm.tardis.data;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.logic.CircuitFittedLogic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public class TardisDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Path tardisSaveDirectory;

    private static final HashMap<UUID, TardisDataModel> tardisData = new HashMap<>();

    private static File getTardisDataDirectory(boolean createIfMissing) throws RuntimeException {
        if (tardisSaveDirectory == null) {
            throw new RuntimeException("Tardis save directory has not been set");
        }

        File directory = new File(tardisSaveDirectory.toUri());
        if (createIfMissing && !directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create tardis data directory");
            }
        }
        return directory;
    }

    private static File getTardisDataFile(@NotNull UUID uuid, boolean createDirectoryIfMissing) {
        File directory = getTardisDataDirectory(createDirectoryIfMissing);
        return new File(directory, uuid + ".json");
    }

    private static TardisDataModel load(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        // Remote clients never run SERVER_STARTED, so the save directory stays unset.
        // Treat that as a cache-only mode instead of crashing during block-entity ticks.
        if (tardisSaveDirectory == null) {
            return null;
        }

        File file = TardisDataLoader.getTardisDataFile(uuid, false);
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            TardisDataModel model = GSON.fromJson(reader, TardisDataModel.class);
            if (model == null) {
                return null;
            }
            tardisData.put(uuid, model);
            return model;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Returns the TardisDataModel from cache, or loads it from file if not already loaded.
     * Returns null if the UUID is null, or if the TardisDataModel doesn't exist.
     *
     * @param uuid
     * @return The TardisDataModel, or null.
     */
    public static @Nullable TardisDataModel get(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }

        if (tardisData.containsKey(uuid)) {
            return tardisData.get(uuid);
        }

        return TardisDataLoader.load(uuid);
    }

    /**
     * Seeds or updates an ephemeral client-side model from S2C portal shell meta.
     * No-ops when the save directory is set (integrated server owns the shared cache).
     */
    public static void applyClientShell(
            @NotNull UUID uuid,
            @NotNull TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen
    ) {
        if (tardisSaveDirectory != null) {
            return;
        }
        TardisDataModel model = tardisData.get(uuid);
        if (model == null) {
            model = new TardisDataModel();
            model.uuid = uuid;
            tardisData.put(uuid, model);
        }
        model.variant = variant;
        model.doorState.isOpen = isOpen;
        model.doorState.doorSwing = doorSwing;
    }

    /**
     * Drops all cached models. Used when a remote client disconnects so the next
     * session does not reuse another world's ephemeral shell state.
     * No-ops when the save directory is set so integrated/server persistence is preserved.
     */
    public static void clearCache() {
        if (tardisSaveDirectory != null) {
            return;
        }
        tardisData.clear();
    }

    private static void save(TardisDataModel dataModel) throws IOException {
        File file = TardisDataLoader.getTardisDataFile(dataModel.uuid, true);

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(dataModel, writer);
        }
    }

    public static void save() {
        for (TardisDataModel model : tardisData.values()) {
            if (model.needsSaving()) {
                try {
                    TardisDataLoader.save(model);
                } catch (IOException e) {
                    System.err.println("Failed to save tardis data for " + model.uuid + ": " + e.getMessage());
                }
            }
        }
    }

    public static TardisDataModel create() {
        TardisDataModel model = new TardisDataModel();
        tardisData.put(model.uuid, model);

        return model;
    }

    /**
     * Creates a found Type 40 profile: broken circuits, stabilisers off.
     * Used when a worldgen TARDIS exterior first assigns its UUID.
     */
    public static TardisDataModel createFoundUnfinished() {
        TardisDataModel model = new TardisDataModel();
        CircuitFittedLogic.applyFoundUnfinished(model);
        tardisData.put(model.uuid, model);
        return model;
    }

    /**
     * Returns the existing model for {@code uuid}, or creates and caches a new one with that id.
     */
    public static TardisDataModel getOrCreate(@NotNull UUID uuid) {
        TardisDataModel existing = get(uuid);
        if (existing != null) {
            return existing;
        }
        TardisDataModel model = new TardisDataModel();
        model.uuid = uuid;
        tardisData.put(uuid, model);
        return model;
    }

    /**
     * Loads every {@code *.json} under the save directory into the cache (idempotent for already-cached ids).
     * No-ops when the save directory is unset.
     */
    public static void ensureAllLoaded() {
        if (tardisSaveDirectory == null) {
            return;
        }
        File directory;
        try {
            directory = getTardisDataDirectory(false);
        } catch (RuntimeException e) {
            return;
        }
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            String idPart = name.substring(0, name.length() - ".json".length());
            try {
                UUID uuid = UUID.fromString(idPart);
                if (!tardisData.containsKey(uuid)) {
                    load(uuid);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip non-UUID filenames.
            }
        }
    }

    /**
     * Returns the TARDIS owned by {@code ownerUuid}, if any. Scans disk so uncached models are included.
     */
    public static Optional<TardisDataModel> findOwnedBy(@Nullable UUID ownerUuid) {
        if (ownerUuid == null) {
            return Optional.empty();
        }
        ensureAllLoaded();
        for (TardisDataModel model : tardisData.values()) {
            if (Objects.equals(model.ownerUuid, ownerUuid)) {
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the TARDIS whose interior plot contains {@code pos}, if any. Scans disk so uncached
     * models are included. Does not use {@code BotiPlotIndex} (empty until an interior is entered).
     */
    public static Optional<TardisDataModel> findAtInteriorPos(@Nullable BlockPos pos) {
        if (pos == null) {
            return Optional.empty();
        }
        ensureAllLoaded();
        for (TardisDataModel model : tardisData.values()) {
            if (model.uuid == null) {
                continue;
            }
            BlockPos origin = TardisPlotAllocator.plotOrigin(model.uuid);
            int localX = pos.getX() - origin.getX();
            int localY = pos.getY() - origin.getY();
            int localZ = pos.getZ() - origin.getZ();
            if (localX >= 0 && localX < FirstDoctorConsoleRoomLayout.SIZE_X
                    && localY >= 0 && localY < FirstDoctorConsoleRoomLayout.SIZE_Y
                    && localZ >= 0 && localZ < FirstDoctorConsoleRoomLayout.SIZE_Z) {
                return Optional.of(model);
            }
        }
        return Optional.empty();
    }
}
