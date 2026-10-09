package com.adamkali.dwm.model.json;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.entity.BroakirModel;
import com.adamkali.dwm.model.entity.DalekLaserModel;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.model.entity.TimeLordModel;
import com.adamkali.dwm.model.tileentity.FifthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FirstDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.FourthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SecondDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SeventhDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.SixthDoctorTardisModel;
import com.adamkali.dwm.model.tileentity.StabilisersModel;
import com.adamkali.dwm.model.tileentity.TTCapsuleModel;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import com.adamkali.dwm.model.tileentity.ThirdDoctorTardisModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JSON {@code texture} field is the only source of a model's atlas texture.
 */
class EntityModelTexturesTest {
    private static final List<ModelLayerLocation> JSON_LAYERS = List.of(
            DalekLaserModel.LAYER_LOCATION,
            BroakirModel.LAYER_LOCATION,
            DalekModel.LAYER_LOCATION,
            FlutterwingModel.LAYER_LOCATION,
            TimeLordModel.LAYER_LOCATION,
            TardisGlobeModel.LAYER_LOCATION,
            StabilisersModel.LAYER_LOCATION,
            FirstDoctorTardisModel.LAYER_LOCATION,
            SecondDoctorTardisModel.LAYER_LOCATION,
            ThirdDoctorTardisModel.LAYER_LOCATION,
            FourthDoctorTardisModel.LAYER_LOCATION,
            FifthDoctorTardisModel.LAYER_LOCATION,
            SixthDoctorTardisModel.LAYER_LOCATION,
            SeventhDoctorTardisModel.LAYER_LOCATION,
            TTCapsuleModel.LAYER_LOCATION,
            TardisClassicInteriorDoorModel.LAYER_LOCATION
    );

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void everyRegisteredJsonLayerDeclaresAnExistingPng() {
        for (ModelLayerLocation layer : JSON_LAYERS) {
            Identifier texture = EntityModelJson.loadClasspathFile(layer.model()).texture()
                    .orElseThrow(() -> new AssertionError(layer.model() + " must declare texture"));
            assertTrue(texture.getPath().startsWith("textures/") && texture.getPath().endsWith(".png"),
                    layer.model() + " texture must be a textures/*.png path: " + texture);
            String classpath = "assets/" + texture.getNamespace() + "/" + texture.getPath();
            assertTrue(EntityModelTexturesTest.class.getClassLoader().getResource(classpath) != null,
                    layer.model() + " texture is missing from resources: " + classpath);
        }
    }

    @Test
    void requireTextureReturnsDeclaredTexture() {
        Identifier model = Identifier.parse("dwm:test");
        EntityModelFile file = EntityModelJson.parse("""
                {"texture_width":16,"texture_height":16,"texture":"dwm:textures/entity/x.png",
                 "parts":[{"name":"a"}]}""");
        assertEquals(Identifier.parse("dwm:textures/entity/x.png"), EntityModelTextures.requireTexture(model, file));
    }

    @Test
    void requireTextureFailsNamingModelWhenMissing() {
        Identifier model = Identifier.parse("dwm:test");
        EntityModelFile file = EntityModelJson.parse("""
                {"texture_width":16,"texture_height":16,"parts":[{"name":"a"}]}""");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> EntityModelTextures.requireTexture(model, file));
        assertTrue(error.getMessage().contains("dwm:test"));
    }

    @Test
    void getFailsForUnrecordedLayer() {
        ModelLayerLocation layer = new ModelLayerLocation(Identifier.parse("dwm:never_loaded"), "main");
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> EntityModelTextures.get(layer));
        assertTrue(error.getMessage().contains("dwm:never_loaded"));
    }

    @Test
    void getReturnsRecordedTexture() {
        ModelLayerLocation layer = new ModelLayerLocation(Identifier.parse("dwm:recorded_test"), "main");
        Identifier texture = Identifier.parse("dwm:textures/entity/recorded.png");
        EntityModelTextures.put(layer.model(), texture);
        assertEquals(texture, EntityModelTextures.get(layer));
    }

    @Test
    void childInheritsParentTexture() {
        EntityModelFile parent = EntityModelJson.parse("""
                {"texture_width":8,"texture_height":8,"texture":"dwm:textures/entity/p.png","parts":[{"name":"a"}]}""");
        EntityModelFile child = EntityModelJson.parse("{\"parent\":\"dwm:p\"}");
        EntityModelFile resolved = EntityModelJson.resolveParents(id -> parent, child);
        assertEquals(Identifier.parse("dwm:textures/entity/p.png"), resolved.texture().orElseThrow());
    }
}
