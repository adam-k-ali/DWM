package com.adamkali.dwm.tardis.data;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

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

    private static File getTardisDataFile(UUID uuid, boolean createDirectoryIfMissing) {
        File directory = getTardisDataDirectory(createDirectoryIfMissing);
        return new File(directory, uuid.toString() + ".json");
    }

    private static TardisDataModel load(UUID uuid) {
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
        } catch (IOException e) {
            return null;
        }
    }

    public static @Nullable TardisDataModel get(UUID uuid) {
        if (tardisData.containsKey(uuid)) {
            return tardisData.get(uuid);
        }

        return TardisDataLoader.load(uuid);
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
}
