package com.adamkali.dwm;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Walks block/item model JSON files and reports concrete {@code dwm:} texture
 * references that do not resolve to a non-empty PNG under the client assets tree.
 */
public final class ModelTextureValidationHelpers {
    private static final String MOD_NAMESPACE = DWMReference.MOD_ID + ":";

    public record MissingTexture(String textureId, Path expectedPath, Set<Path> referencingModels) {
    }

    private ModelTextureValidationHelpers() {
    }

    /**
     * Collects missing {@code dwm:} textures referenced by model {@code textures} maps.
     *
     * @param textureRoot directory containing {@code block/}, {@code item/}, etc. PNG trees
     *                    (typically {@code src/client/resources/assets/dwm/textures})
     * @param modelRoots  model trees to scan (client + generated)
     */
    public static List<MissingTexture> collectMissingModelTextures(Path textureRoot, Path... modelRoots)
            throws IOException {
        // textureId -> models that reference it
        Map<String, Set<Path>> references = new LinkedHashMap<>();

        for (Path modelRoot : modelRoots) {
            if (!Files.isDirectory(modelRoot)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(modelRoot)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))) {
                for (Path modelPath : (Iterable<Path>) paths::iterator) {
                    collectFromModel(modelPath, references);
                }
            }
        }

        List<MissingTexture> missing = new ArrayList<>();
        for (Map.Entry<String, Set<Path>> entry : references.entrySet()) {
            String textureId = entry.getKey();
            Path expected = toTexturePath(textureRoot, textureId);
            if (!isPresentNonEmpty(expected)) {
                missing.add(new MissingTexture(textureId, expected, entry.getValue()));
            }
        }
        return missing;
    }

    public static String formatMissingReport(List<MissingTexture> missing) {
        StringBuilder sb = new StringBuilder();
        sb.append("Missing model-defined textures (").append(missing.size()).append("):\n");
        for (MissingTexture entry : missing) {
            sb.append("  ").append(entry.textureId())
                    .append(" -> ").append(entry.expectedPath())
                    .append("\n    referenced by:\n");
            for (Path model : entry.referencingModels()) {
                sb.append("      ").append(model).append('\n');
            }
        }
        return sb.toString();
    }

    private static void collectFromModel(Path modelPath, Map<String, Set<Path>> references) throws IOException {
        String content = Files.readString(modelPath);
        JSONObject json = new JSONObject(new JSONTokener(content));
        if (!json.has("textures")) {
            return;
        }
        Object texturesRaw = json.get("textures");
        if (!(texturesRaw instanceof JSONObject textures)) {
            return;
        }
        for (String key : textures.keySet()) {
            Object value = textures.get(key);
            if (!(value instanceof String textureId)) {
                continue;
            }
            if (!isConcreteDwmTexture(textureId)) {
                continue;
            }
            references.computeIfAbsent(textureId, ignored -> new LinkedHashSet<>()).add(modelPath);
        }
    }

    static boolean isConcreteDwmTexture(String textureId) {
        if (textureId.isEmpty() || textureId.startsWith("#")) {
            return false;
        }
        // Only mod textures are validated; minecraft: and un-namespaced IDs are skipped.
        return textureId.startsWith(MOD_NAMESPACE);
    }

    static Path toTexturePath(Path textureRoot, String textureId) {
        String path = textureId.startsWith(MOD_NAMESPACE)
                ? textureId.substring(MOD_NAMESPACE.length())
                : textureId;
        return textureRoot.resolve(path + ".png");
    }

    private static boolean isPresentNonEmpty(Path path) throws IOException {
        return Files.isRegularFile(path) && Files.size(path) > 0;
    }
}
