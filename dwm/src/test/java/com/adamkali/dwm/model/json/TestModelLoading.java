package com.adamkali.dwm.model.json;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

/** Test stand-in for the client model reload: loads a layer from the classpath and records its animation. */
public final class TestModelLoading {
    private TestModelLoading() {
    }

    public static ModelPart bake(ModelLayerLocation layer) {
        EntityModelFile file = EntityModelJson.loadClasspathFile(layer.model());
        EntityModelAnimations.put(layer.model(), file.animation());
        return EntityModelJson.toLayerDefinition(file).bakeRoot();
    }
}
