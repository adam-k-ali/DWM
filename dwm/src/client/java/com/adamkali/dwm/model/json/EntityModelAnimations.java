package com.adamkali.dwm.model.json;

import com.adamkali.dwm.model.json.anim.EntityAnimation;
import net.minecraft.client.model.geom.ModelLayerLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

/**
 * The {@code animation} block of each JSON-backed model layer, recorded when the layer loads so a
 * resource pack override can change animation as well as mesh.
 */
public final class EntityModelAnimations {
    private static final Map<Identifier, EntityAnimation> ANIMATIONS = new ConcurrentHashMap<>();

    private EntityModelAnimations() {
    }

    static void put(Identifier modelId, EntityAnimation animation) {
        ANIMATIONS.put(modelId, animation);
    }

    /** Empty when the layer declares no animation (or has not been loaded). */
    public static EntityAnimation get(ModelLayerLocation layer) {
        return ANIMATIONS.getOrDefault(layer.model(), EntityAnimation.EMPTY);
    }
}
