package com.adamkali.dwm.data.definition;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionFilesTest {
    @Test
    void resolvesProductionFoldersFromTheClasspathNotTheWorkingDirectory() {
        Path dir = DefinitionFiles.resolveDir(DefinitionFiles.folder("block"));
        assertTrue(Files.isDirectory(dir));
        assertTrue(dir.isAbsolute(), "classpath resolution yields an absolute path, not a cwd-relative one: " + dir);
        assertTrue(Files.isRegularFile(dir.resolve("gallifrey_stone.json")));
    }

    @Test
    void readAllLoadsEveryFileKeyedByStem() {
        Map<Identifier, JsonObject> raw = DefinitionFiles.readAll("item");
        assertTrue(raw.containsKey(Identifier.fromNamespaceAndPath("dwm", "steel_sword")));
        raw.forEach((id, json) -> assertEquals(id.toString(), json.get("item").getAsString()));
    }

    @Test
    void missingFolderFailsFastInsteadOfFallingBack() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> DefinitionFiles.resolveDir("data/dwm/nonexistent/definition"));
        assertTrue(e.getMessage().contains("nonexistent"), e.getMessage());
        assertFalse(e.getMessage().contains("src/main"), "must not mention a cwd-relative fallback");
    }
}
