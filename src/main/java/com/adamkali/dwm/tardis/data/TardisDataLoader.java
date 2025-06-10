package com.adamkali.dwm.tardis.data;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

public class TardisDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Path tardisSaveDirectory;

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

    public static TardisDataModel get(UUID uuid) {
        File file = TardisDataLoader.getTardisDataFile(uuid, false);
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, TardisDataModel.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static void save(TardisDataModel dataModel) throws IOException {
        File file = TardisDataLoader.getTardisDataFile(dataModel.uuid, true);

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(dataModel, writer);
        }
    }
}
