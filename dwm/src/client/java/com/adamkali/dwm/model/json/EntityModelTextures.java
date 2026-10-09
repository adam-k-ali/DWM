package com.adamkali.dwm.model.json;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Atlas texture of each JSON-backed model layer, recorded when the layer's JSON is loaded so the
 * file's {@code texture} field is the single source of truth (resource packs can retarget it).
 */
public final class EntityModelTextures {
    private static final Map<Identifier, Identifier> TEXTURES = new ConcurrentHashMap<>();

    private EntityModelTextures() {
    }

    static void put(Identifier modelId, Identifier texture) {
        TEXTURES.put(modelId, texture);
    }

    public static Identifier get(ModelLayerLocation layer) {
        Identifier texture = TEXTURES.get(layer.model());
        if (texture == null) {
            throw new IllegalStateException("No texture recorded for entity model " + layer.model()
                    + "; the layer must be registered with JsonEntityModelLayers and its model reloaded");
        }
        return texture;
    }

    /** Resolves the texture declared by a loaded model file, failing when it declares none. */
    static Identifier requireTexture(Identifier modelId, EntityModelFile file) {
        return file.texture().orElseThrow(
                () -> new IllegalArgumentException("entity model " + modelId + " must declare texture"));
    }
}
