package com.adamkali.dwm.model.json;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.entity.BroakirModel;
import com.adamkali.dwm.model.entity.DalekLaserModel;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.model.entity.DalekMutantModel;
import com.adamkali.dwm.model.entity.ParadigmDalekModel;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.model.entity.TimeLordModel;
import com.adamkali.dwm.model.tileentity.StabilisersModel;
import com.adamkali.dwm.model.tileentity.TardisClassicInteriorDoorModel;
import com.adamkali.dwm.model.tileentity.TardisGlobeModel;
import com.adamkali.dwm.model.tileentity.TardisModels;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The JSON {@code texture} field is the only source of a model's atlas texture.
 */
class EntityModelTexturesTest {
    private static final Map<String, ModelLayerLocation> JSON_LAYERS_BY_CLASS = Map.ofEntries(
            Map.entry("BroakirModel", BroakirModel.LAYER_LOCATION),
            Map.entry("DalekModel", DalekModel.LAYER_LOCATION),
            Map.entry("ParadigmDalekModel", ParadigmDalekModel.LAYER_LOCATION),
            Map.entry("DalekMutantModel", DalekMutantModel.LAYER_LOCATION),
            Map.entry("FlutterwingModel", FlutterwingModel.LAYER_LOCATION),
            Map.entry("TimeLordModel", TimeLordModel.LAYER_LOCATION),
            Map.entry("DalekLaserModel", DalekLaserModel.LAYER_LOCATION),
            Map.entry("TardisGlobeModel", TardisGlobeModel.LAYER_LOCATION),
            Map.entry("StabilisersModel", StabilisersModel.LAYER_LOCATION),
            Map.entry("TardisClassicInteriorDoorModel", TardisClassicInteriorDoorModel.LAYER_LOCATION)
    );
    private static final List<ModelLayerLocation> JSON_LAYERS = jsonLayers();

    /** Per-class layers plus the per-variant TARDIS layers registered in a loop via {@link TardisModels}. */
    private static List<ModelLayerLocation> jsonLayers() {
        List<ModelLayerLocation> layers = new ArrayList<>(JSON_LAYERS_BY_CLASS.values());
        for (TardisChameleonVariant variant : TardisChameleonVariant.values()) {
            layers.add(TardisModels.layer(variant));
        }
        return layers;
    }

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void clearRecorded() {
        EntityModelTextures.remove(Identifier.parse("dwm:recorded_test"));
    }

    @Test
    void jsonLayersMatchClientRegistrations() throws IOException {
        Pattern register = Pattern.compile("JsonEntityModelLayers\\.register\\((\\w+)\\.LAYER_LOCATION\\)");
        Set<String> registered = new HashSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/client/java"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher m = register.matcher(Files.readString(file));
                while (m.find()) {
                    registered.add(m.group(1));
                }
            }
        }
        assertEquals(registered, JSON_LAYERS_BY_CLASS.keySet(), "JSON_LAYERS must list every layer registered via JsonEntityModelLayers");
    }

    @Test
    void loadWithTextureReturnsDefinitionAndTexture() throws IOException {
        Identifier modelId = Identifier.parse("dwm:pack_model");
        ResourceManager resources = resourcesFor(modelId, """
                {"texture_width":16,"texture_height":16,"texture":"dwm:textures/entity/pack.png",
                 "parts":[{"name":"a"}]}""");
        EntityModelJson.LoadedModel loaded = EntityModelJson.loadWithTexture(resources, modelId);
        assertEquals(Identifier.parse("dwm:textures/entity/pack.png"), loaded.texture());
        assertTrue(loaded.definition() != null);
    }

    @Test
    void loadWithTextureFailsNamingModelWhenTextureMissing() throws IOException {
        Identifier modelId = Identifier.parse("dwm:pack_model");
        ResourceManager resources = resourcesFor(modelId, """
                {"texture_width":16,"texture_height":16,"parts":[{"name":"a"}]}""");
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> EntityModelJson.loadWithTexture(resources, modelId));
        assertTrue(error.getMessage().contains("dwm:pack_model"), error.getMessage());
        assertTrue(error.getMessage().contains("must declare texture"), error.getMessage());
    }

    @Test
    void removeDropsRecordedTexture() {
        ModelLayerLocation layer = new ModelLayerLocation(Identifier.parse("dwm:recorded_test"), "main");
        EntityModelTextures.put(layer.model(), Identifier.parse("dwm:textures/entity/recorded.png"));
        EntityModelTextures.remove(layer.model());
        assertThrows(IllegalStateException.class, () -> EntityModelTextures.get(layer));
    }

    private static ResourceManager resourcesFor(Identifier modelId, String json) throws IOException {
        ResourceManager resources = mock(ResourceManager.class);
        Resource resource = mock(Resource.class);
        when(resource.open()).thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(resources.getResource(EntityModelJson.toResourceId(modelId))).thenReturn(Optional.of(resource));
        return resources;
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
