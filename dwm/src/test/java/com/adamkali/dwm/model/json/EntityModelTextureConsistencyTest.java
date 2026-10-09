package com.adamkali.dwm.model.json;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.entity.DalekLaserModel;
import com.adamkali.dwm.model.tileentity.StabilisersModel;
import com.adamkali.dwm.model.tileentity.FifthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FourthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SecondDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SeventhDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SixthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.TTCapsuleModel;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.model.tileentity.ThirdDoctorTardisModel;
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
        assertTextureMatches(FirstDoctorTardisModel.LAYER_LOCATION, FirstDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(SecondDoctorTardisModel.LAYER_LOCATION, SecondDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(ThirdDoctorTardisModel.LAYER_LOCATION, ThirdDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(FourthDoctorTardisModel.LAYER_LOCATION, FourthDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(FifthDoctorTardisModel.LAYER_LOCATION, FifthDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(SixthDoctorTardisModel.LAYER_LOCATION, SixthDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(SeventhDoctorTardisModel.LAYER_LOCATION, SeventhDoctorTardisModel.TEXTURE_LOCATION);
        assertTextureMatches(TTCapsuleModel.LAYER_LOCATION, TTCapsuleModel.TEXTURE_LOCATION);
        assertTextureMatches(TardisClassicInteriorDoorModel.LAYER_LOCATION, TardisClassicInteriorDoorModel.TEXTURE_LOCATION);
    }

    private static void assertTextureMatches(ModelLayerLocation layer, Identifier textureLocation) {
        Identifier jsonTexture = EntityModelJson.loadClasspathFile(layer.model())
                .texture()
                .orElseThrow(() -> new AssertionError(layer.model() + " must declare texture"));
        assertEquals(textureLocation, jsonTexture, "texture mismatch for " + layer.model());
    }
}
