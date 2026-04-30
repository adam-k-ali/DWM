package com.adamkali.dwm.tardis.data;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TardisDataLoaderTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        TardisDataLoader.tardisSaveDirectory = tempDir;
        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        HashMap<?, ?> cache = (HashMap<?, ?>) field.get(null);
        cache.clear();
    }

    @AfterEach
    void tearDown() {
        TardisDataLoader.tardisSaveDirectory = null;
    }

    @Test
    void save_CreatesFileWithCorrectContent() throws IOException {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        TardisDataModel testModel = TardisDataLoader.create();
        testModel.uuid = testUuid;
        testModel.markDirty();

        // Act
        TardisDataLoader.save();

        // Assert
        File savedFile = new File(tempDir.toFile(), testUuid + ".json");
        assertTrue(savedFile.exists(), "File should exist after saving");
        assertTrue(savedFile.length() > 0, "File should not be empty");

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(savedFile)) {
            TardisDataModel loadedModel = gson.fromJson(reader, TardisDataModel.class);
            assertEquals(loadedModel, testModel, "File should contain correct data");
        }

    }

    @Test
    void save_CreatesDirectoryIfNotExists() {
        // Arrange
        Path saveDirPath = tempDir.resolve("nested/directory");
        File saveDir = saveDirPath.toFile();
        TardisDataLoader.tardisSaveDirectory = saveDirPath;

        TardisDataModel testModel = TardisDataLoader.create();
        testModel.uuid = UUID.randomUUID();
        testModel.markDirty();

        // Act
        TardisDataLoader.save();

        // Assert
        assertTrue(saveDir.exists(), "Directory should be created");
        assertTrue(saveDir.isDirectory(), "Should be a directory");
    }

    @Test
    void get_ReturnsNullForNonexistentFile() {
        // Arrange
        UUID nonexistentUuid = UUID.randomUUID();

        // Act
        TardisDataModel result = TardisDataLoader.get(nonexistentUuid);

        // Assert
        assertNull(result, "Should return null for nonexistent file");
    }

    @Test
    void get_LoadsExistingFile() {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        TardisDataModel originalModel = TardisDataLoader.create();
        originalModel.uuid = testUuid;
        originalModel.markDirty();
        TardisDataLoader.save();

        // Act
        TardisDataModel loadedModel = TardisDataLoader.get(testUuid);

        // Assert
        assertNotNull(loadedModel, "Loaded model should not be null");
        assertEquals(testUuid, loadedModel.uuid, "UUIDs should match");
        assertFalse(loadedModel.needsSaving(), "Unmodified model should not need saving");
    }

    @Test
    void saveAndGet_MaintainsDataIntegrity() {
        // Arrange
        TardisDataModel originalModel = TardisDataLoader.create();
        UUID testUuid = originalModel.uuid;
        originalModel.markDirty();

        // Act
        TardisDataLoader.save();
        TardisDataModel loadedModel = TardisDataLoader.get(testUuid);

        // Assert
        assertNotNull(loadedModel, "Loaded model should not be null");
        assertEquals(originalModel.uuid, loadedModel.uuid, "UUIDs should match");
    }

    @Test
    void save_OnlyPersistsDirtyModels() {
        TardisDataModel dirtyModel = TardisDataLoader.create();
        dirtyModel.markDirty();
        UUID dirtyId = dirtyModel.uuid;

        TardisDataModel cleanModel = TardisDataLoader.create();
        UUID cleanId = cleanModel.uuid;

        TardisDataLoader.save();

        File dirtyFile = new File(tempDir.toFile(), dirtyId + ".json");
        File cleanFile = new File(tempDir.toFile(), cleanId + ".json");

        assertTrue(dirtyFile.exists(), "Dirty model should be saved");
        assertFalse(cleanFile.exists(), "Clean model should not be saved");
    }

    @Test
    void get_WithCorruptedJson_ShouldFailSafe() throws IOException {
        UUID testUuid = UUID.randomUUID();
        File corruptedFile = new File(tempDir.toFile(), testUuid + ".json");
        try (FileWriter writer = new FileWriter(corruptedFile)) {
            writer.write("{invalid json");
        }

        TardisDataModel loadedModel = TardisDataLoader.get(testUuid);

        assertNull(loadedModel, "Corrupted json should be treated as missing model");
    }


}
