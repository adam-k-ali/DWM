package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.Blueprint;
import com.adamkali.dwm.blueprint.model.CircleShape;
import com.adamkali.dwm.blueprint.model.PolygonShape;
import com.adamkali.dwm.blueprint.model.RectangleShape;
import com.google.gson.JsonParseException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintLoaderTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        BlueprintLoader.blueprintsDirectory = tempDir;
    }

    @AfterEach
    void tearDown() {
        BlueprintLoader.blueprintsDirectory = null;
    }

    @Test
    void parse_loadsAllShapeTypes() {
        String json = """
                {
                  "shapes": [
                    {
                      "type": "circle",
                      "origin": {"x": 0, "y": 0, "z": 0},
                      "block": {"id": "minecraft:stone"},
                      "filled": true,
                      "radius": 2
                    },
                    {
                      "type": "rectangle",
                      "from": {"x": 0, "y": 0, "z": 0},
                      "to": {"x": 1, "y": 1, "z": 1},
                      "block": {"id": "minecraft:dirt"},
                      "fill": false
                    },
                    {
                      "type": "polygon",
                      "n_sides": 5,
                      "origin": {"x": 0, "y": 0, "z": 0},
                      "block": {"id": "minecraft:oak_planks"},
                      "fill": true,
                      "radius": 3
                    }
                  ]
                }
                """;
        Blueprint blueprint = BlueprintLoader.parse(json);
        assertEquals(3, blueprint.shapes().size());
        assertInstanceOf(CircleShape.class, blueprint.shapes().get(0));
        assertInstanceOf(RectangleShape.class, blueprint.shapes().get(1));
        assertInstanceOf(PolygonShape.class, blueprint.shapes().get(2));
        assertEquals(5, ((PolygonShape) blueprint.shapes().get(2)).nSides());
    }

    @Test
    void load_readsNamedFileFromBlueprintsFolder() throws Exception {
        Files.writeString(tempDir.resolve("demo.json"), """
                {"shapes":[{"type":"circle","origin":{"x":0,"y":0,"z":0},"block":{"id":"minecraft:stone"},"filled":true,"radius":1}]}
                """);
        Blueprint blueprint = BlueprintLoader.load("demo");
        assertEquals(1, blueprint.shapes().size());
    }

    @Test
    void load_missingFile_throwsMissing() {
        BlueprintLoadException ex = assertThrows(BlueprintLoadException.class, () -> BlueprintLoader.load("nope"));
        assertEquals(BlueprintLoadException.Reason.MISSING, ex.reason());
    }

    @Test
    void load_unsafeName_rejected() {
        BlueprintLoadException ex = assertThrows(BlueprintLoadException.class, () -> BlueprintLoader.load("../escape"));
        assertEquals(BlueprintLoadException.Reason.UNSAFE_NAME, ex.reason());
        assertTrue(BlueprintLoader.resolveBlueprintPath("../escape").isEmpty());
        assertTrue(BlueprintLoader.resolveBlueprintPath("a/b").isEmpty());
    }

    @Test
    void load_invalidJson_throwsInvalid() throws Exception {
        Files.writeString(tempDir.resolve("bad.json"), """
                {"shapes":[{"type":"not_a_shape","origin":{"x":0,"y":0,"z":0},"block":{"id":"minecraft:stone"},"fill":true,"radius":1}]}
                """);
        BlueprintLoadException ex = assertThrows(BlueprintLoadException.class, () -> BlueprintLoader.load("bad"));
        assertEquals(BlueprintLoadException.Reason.INVALID, ex.reason());
    }

    @Test
    void parse_unknownType_throws() {
        assertThrows(JsonParseException.class, () -> BlueprintLoader.parse("""
                {"shapes":[{"type":"cube","origin":{"x":0,"y":0,"z":0},"block":{"id":"minecraft:stone"},"fill":true,"radius":1}]}
                """));
    }

    @Test
    void listBlueprintNames_returnsSortedStems() throws Exception {
        Files.writeString(tempDir.resolve("zeta.json"), "{\"shapes\":[]}");
        Files.writeString(tempDir.resolve("alpha.json"), "{\"shapes\":[]}");
        Files.writeString(tempDir.resolve("ignore.txt"), "nope");
        assertEquals(List.of("alpha", "zeta"), BlueprintLoader.listBlueprintNames());
    }

    @Test
    void resolveBlueprintPath_staysInsideFolder() {
        Optional<Path> path = BlueprintLoader.resolveBlueprintPath("safe_name");
        assertTrue(path.isPresent());
        assertTrue(path.get().startsWith(tempDir.toAbsolutePath().normalize()));
    }

    @Test
    void ensureDirectory_createsFolder() {
        Path nested = tempDir.resolve("nested/blueprints");
        BlueprintLoader.blueprintsDirectory = nested;
        BlueprintLoader.ensureDirectory();
        assertTrue(Files.isDirectory(nested));
    }
}
