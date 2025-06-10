package com.adamkali.dwm.tardis.data;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TardisDataLoaderTest {

    @TempDir
    Path tempDir;

    private File originalSaveDir;
    private static final String TEST_FIELD_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        originalSaveDir = new File(TardisDataLoader.tardisSaveDirectory);
        TardisDataLoader.tardisSaveDirectory = tempDir.toString();
    }

    @AfterEach
    void tearDown() {
        TardisDataLoader.tardisSaveDirectory = originalSaveDir.getPath();
    }

    @Test
    void save_CreatesFileWithCorrectContent() throws IOException {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        TardisDataModel testModel = new TardisDataModel();
        testModel.uuid = testUuid;

        // Act
        TardisDataLoader.save(testModel);

        // Assert
        File savedFile = new File(tempDir.toFile(), testUuid.toString() + ".json");
        assertTrue(savedFile.exists(), "File should exist after saving");
        assertTrue(savedFile.length() > 0, "File should not be empty");
    }

    @Test
    void save_CreatesDirectoryIfNotExists() throws IOException {
        // Arrange
        File saveDir = tempDir.resolve("nested/directory").toFile();
        TardisDataLoader.tardisSaveDirectory = saveDir.getPath();

        TardisDataModel testModel = new TardisDataModel();
        testModel.uuid = UUID.randomUUID();

        // Act
        TardisDataLoader.save(testModel);

        // Assert
        assertTrue(saveDir.exists(), "Directory should be created");
        assertTrue(saveDir.isDirectory(), "Should be a directory");
    }

    @Test
    void get_ReturnsNullForNonexistentFile() throws IOException {
        // Arrange
        UUID nonexistentUuid = UUID.randomUUID();

        // Act
        TardisDataModel result = TardisDataLoader.get(nonexistentUuid);

        // Assert
        assertNull(result, "Should return null for nonexistent file");
    }

    @Test
    void get_LoadsExistingFile() throws IOException {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        TardisDataModel originalModel = new TardisDataModel();
        originalModel.uuid = testUuid;
        TardisDataLoader.save(originalModel);

        // Act
        TardisDataModel loadedModel = TardisDataLoader.get(testUuid);

        // Assert
        assertNotNull(loadedModel, "Loaded model should not be null");
        assertEquals(testUuid, loadedModel.uuid, "UUIDs should match");
    }

    @Test
    void saveAndGet_MaintainsDataIntegrity() throws IOException {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        TardisDataModel originalModel = new TardisDataModel();
        originalModel.uuid = testUuid;

        // Act
        TardisDataLoader.save(originalModel);
        TardisDataModel loadedModel = TardisDataLoader.get(testUuid);

        // Assert
        assertNotNull(loadedModel, "Loaded model should not be null");
        assertEquals(originalModel.uuid, loadedModel.uuid, "UUIDs should match");
    }


}
