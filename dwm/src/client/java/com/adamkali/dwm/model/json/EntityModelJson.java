package com.adamkali.dwm.model.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Parses DWM entity-model JSON and bakes vanilla {@link LayerDefinition} meshes.
 */
public final class EntityModelJson {
    public static final Codec<EntityModelFile> CODEC = EntityModelFile.CODEC;

    private static final List<Path> CLASSPATH_FALLBACK_ROOTS = List.of(
            Path.of("src/client/resources"),
            Path.of("dwm/src/client/resources")
    );

    private EntityModelJson() {
    }

    public static EntityModelFile parse(JsonElement json) {
        return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    public static EntityModelFile parse(String json) {
        return parse(JsonParser.parseString(json));
    }

    /**
     * Walks {@code parent} references. Child {@code texture}, {@code layer}, and texture size
     * overlay the parent; non-empty {@code parts} replace the parent tree.
     */
    public static EntityModelFile resolveParents(
            Function<Identifier, EntityModelFile> loader,
            EntityModelFile file
    ) {
        return resolveParents(loader, file, new LinkedHashSet<>());
    }

    public static LayerDefinition toLayerDefinition(EntityModelFile file) {
        if (file.parent().isPresent()) {
            throw new IllegalArgumentException("parent must be resolved before baking");
        }
        int width = file.textureWidth().orElseThrow(
                () -> new IllegalArgumentException("texture_width is required"));
        int height = file.textureHeight().orElseThrow(
                () -> new IllegalArgumentException("texture_height is required"));
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (EntityModelPart part : file.parts()) {
            addPart(root, part);
        }
        return LayerDefinition.create(mesh, width, height);
    }

    /**
     * Loads {@code assets/<ns>/models/entity/<path>.json} from the classpath (mod jar or
     * {@code src/client/resources} when running unit tests).
     */
    public static LayerDefinition loadClasspath(Identifier modelId) {
        return toLayerDefinition(loadFile(modelId, EntityModelJson::readClasspath, new LinkedHashSet<>()));
    }

    /**
     * Loads from a resource pack tree so packs can override registered meshes.
     */
    public static LayerDefinition load(ResourceManager resources, Identifier modelId) {
        try {
            return toLayerDefinition(loadFile(
                    modelId,
                    id -> readResource(resources, toResourceId(id)),
                    new LinkedHashSet<>()
            ));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Failed to load entity model " + modelId + " (" + toResourceId(modelId) + "): " + e.getMessage(),
                    e
            );
        }
    }

    public static Identifier toResourceId(Identifier modelId) {
        String path = modelId.getPath();
        if (path.startsWith("entity/")) {
            return Identifier.fromNamespaceAndPath(modelId.getNamespace(), "models/" + path + ".json");
        }
        return Identifier.fromNamespaceAndPath(
                modelId.getNamespace(),
                "models/entity/" + path + ".json"
        );
    }

    private static EntityModelFile loadFile(
            Identifier modelId,
            Function<Identifier, EntityModelFile> loader,
            Set<Identifier> visiting
    ) {
        if (!visiting.add(modelId)) {
            throw new IllegalArgumentException("cyclic entity model parent: " + visiting + " -> " + modelId);
        }
        EntityModelFile file = loader.apply(modelId);
        return resolveParents(loader, file, visiting);
    }

    private static EntityModelFile resolveParents(
            Function<Identifier, EntityModelFile> loader,
            EntityModelFile file,
            Set<Identifier> visiting
    ) {
        Optional<Identifier> parentId = file.parent();
        if (parentId.isEmpty()) {
            return file;
        }
        EntityModelFile parent = loadFile(parentId.get(), loader, visiting);
        return overlay(parent, file);
    }

    static EntityModelFile overlay(EntityModelFile parent, EntityModelFile child) {
        return new EntityModelFile(
                Optional.empty(),
                child.layer().or(parent::layer),
                child.textureWidth().or(parent::textureWidth),
                child.textureHeight().or(parent::textureHeight),
                child.texture().or(parent::texture),
                child.parts().isEmpty() ? parent.parts() : child.parts()
        );
    }

    private static EntityModelFile readClasspath(Identifier modelId) {
        Identifier resourceId = toResourceId(modelId);
        String classpath = "assets/" + resourceId.getNamespace() + "/" + resourceId.getPath();
        InputStream fromClasspath = EntityModelJson.class.getClassLoader().getResourceAsStream(classpath);
        try (InputStream stream = fromClasspath != null ? fromClasspath : openFallbackFile(classpath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing entity model: " + resourceId);
            }
            return parseStream(stream, resourceId);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read entity model: " + resourceId, e);
        }
    }

    private static InputStream openFallbackFile(String classpath) {
        for (Path root : CLASSPATH_FALLBACK_ROOTS) {
            Path file = root.resolve(classpath);
            if (Files.isRegularFile(file)) {
                try {
                    return Files.newInputStream(file);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to read entity model: " + file, e);
                }
            }
        }
        return null;
    }

    private static EntityModelFile readResource(ResourceManager resources, Identifier resourceId) {
        Optional<Resource> resource = resources.getResource(resourceId);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Missing entity model: " + resourceId);
        }
        try (InputStream stream = resource.get().open()) {
            return parseStream(stream, resourceId);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read entity model: " + resourceId, e);
        }
    }

    private static EntityModelFile parseStream(InputStream stream, Identifier resourceId) {
        try {
            return parse(JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid entity model " + resourceId + ": " + e.getMessage(), e);
        }
    }

    private static void addPart(PartDefinition parent, EntityModelPart part) {
        CubeListBuilder cubes = CubeListBuilder.create();
        for (EntityModelCube cube : part.cubes()) {
            cubes.texOffs(cube.uv().u(), cube.uv().v());
            if (cube.mirror()) {
                cubes.mirror();
            }
            EntityModelVec3 origin = cube.origin();
            EntityModelVec3 size = cube.size();
            cubes.addBox(
                    origin.x(),
                    origin.y(),
                    origin.z(),
                    size.x(),
                    size.y(),
                    size.z(),
                    new CubeDeformation(cube.inflate())
            );
            if (cube.mirror()) {
                cubes.mirror(false);
            }
        }
        PartDefinition child = parent.addOrReplaceChild(part.name(), cubes, toPartPose(part));
        for (EntityModelPart nested : part.children()) {
            addPart(child, nested);
        }
    }

    static PartPose toPartPose(EntityModelPart part) {
        EntityModelVec3 pivot = part.pivot();
        EntityModelVec3 rotation = part.rotation();
        if (rotation.isZero()) {
            if (pivot.isZero()) {
                return PartPose.ZERO;
            }
            return PartPose.offset(pivot.x(), pivot.y(), pivot.z());
        }
        return PartPose.offsetAndRotation(
                pivot.x(),
                pivot.y(),
                pivot.z(),
                (float) Math.toRadians(rotation.x()),
                (float) Math.toRadians(rotation.y()),
                (float) Math.toRadians(rotation.z())
        );
    }
}
