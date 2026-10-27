package com.adamkali.dwm.entity.definition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityDefinitionInheritanceTest {
    private static final String BASE = """
            {
              "entity": "dwm:base",
              "attribute_base": "monster",
              "attributes": { "minecraft:max_health": 10.0, "minecraft:armor": 2.0 },
              "attribute_overrides": { "angry": { "minecraft:attack_damage": 3.0, "minecraft:armor": 1.0 } },
              "goals": [ { "type": "minecraft:float", "priority": 0 } ],
              "targets": [ { "type": "minecraft:hurt_by_target", "priority": 1 } ]
            }
            """;

    @Test
    void omittedFieldsAreInherited() {
        JsonObject child = resolve(BASE, """
                { "entity": "dwm:child", "parent": "dwm:base" }
                """).get(id("child"));
        assertEquals("monster", child.get("attribute_base").getAsString());
        assertEquals(1, child.getAsJsonArray("goals").size());
        assertEquals(1, child.getAsJsonArray("targets").size());
        assertEquals("dwm:child", child.get("entity").getAsString());
        assertFalse(child.has("parent"));
    }

    @Test
    void attributeMapsMergePerKey() {
        JsonObject child = resolve(BASE, """
                {
                  "entity": "dwm:child",
                  "parent": "dwm:base",
                  "attributes": { "minecraft:max_health": 40.0 },
                  "attribute_overrides": { "angry": { "minecraft:attack_damage": 9.0 }, "calm": { "minecraft:armor": 0.0 } }
                }
                """).get(id("child"));
        JsonObject attributes = child.getAsJsonObject("attributes");
        assertEquals(40.0, attributes.get("minecraft:max_health").getAsDouble());
        assertEquals(2.0, attributes.get("minecraft:armor").getAsDouble());
        JsonObject angry = child.getAsJsonObject("attribute_overrides").getAsJsonObject("angry");
        assertEquals(9.0, angry.get("minecraft:attack_damage").getAsDouble());
        assertEquals(1.0, angry.get("minecraft:armor").getAsDouble());
        assertEquals(0.0, child.getAsJsonObject("attribute_overrides").getAsJsonObject("calm")
                .get("minecraft:armor").getAsDouble());
    }

    @Test
    void listsAreReplacedWholesale() {
        JsonObject child = resolve(BASE, """
                {
                  "entity": "dwm:child",
                  "parent": "dwm:base",
                  "targets": [
                    { "type": "minecraft:owner_hurt_by_target", "priority": 1 },
                    { "type": "minecraft:owner_hurt_target", "priority": 2 }
                  ]
                }
                """).get(id("child"));
        assertEquals(2, child.getAsJsonArray("targets").size());
        assertEquals(1, child.getAsJsonArray("goals").size());
    }

    @Test
    void parentIsNotMutated() {
        Map<Identifier, JsonObject> resolved = resolve(BASE, """
                { "entity": "dwm:child", "parent": "dwm:base", "attributes": { "minecraft:max_health": 40.0 } }
                """);
        assertEquals(10.0, resolved.get(id("base")).getAsJsonObject("attributes")
                .get("minecraft:max_health").getAsDouble());
    }

    @Test
    void multiLevelChainsResolve() {
        JsonObject grandchild = resolve(BASE, """
                { "entity": "dwm:child", "parent": "dwm:base", "attributes": { "minecraft:armor": 5.0 } }
                """, """
                { "entity": "dwm:grandchild", "parent": "dwm:child", "attributes": { "minecraft:max_health": 1.0 } }
                """).get(id("grandchild"));
        JsonObject attributes = grandchild.getAsJsonObject("attributes");
        assertEquals(1.0, attributes.get("minecraft:max_health").getAsDouble());
        assertEquals(5.0, attributes.get("minecraft:armor").getAsDouble());
        assertEquals("monster", grandchild.get("attribute_base").getAsString());
    }

    @Test
    void unknownParentFails() {
        assertThrows(IllegalStateException.class, () -> resolve(BASE, """
                { "entity": "dwm:child", "parent": "dwm:missing" }
                """));
    }

    @Test
    void selfParentFails() {
        assertThrows(IllegalStateException.class, () -> resolve("""
                { "entity": "dwm:base", "parent": "dwm:base" }
                """));
    }

    @Test
    void cycleFails() {
        assertThrows(IllegalStateException.class, () -> resolve("""
                { "entity": "dwm:base", "parent": "dwm:child" }
                """, """
                { "entity": "dwm:child", "parent": "dwm:base" }
                """));
    }

    private static Map<Identifier, JsonObject> resolve(String... definitions) {
        Map<Identifier, JsonObject> raw = new LinkedHashMap<>();
        for (String text : definitions) {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            raw.put(Identifier.parse(json.get("entity").getAsString()), json);
        }
        return EntityDefinitions.resolveParents(raw);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }
}
