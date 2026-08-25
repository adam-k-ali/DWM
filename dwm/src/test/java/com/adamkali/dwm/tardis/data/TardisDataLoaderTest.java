package com.adamkali.dwm.tardis.data;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
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
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;

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
        testModel.setChanged();

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
        testModel.setChanged();

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
    void get_ReturnsNullWhenSaveDirectoryUnset() {
        TardisDataLoader.tardisSaveDirectory = null;
        UUID uuid = UUID.randomUUID();

        assertNull(TardisDataLoader.get(uuid), "Remote clients must not crash when save dir is unset");
    }

    @Test
    void applyClientShell_SeedsCacheWhenSaveDirectoryUnset() {
        TardisDataLoader.tardisSaveDirectory = null;
        UUID uuid = UUID.randomUUID();

        TardisDataLoader.applyClientShell(uuid, TardisChameleonVariant.FIRST_DOCTOR_BOX, 0.5f, true);

        TardisDataModel model = TardisDataLoader.get(uuid);
        assertNotNull(model);
        assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, model.variant);
        assertTrue(model.doorState.isOpen);
        assertEquals(0.5f, model.doorState.doorSwing, 0.001f);
        assertFalse(model.needsSaving(), "Client shell seed must not mark dirty for persistence");
    }

    @Test
    void applyClientShell_NoOpsWhenSaveDirectorySet() {
        UUID uuid = UUID.randomUUID();

        TardisDataLoader.applyClientShell(uuid, TardisChameleonVariant.FIRST_DOCTOR_BOX, 1.0f, true);

        assertNull(TardisDataLoader.get(uuid), "Integrated/server cache must not be overwritten by client meta");
    }

    @Test
    void clearCache_RemovesCachedModels() {
        TardisDataLoader.tardisSaveDirectory = null;
        UUID uuid = UUID.randomUUID();
        TardisDataLoader.applyClientShell(uuid, TardisChameleonVariant.TT_CAPSULE, 0.0f, false);

        TardisDataLoader.clearCache();

        assertNull(TardisDataLoader.get(uuid));
    }

    @Test
    void clearCache_NoOpsWhenSaveDirectorySet() {
        TardisDataModel model = TardisDataLoader.create();
        UUID uuid = model.uuid;

        TardisDataLoader.clearCache();

        assertSame(model, TardisDataLoader.get(uuid), "Server/integrated cache must survive clearCache");
    }

    @Test
    void get_LoadsExistingFile() {
        // Arrange
        UUID testUuid = UUID.randomUUID();
        TardisDataModel originalModel = TardisDataLoader.create();
        originalModel.uuid = testUuid;
        originalModel.setChanged();
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
        originalModel.setChanged();

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
        dirtyModel.setChanged();
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

    @Test
    void findOwnedBy_ReturnsOwnedModelAfterCreateAndSetOwner() {
        UUID owner = UUID.randomUUID();
        TardisDataModel model = TardisDataLoader.create();
        model.setOwner(owner);

        assertTrue(TardisDataLoader.findOwnedBy(owner).isPresent());
        assertEquals(model.uuid, TardisDataLoader.findOwnedBy(owner).get().uuid);
        assertTrue(TardisDataLoader.findOwnedBy(UUID.randomUUID()).isEmpty());
        assertTrue(TardisDataLoader.findOwnedBy(null).isEmpty());
    }

    @Test
    void findOwnedBy_LoadsFromDiskWhenNotCached() throws Exception {
        UUID owner = UUID.randomUUID();
        TardisDataModel model = TardisDataLoader.create();
        model.setOwner(owner);
        model.setChanged();
        TardisDataLoader.save();

        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        HashMap<?, ?> cache = (HashMap<?, ?>) field.get(null);
        cache.clear();

        assertTrue(TardisDataLoader.findOwnedBy(owner).isPresent());
        assertEquals(model.uuid, TardisDataLoader.findOwnedBy(owner).get().uuid);
    }

    @Test
    void findAtInteriorPos_ReturnsModelInsidePlotFootprint() {
        UUID tardisId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        TardisDataModel model = TardisDataLoader.getOrCreate(tardisId);
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        BlockPos inside = origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);

        Optional<TardisDataModel> found = TardisDataLoader.findAtInteriorPos(inside);

        assertTrue(found.isPresent());
        assertEquals(model.uuid, found.get().uuid);
        assertTrue(TardisDataLoader.findAtInteriorPos(origin.offset(TardisPlotAllocator.PLOT_SPACING / 2, 0, 0)).isEmpty());
        assertTrue(TardisDataLoader.findAtInteriorPos(null).isEmpty());
    }

    @Test
    void findAtInteriorPos_LoadsFromDiskWhenNotCached() throws Exception {
        UUID tardisId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        TardisDataModel model = TardisDataLoader.getOrCreate(tardisId);
        model.setChanged();
        TardisDataLoader.save();

        Field field = TardisDataLoader.class.getDeclaredField("tardisData");
        field.setAccessible(true);
        HashMap<?, ?> cache = (HashMap<?, ?>) field.get(null);
        cache.clear();

        BlockPos inside = TardisPlotAllocator.plotOrigin(tardisId)
                .offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
        Optional<TardisDataModel> found = TardisDataLoader.findAtInteriorPos(inside);

        assertTrue(found.isPresent());
        assertEquals(tardisId, found.get().uuid);
    }
}
