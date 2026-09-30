package com.adamkali.dwm.model.json;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Registers a {@link ModelLayerLocation} whose mesh is loaded from
 * {@code assets/<ns>/models/entity/<path>.json}.
 */
public final class JsonEntityModelLayers {
    private JsonEntityModelLayers() {
    }

    public static void register(ModelLayerLocation location) {
        ModelLayerRegistry.registerModelLayer(location, () -> load(location.model()));
    }

    static LayerDefinition load(Identifier modelId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            ResourceManager resources = minecraft.getResourceManager();
            if (resources != null) {
                return EntityModelJson.load(resources, modelId);
            }
        }
        return EntityModelJson.loadClasspath(modelId);
    }
}
