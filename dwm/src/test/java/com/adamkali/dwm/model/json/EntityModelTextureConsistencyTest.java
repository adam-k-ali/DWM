package com.adamkali.dwm.model.json;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.entity.DalekLaserModel;
import com.adamkali.dwm.model.tileentity.StabilisersModel;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The renderer texture stays a Java constant; the JSON {@code texture} field must not drift from it.
 */
class EntityModelTextureConsistencyTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void jsonTextureMatchesJavaTextureLocation() {
        assertTextureMatches(DalekLaserModel.LAYER_LOCATION, DalekLaserModel.TEXTURE_LOCATION);
        assertTextureMatches(TardisGlobeModel.LAYER_LOCATION, TardisGlobeModel.TEXTURE_LOCATION);
        assertTextureMatches(StabilisersModel.LAYER_LOCATION, StabilisersModel.TEXTURE_LOCATION);
    }

    private static void assertTextureMatches(ModelLayerLocation layer, Identifier textureLocation) {
        Identifier jsonTexture = EntityModelJson.loadClasspathFile(layer.model())
                .texture()
                .orElseThrow(() -> new AssertionError(layer.model() + " must declare texture"));
        assertEquals(textureLocation, jsonTexture, "texture mismatch for " + layer.model());
    }
}
