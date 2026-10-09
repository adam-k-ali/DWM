package com.adamkali.dwm.model.json;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityModelJsonTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void parseRoundTripsMinimalMesh() {
        var json = JsonParser.parseString("""
                {
                  "texture_width": 8,
                  "texture_height": 8,
                  "parts": [
                    {
                      "name": "bolt",
                      "cubes": [
                        {"uv": [0, 0], "origin": [-4, -0.5, -0.5], "size": [8, 1, 1]}
                      ]
                    }
                  ]
                }
                """);
        EntityModelFile parsed = EntityModelJson.parse(json);
        assertEquals(8, parsed.textureWidth().orElseThrow());
        assertEquals("bolt", parsed.parts().getFirst().name());
        assertEquals(0.0F, parsed.parts().getFirst().cubes().getFirst().inflate());
        assertFalse(parsed.parts().getFirst().cubes().getFirst().mirror());

        var encoded = EntityModelFile.CODEC.encodeStart(JsonOps.INSTANCE, parsed).getOrThrow();
        EntityModelFile roundTripped = EntityModelJson.parse(encoded);
        assertEquals(parsed, roundTripped);
    }

    @Test
    void invalidUvLengthFailsParse() {
        var json = JsonParser.parseString("""
                {
                  "texture_width": 8,
                  "texture_height": 8,
                  "parts": [
                    {
                      "name": "bolt",
                      "cubes": [
                        {"uv": [0, 0, 0], "origin": [0, 0, 0], "size": [1, 1, 1]}
                      ]
                    }
                  ]
                }
                """);
        assertTrue(EntityModelFile.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    @Test
    void parentOverlaysTextureAndLayer() {
        Identifier parentId = Identifier.fromNamespaceAndPath("dwm", "console_selector");
        EntityModelFile parent = EntityModelJson.parse("""
                {
                  "layer": "main",
                  "texture_width": 64,
                  "texture_height": 64,
                  "texture": "dwm:textures/entity/console_selector.png",
                  "parts": [{"name": "console_selector"}]
                }
                """);
        EntityModelFile child = EntityModelJson.parse("""
                {
                  "parent": "dwm:console_selector",
                  "layer": "panel",
                  "texture": "dwm:textures/entity/waypoint_selector.png"
                }
                """);
        EntityModelFile resolved = EntityModelJson.resolveParents(
                id -> {
                    assertEquals(parentId, id);
                    return parent;
                },
                child
        );
        assertTrue(resolved.parent().isEmpty());
        assertEquals("panel", resolved.layerOrDefault());
        assertEquals(64, resolved.textureWidth().orElseThrow());
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "textures/entity/waypoint_selector.png"),
                resolved.texture().orElseThrow()
        );
        assertEquals("console_selector", resolved.parts().getFirst().name());
    }

    @Test
    void cyclicParentFails() {
        Identifier a = Identifier.fromNamespaceAndPath("dwm", "a");
        Identifier b = Identifier.fromNamespaceAndPath("dwm", "b");
        Map<Identifier, EntityModelFile> files = Map.of(
                a, EntityModelJson.parse("{\"parent\":\"dwm:b\"}"),
                b, EntityModelJson.parse("{\"parent\":\"dwm:a\"}")
        );
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> EntityModelJson.resolveParents(files::get, files.get(a))
        );
        assertTrue(thrown.getMessage().contains("cyclic"));
    }

    @Test
    void unresolvedParentCannotBake() {
        EntityModelFile child = EntityModelJson.parse("{\"parent\":\"dwm:console_selector\"}");
        assertThrows(IllegalArgumentException.class, () -> EntityModelJson.toLayerDefinition(child));
    }

    @Test
    void rotationDegreesConvertToRadians() {
        EntityModelFile file = EntityModelJson.parse("""
                {
                  "texture_width": 16,
                  "texture_height": 16,
                  "parts": [
                    {"name": "head", "rotation": [90, 0, -45]}
                  ]
                }
                """);
        ModelPart root = EntityModelJson.toLayerDefinition(file).bakeRoot();
        ModelPart head = root.getChild("head");
        assertEquals((float) Math.toRadians(90.0), head.xRot, 1.0e-5F);
        assertEquals(0.0F, head.yRot, 1.0e-5F);
        assertEquals((float) Math.toRadians(-45.0), head.zRot, 1.0e-5F);
    }

    @Test
    void loadClasspathBakesConvertedMeshes() {
        ModelPart laser = assertDoesNotThrow(
                () -> EntityModelJson.loadClasspath(Identifier.fromNamespaceAndPath("dwm", "dalek_laser"))
                        .bakeRoot()
        );
        assertTrue(laser.hasChild("bolt"));

        ModelPart globe = globeRoot();
        assertTrue(globe.hasChild("mesh"));
        assertTrue(globe.hasChild("Globe"));
        assertTrue(globe.getChild("Globe").hasChild("mesh_1"));
        assertEquals((float) Math.toRadians(-45.0), globe.getChild("arrow").zRot, 1.0e-4F);

        ModelPart stabilisers = EntityModelJson.loadClasspath(
                Identifier.fromNamespaceAndPath("dwm", "stabilisers")
        ).bakeRoot();
        assertTrue(stabilisers.hasChild("stable_adjust"));
        assertTrue(stabilisers.getChild("stable_adjust").hasChild("lever"));
        assertEquals(
                (float) Math.toRadians(60.0),
                stabilisers.getChild("stable_adjust").getChild("lever").xRot,
                1.0e-4F
        );
    }

    @Test
    void jsonEntityModelWrapsBakedRoot() {
        ModelPart root = EntityModelJson.loadClasspath(
                Identifier.fromNamespaceAndPath("dwm", "dalek_laser")
        ).bakeRoot();
        JsonEntityModel<EntityRenderState> model = new JsonEntityModel<>(root);
        assertDoesNotThrow(() -> model.setupAnim(new EntityRenderState()));
    }

    @Test
    void resourceIdUsesEntityFolder() {
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "models/entity/dalek_laser.json"),
                EntityModelJson.toResourceId(Identifier.fromNamespaceAndPath("dwm", "dalek_laser"))
        );
        assertEquals(
                Identifier.fromNamespaceAndPath("dwm", "models/entity/console_selector.json"),
                EntityModelJson.toResourceId(Identifier.fromNamespaceAndPath("dwm", "entity/console_selector"))
        );
    }

    @Test
    void missingModelFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EntityModelJson.loadClasspath(Identifier.fromNamespaceAndPath("dwm", "does_not_exist"))
        );
    }

    @Test
    void loadFromResourcesFailsClearlyOnMalformedJson() throws IOException {
        Identifier modelId = Identifier.fromNamespaceAndPath("dwm", "dalek_laser");
        ResourceManager resources = mock(ResourceManager.class);
        Resource resource = mock(Resource.class);
        when(resource.open()).thenReturn(
                new ByteArrayInputStream("{\"texture_width\": \"wide\"}".getBytes(StandardCharsets.UTF_8)));
        when(resources.getResource(EntityModelJson.toResourceId(modelId))).thenReturn(Optional.of(resource));

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> EntityModelJson.load(resources, modelId)
        );
        assertTrue(thrown.getMessage().contains("dwm:dalek_laser"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("models/entity/dalek_laser.json"), thrown.getMessage());
    }

    @Test
    void loadFromResourcesFailsClearlyWhenMissing() {
        Identifier modelId = Identifier.fromNamespaceAndPath("dwm", "dalek_laser");
        ResourceManager resources = mock(ResourceManager.class);
        when(resources.getResource(EntityModelJson.toResourceId(modelId))).thenReturn(Optional.empty());

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> EntityModelJson.load(resources, modelId)
        );
        assertTrue(thrown.getMessage().contains("dwm:dalek_laser"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Missing entity model"), thrown.getMessage());
    }

    private static ModelPart globeRoot() {
        return EntityModelJson.loadClasspath(Identifier.fromNamespaceAndPath("dwm", "tardis_globe")).bakeRoot();
    }
}
