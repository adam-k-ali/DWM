package com.adamkali.dwm.entity.definition;

import com.adamkali.dwm.DWMReference;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Loads jar JSON entity definitions at init and applies attributes / AI goals.
 */
public final class EntityDefinitions {
    public static final String DEFAULT_OVERRIDE = "default";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFINITION_DIR = "data/" + DWMReference.MOD_ID + "/entity/definition";

    private static final Map<Identifier, EntityDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean initialized;

    private EntityDefinitions() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        DEFINITIONS.clear();
        Path dir = resolveDefinitionsDir();
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                loadFile(path);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list entity definitions in " + dir, e);
        }
        if (DEFINITIONS.isEmpty()) {
            throw new IllegalStateException("No entity definitions loaded from " + dir);
        }
        initialized = true;
        LOGGER.info("Loaded {} entity definition(s) from {}", DEFINITIONS.size(), dir);
    }

    public static Optional<EntityDefinition> get(Identifier entityId) {
        ensureInitialized();
        return Optional.ofNullable(DEFINITIONS.get(entityId));
    }

    public static AttributeSupplier.Builder createAttributes(Identifier entityId) {
        EntityDefinition definition = require(entityId);
        AttributeSupplier.Builder builder = definition.attributeBase().create();
        definition.attributes().forEach((attributeId, value) -> builder.add(resolveAttribute(attributeId, entityId), value));
        return builder;
    }

    public static void registerGoals(Mob mob, GoalSelector goalSelector, GoalSelector targetSelector) {
        Identifier entityId = idOf(mob);
        EntityDefinition definition = require(entityId);
        for (EntityGoalBinding binding : definition.goals()) {
            if (binding.goal().kind().selector() != EntityGoal.Selector.GOAL) {
                throw new IllegalStateException(entityId + " goals list contains target type "
                        + binding.goal().kind().getSerializedName());
            }
            goalSelector.addGoal(binding.priority(), binding.create(mob));
        }
        for (EntityGoalBinding binding : definition.targets()) {
            if (binding.goal().kind().selector() != EntityGoal.Selector.TARGET) {
                throw new IllegalStateException(entityId + " targets list contains goal type "
                        + binding.goal().kind().getSerializedName());
            }
            targetSelector.addGoal(binding.priority(), binding.create(mob));
        }
    }

    /**
     * Applies a named attribute overlay, or restores JSON base values when {@code setName} is {@code default}.
     */
    public static void applyAttributeOverride(Mob mob, String setName) {
        Identifier entityId = idOf(mob);
        EntityDefinition definition = require(entityId);
        Map<Identifier, Double> values;
        if (DEFAULT_OVERRIDE.equals(setName)) {
            values = definition.attributes();
        } else {
            values = definition.attributeOverrides().get(setName);
            if (values == null) {
                throw new IllegalArgumentException("No attribute override '" + setName + "' for " + entityId);
            }
        }
        values.forEach((attributeId, value) -> {
            Holder<Attribute> holder = resolveAttribute(attributeId, entityId);
            AttributeInstance instance = mob.getAttribute(holder);
            if (instance == null) {
                throw new IllegalStateException("Entity " + entityId + " is missing attribute " + attributeId);
            }
            instance.setBaseValue(value);
        });
    }

    private static void loadFile(Path path) {
        String fileName = path.getFileName().toString();
        String stem = fileName.substring(0, fileName.length() - ".json".length());
        Identifier expectedId = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, stem);
        try {
            var json = JsonParser.parseString(Files.readString(path));
            EntityDefinition definition = EntityDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            if (!expectedId.equals(definition.entity())) {
                throw new IllegalStateException("Entity id mismatch: file " + expectedId + " vs json " + definition.entity());
            }
            EntityDefinition previous = DEFINITIONS.putIfAbsent(definition.entity(), definition);
            if (previous != null) {
                throw new IllegalStateException("Duplicate entity definition for " + definition.entity());
            }
        } catch (RuntimeException | IOException e) {
            throw new IllegalStateException("Failed to load entity definition " + path, e);
        }
    }

    private static Path resolveDefinitionsDir() {
        try {
            Optional<Path> packed = FabricLoader.getInstance()
                    .getModContainer(DWMReference.MOD_ID)
                    .flatMap(container -> container.findPath(DEFINITION_DIR));
            if (packed.isPresent() && Files.isDirectory(packed.get())) {
                return packed.get();
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Fabric mod container unavailable for entity definitions: {}", e.toString());
        }
        Path fallback = Path.of("src/main/resources", DEFINITION_DIR);
        if (Files.isDirectory(fallback)) {
            return fallback;
        }
        throw new IllegalStateException("Cannot locate entity definition directory " + DEFINITION_DIR);
    }

    private static EntityDefinition require(Identifier entityId) {
        ensureInitialized();
        EntityDefinition definition = DEFINITIONS.get(entityId);
        if (definition == null) {
            throw new IllegalStateException("Missing entity definition for " + entityId);
        }
        return definition;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    private static Identifier idOf(Mob mob) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (id == null) {
            throw new IllegalStateException("Unregistered entity type " + mob.getType());
        }
        return id;
    }

    private static Holder<Attribute> resolveAttribute(Identifier attributeId, Identifier entityId) {
        return BuiltInRegistries.ATTRIBUTE.get(attributeId).orElseThrow(
                () -> new IllegalArgumentException("Unknown attribute " + attributeId + " on " + entityId)
        );
    }
}
