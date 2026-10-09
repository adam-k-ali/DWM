package com.adamkali.dwm.model.json;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

/**
 * Registers a {@link ModelLayerLocation} whose mesh is loaded from
 * {@code assets/<ns>/models/entity/<path>.json}. A missing or malformed file fails the
 * model reload with an error naming the model; there is no built-in fallback.
 */
public final class JsonEntityModelLayers {
    private JsonEntityModelLayers() {
    }

    public static void register(ModelLayerLocation location) {
        ModelLayerRegistry.registerModelLayer(location, () -> load(location.model()));
    }

    static LayerDefinition load(Identifier modelId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getResourceManager() == null) {
            throw new IllegalStateException("Cannot load entity model " + modelId + ": no resource manager");
        }
        return EntityModelJson.load(minecraft.getResourceManager(), modelId);
    }
}
