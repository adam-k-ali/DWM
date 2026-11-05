package com.adamkali.dwm.data.definition;

import com.adamkali.dwm.DWMReference;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Shared plumbing for jar-bundled JSON definition folders ({@code data/dwm/<kind>/definition/*.json}):
 * file discovery, raw parsing and {@code "parent"} flattening.
 *
 * <p>Definitions load at mod init, before registry freeze, so they are read straight from the mod jar
 * (not from the resource manager) and {@code /reload} does not apply.
 */
public final class DefinitionFiles {
    /** Key marking a definition as a template: it can be a {@code parent} but is never registered. */
    public static final String ABSTRACT = "abstract";
    public static final String PARENT = "parent";

    private static final Logger LOGGER = LogUtils.getLogger();

    private DefinitionFiles() {
    }

    /** Resource-root relative folder, e.g. {@code data/dwm/block/definition}. */
    public static String folder(String kind) {
        return "data/" + DWMReference.MOD_ID + "/" + kind + "/definition";
    }

    /**
     * Reads every {@code *.json} file in the definition folder, keyed by {@code dwm:<file stem>}.
     */
    public static Map<Identifier, JsonObject> readAll(String kind) {
        String folder = folder(kind);
        Path dir = resolveDir(folder);
        List<Path> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream.filter(file -> file.getFileName().toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list " + kind + " definitions in " + dir, e);
        }
        Map<Identifier, JsonObject> raw = new LinkedHashMap<>();
        for (Path path : files) {
            String name = path.getFileName().toString();
            Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, name.substring(0, name.length() - ".json".length()));
            try {
                raw.put(id, JsonParser.parseString(Files.readString(path)).getAsJsonObject());
            } catch (RuntimeException | IOException e) {
                throw new IllegalStateException("Failed to read " + kind + " definition " + path, e);
            }
        }
        return raw;
    }

    /**
     * Flattens {@code "parent"} references: anything a child omits comes from its parent.
     * {@code merge_keys} (objects such as {@code properties}) merge per key with the child winning; every other
     * field is replaced wholesale. {@code abstract} is never inherited. The result has no {@code parent} field.
     */
    public static Map<Identifier, JsonObject> resolveParents(Map<Identifier, JsonObject> definitions, Set<String> mergeKeys) {
        Map<Identifier, JsonObject> resolved = new LinkedHashMap<>();
        for (Identifier id : definitions.keySet()) {
            resolve(id, definitions, mergeKeys, resolved, new LinkedHashSet<>());
        }
        return resolved;
    }

    private static JsonObject resolve(
            Identifier id,
            Map<Identifier, JsonObject> definitions,
            Set<String> mergeKeys,
            Map<Identifier, JsonObject> resolved,
            Set<Identifier> visiting
    ) {
        JsonObject done = resolved.get(id);
        if (done != null) {
            return done;
        }
        if (!visiting.add(id)) {
            throw new IllegalStateException("Definition parent cycle: " + visiting + " -> " + id);
        }
        JsonObject child = definitions.get(id);
        JsonObject result;
        if (!child.has(PARENT)) {
            result = child.deepCopy();
        } else {
            String parentName = child.get(PARENT).getAsString();
            Identifier parentId = Identifier.tryParse(parentName);
            if (parentId == null || !definitions.containsKey(parentId)) {
                throw new IllegalStateException("Definition " + id + " has unknown parent " + parentName);
            }
            result = resolve(parentId, definitions, mergeKeys, resolved, visiting).deepCopy();
            result.remove(ABSTRACT);
            for (Map.Entry<String, JsonElement> field : child.entrySet()) {
                String key = field.getKey();
                if (key.equals(PARENT)) {
                    continue;
                }
                JsonElement value = field.getValue();
                if (mergeKeys.contains(key) && value.isJsonObject() && result.has(key) && result.get(key).isJsonObject()) {
                    value.getAsJsonObject().entrySet()
                            .forEach(entry -> result.getAsJsonObject(key).add(entry.getKey(), entry.getValue().deepCopy()));
                } else {
                    result.add(key, value.deepCopy());
                }
            }
        }
        visiting.remove(id);
        resolved.put(id, result);
        return result;
    }

    public static boolean isAbstract(JsonObject json) {
        return json.has(ABSTRACT) && json.get(ABSTRACT).getAsBoolean();
    }

    private static Path resolveDir(String folder) {
        try {
            Optional<Path> packed = FabricLoader.getInstance()
                    .getModContainer(DWMReference.MOD_ID)
                    .flatMap(container -> container.findPath(folder));
            if (packed.isPresent() && Files.isDirectory(packed.get())) {
                return packed.get();
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Fabric mod container unavailable for {}: {}", folder, e.toString());
        }
        Path fallback = Path.of("src/main/resources", folder);
        if (Files.isDirectory(fallback)) {
            return fallback;
        }
        throw new IllegalStateException("Cannot locate definition directory " + folder);
    }
}
