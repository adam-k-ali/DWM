package com.adamkali.dwm.entity.definition;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.MinecraftTestBootstrap;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityDefinitionCatalogTest {
    private static final Path DEFINITION_DIR = Path.of(
            "src/main/resources/data/" + DWMReference.MOD_ID + "/entity/definition"
    );

    private static Map<Identifier, EntityDefinition> definitions;

    @BeforeAll
    static void loadProductionDefinitions() throws IOException {
        MinecraftTestBootstrap.ensure();
        EntityDefinitions.initialize();
        definitions = loadAll();
    }

    @Test
    void productionFilesRoundTripThroughCodec() throws IOException {
        try (var stream = Files.list(DEFINITION_DIR)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                var json = JsonParser.parseString(Files.readString(path));
                EntityDefinition decoded = EntityDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                var encoded = EntityDefinition.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
                EntityDefinition roundTripped = EntityDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
                assertEquals(decoded, roundTripped, path.getFileName().toString());
            }
        }
    }

    @Test
    void fileStemMatchesEntityId() {
        assertEquals(5, definitions.size());
        for (Map.Entry<Identifier, EntityDefinition> entry : definitions.entrySet()) {
            assertEquals(entry.getKey(), entry.getValue().entity());
        }
    }

    @Test
    void loaderExposesTheSameProductionDefinitions() {
        for (Identifier id : definitions.keySet()) {
            assertEquals(definitions.get(id), EntityDefinitions.get(id).orElseThrow());
        }
    }

    @Test
    void broakirMatchesShippedStats() {
        EntityDefinition definition = definition("broakir");
        assertEquals(EntityAttributeBase.ANIMAL, definition.attributeBase());
        assertEquals(15.0, attribute(definition, "max_health"));
        assertEquals(0.25, attribute(definition, "movement_speed"));
        assertEquals(List.of(
                EntityGoal.Kind.FLOAT,
                EntityGoal.Kind.PANIC,
                EntityGoal.Kind.WATER_AVOIDING_RANDOM_STROLL,
                EntityGoal.Kind.LOOK_AT,
                EntityGoal.Kind.RANDOM_LOOK_AROUND
        ), kinds(definition.goals()));
        assertEquals(List.of(0, 1, 2, 3, 4), priorities(definition.goals()));
        assertTrue(definition.targets().isEmpty());
        EntityGoal.Panic panic = assertInstanceOf(EntityGoal.Panic.class, definition.goals().get(1).goal());
        assertEquals(1.25, panic.speed());
        EntityGoal.LookAt lookAt = assertInstanceOf(EntityGoal.LookAt.class, definition.goals().get(3).goal());
        assertEquals(id("minecraft", "player"), lookAt.target());
        assertEquals(6.0F, lookAt.lookDistance());
    }

    @Test
    void flutterwingMatchesShippedStats() {
        EntityDefinition definition = definition("flutterwing");
        assertEquals(EntityAttributeBase.ANIMAL, definition.attributeBase());
        assertEquals(10.0, attribute(definition, "max_health"));
        assertEquals(0.6, attribute(definition, "flying_speed"));
        assertEquals(0.3, attribute(definition, "movement_speed"));
        assertEquals(EntityGoal.Kind.WATER_AVOIDING_RANDOM_FLYING, definition.goals().get(2).goal().kind());
    }

    @Test
    void mewingDogMatchesShippedStatsAndTamedOverride() {
        EntityDefinition definition = definition("mewing_dog");
        assertEquals(EntityAttributeBase.ANIMAL, definition.attributeBase());
        assertEquals(8.0, attribute(definition, "max_health"));
        assertEquals(0.3, attribute(definition, "movement_speed"));
        assertEquals(2.0, attribute(definition, "attack_damage"));
        assertEquals(40.0, definition.attributeOverrides().get("tamed").get(id("minecraft", "max_health")));
        assertEquals(EntityGoal.Kind.TAMABLE_PANIC, definition.goals().get(1).goal().kind());
        assertEquals(EntityGoal.Kind.FOLLOW_OWNER, definition.goals().get(5).goal().kind());
        assertEquals(
                List.of(
                        EntityGoal.Kind.OWNER_HURT_BY_TARGET,
                        EntityGoal.Kind.OWNER_HURT_TARGET,
                        EntityGoal.Kind.HURT_BY_TARGET,
                        EntityGoal.Kind.NEAREST_ATTACKABLE_TARGET,
                        EntityGoal.Kind.RESET_UNIVERSAL_ANGER
                ),
                kinds(definition.targets())
        );
        EntityGoal.NearestAttackableTarget nearest =
                assertInstanceOf(EntityGoal.NearestAttackableTarget.class, definition.targets().get(3).goal());
        assertEquals(Optional.of(10), nearest.interval());
        assertEquals(Optional.of(false), nearest.mustReach());
        assertTrue(nearest.filter().isPresent());
        assertEquals(TargetFilter.Kind.IS_ANGRY_AT, nearest.filter().orElseThrow().kind());
    }

    @Test
    void timeLordMatchesShippedStats() {
        EntityDefinition definition = definition("time_lord");
        assertEquals(EntityAttributeBase.MOB, definition.attributeBase());
        assertEquals(20.0, attribute(definition, "max_health"));
        assertEquals(0.3, attribute(definition, "movement_speed"));
        EntityGoal.OpenDoor openDoor = assertInstanceOf(EntityGoal.OpenDoor.class, definition.goals().get(2).goal());
        assertTrue(openDoor.closeDoor());
    }

    @Test
    void dalekMatchesShippedStatsAndFlightCondition() {
        EntityDefinition definition = definition("dalek");
        assertEquals(EntityAttributeBase.MONSTER, definition.attributeBase());
        assertEquals(30.0, attribute(definition, "max_health"));
        assertEquals(0.23, attribute(definition, "movement_speed"));
        assertEquals(0.4, attribute(definition, "flying_speed"));
        assertEquals(24.0, attribute(definition, "follow_range"));
        assertEquals(4.0, attribute(definition, "attack_damage"));
        assertEquals(0.9, attribute(definition, "knockback_resistance"));
        assertEquals(6.0, attribute(definition, "armor"));
        assertEquals(EntityGoal.Kind.DALEK_FLIGHT, definition.goals().get(1).goal().kind());
        EntityGoalBinding stroll = definition.goals().get(3);
        assertEquals(EntityGoal.Kind.WATER_AVOIDING_RANDOM_STROLL, stroll.goal().kind());
        assertEquals(GoalCondition.Kind.NOT_FLYING, stroll.canUse().orElseThrow().kind());
        assertEquals(EntityGoal.Kind.DALEK_SHARE_TARGET, definition.targets().get(1).goal().kind());
        EntityGoal.DalekFlight flight = assertInstanceOf(EntityGoal.DalekFlight.class, definition.goals().get(1).goal());
        assertEquals(2.5, flight.flyYDelta());
        assertEquals(4.0, flight.minDistanceForMissingPath());
    }

    @Test
    void unknownGoalTypeFailsToParse() {
        var json = JsonParser.parseString("""
                {
                  "entity": "dwm:broakir",
                  "attribute_base": "animal",
                  "attributes": { "minecraft:max_health": 15.0 },
                  "goals": [ { "type": "minecraft:does_not_exist", "priority": 0 } ]
                }
                """);
        assertThrows(RuntimeException.class, () -> EntityDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    private static EntityDefinition definition(String path) {
        return definitions.get(id(DWMReference.MOD_ID, path));
    }

    private static double attribute(EntityDefinition definition, String path) {
        return definition.attributes().get(id("minecraft", path));
    }

    private static List<EntityGoal.Kind> kinds(List<EntityGoalBinding> bindings) {
        return bindings.stream().map(binding -> binding.goal().kind()).toList();
    }

    private static List<Integer> priorities(List<EntityGoalBinding> bindings) {
        return bindings.stream().map(EntityGoalBinding::priority).toList();
    }

    private static Map<Identifier, EntityDefinition> loadAll() throws IOException {
        java.util.HashMap<Identifier, EntityDefinition> map = new java.util.HashMap<>();
        try (var stream = Files.list(DEFINITION_DIR)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                String stem = path.getFileName().toString().replace(".json", "");
                var json = JsonParser.parseString(Files.readString(path));
                map.put(id(DWMReference.MOD_ID, stem), EntityDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            }
        }
        return Map.copyOf(map);
    }

    private static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
