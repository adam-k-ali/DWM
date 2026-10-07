package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.Blueprint;
import com.adamkali.dwm.blueprint.model.BlueprintShape;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

/**
 * Loads blueprint JSON files from the world save {@code blueprints/} folder.
 */
public final class BlueprintLoader {
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlueprintShape.class, new BlueprintShapeDeserializer())
            .create();

    /** Set on {@code SERVER_STARTED} to {@code <world>/blueprints}. */
    public static @Nullable Path blueprintsDirectory;

    private BlueprintLoader() {
    }

    public static boolean isSafeName(String name) {
        return name != null && SAFE_NAME.matcher(name).matches();
    }

    /**
     * Ensures the blueprints directory exists. No-op if the directory path is unset.
     */
    public static void ensureDirectory() {
        if (blueprintsDirectory == null) {
            return;
        }
        try {
            Files.createDirectories(blueprintsDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create blueprints directory", e);
        }
    }

    /**
     * Lists blueprint name stems ({@code *.json} without extension) in the blueprints folder.
     */
    public static List<String> listBlueprintNames() {
        if (blueprintsDirectory == null || !Files.isDirectory(blueprintsDirectory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(blueprintsDirectory)) {
            List<String> names = new ArrayList<>();
            stream.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - ".json".length()))
                    .filter(BlueprintLoader::isSafeName)
                    .sorted()
                    .forEach(names::add);
            return names;
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Resolves {@code blueprints/<name>.json} with path-traversal protection.
     *
     * @return empty if the name is unsafe or the resolved path escapes the blueprints folder
     */
    public static Optional<Path> resolveBlueprintPath(String name) {
        if (!isSafeName(name) || blueprintsDirectory == null) {
            return Optional.empty();
        }
        Path root = blueprintsDirectory.toAbsolutePath().normalize();
        Path resolved = root.resolve(name + ".json").normalize();
        if (!resolved.startsWith(root)) {
            return Optional.empty();
        }
        return Optional.of(resolved);
    }

    /**
     * Loads and parses a blueprint by name from the world blueprints folder.
     *
     * @throws BlueprintLoadException if the file is missing, unreadable, or invalid JSON
     */
    public static Blueprint load(String name) throws BlueprintLoadException {
        if (!isSafeName(name)) {
            throw new BlueprintLoadException(BlueprintLoadException.Reason.UNSAFE_NAME, name);
        }
        Optional<Path> path = resolveBlueprintPath(name);
        if (path.isEmpty()) {
            throw new BlueprintLoadException(BlueprintLoadException.Reason.UNSAFE_NAME, name);
        }
        if (!Files.isRegularFile(path.get())) {
            throw new BlueprintLoadException(BlueprintLoadException.Reason.MISSING, name);
        }
        try (Reader reader = Files.newBufferedReader(path.get())) {
            return parse(reader);
        } catch (IOException e) {
            throw new BlueprintLoadException(BlueprintLoadException.Reason.IO_ERROR, name, e);
        } catch (JsonParseException e) {
            throw new BlueprintLoadException(BlueprintLoadException.Reason.INVALID, name, e);
        }
    }

    /**
     * Parses blueprint JSON from a reader (for tests and in-memory fixtures).
     */
    public static Blueprint parse(Reader reader) throws JsonParseException {
        Blueprint blueprint = GSON.fromJson(reader, Blueprint.class);
        if (blueprint == null || blueprint.shapes() == null) {
            throw new JsonParseException("Blueprint must contain a shapes array");
        }
        return blueprint;
    }

    /**
     * Parses blueprint JSON from a string (for tests).
     */
    public static Blueprint parse(String json) throws JsonParseException {
        return parse(new java.io.StringReader(json));
    }
}
