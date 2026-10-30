package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DalekJob;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Resource contract for the Dalek battlefield trench (DWM-069). */
class SkaroTrenchResourceTest {
    private static final Path DATA = Path.of("src/main/resources/data");

    private static final String INTACT = "dwm:dalek_trench";
    private static final String RUINED = "dwm:dalek_trench_ruined";
    private static final String RUIN_PROCESSORS = "dwm:skaro/dalek_outpost_ruined";
    private static final String GARRISONED = "dwm:garrisoned_single_pool_element";
    /** Blocks the trench is carved below ground; the structure start height is its negative. */
    private static final int SINK = 2;
    private static final List<String> POOLS = List.of("start", "segments", "bunkers");

    /** Documented per-piece size caps (X, Y, Z) from feature-skaro-dimension.md. */
    private static final Map<String, int[]> SIZE_CAPS = Map.of(
            DalekTrenchTemplates.CHECKPOINT, new int[] {11, 5, 11},
            DalekTrenchTemplates.SEGMENT, new int[] {5, 4, 8},
            DalekTrenchTemplates.JUNCTION, new int[] {5, 4, 8},
            DalekTrenchTemplates.BUNKER, new int[] {9, 6, 9},
            DalekTrenchTemplates.END_CAP, new int[] {5, 4, 3}
    );

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        assertNotNull(DWMBlocks.BRONZE_DALEKANIUM_PANEL);
    }

    @Test
    void trenchStructuresLockPlacementValuesAndResolveReferences() throws IOException {
        for (String structure : List.of(INTACT, RUINED)) {
            JSONObject json = readJson(resource("worldgen/structure", structure));
            assertEquals("minecraft:jigsaw", json.getString("type"), structure);
            assertEquals("surface_structures", json.getString("step"), structure);
            assertEquals("beard_thin", json.getString("terrain_adaptation"), structure);
            assertEquals(5, json.getInt("size"), structure);
            assertEquals(-SINK, json.getJSONObject("start_height").getInt("absolute"),
                    structure + " starts the rigid checkpoint as deep as the terrain-matched pieces sink");
            assertEquals("WORLD_SURFACE_WG", json.getString("project_start_to_heightmap"), structure);
            assertEquals(56, json.getInt("max_distance_from_center"), structure);
            assertFalse(json.getBoolean("use_expansion_hack"), structure);
            assertTrue(json.getJSONObject("spawn_overrides").isEmpty(), structure + " must not override spawns");
            assertExists(resource("worldgen/template_pool", json.getString("start_pool")));
            assertExists(resource("tags/worldgen/biome", json.getString("biomes").substring(1)));
        }
        assertEquals("dwm:skaro/dalek_trench/start", readJson(resource("worldgen/structure", INTACT)).getString("start_pool"));

        JSONObject ruined = readJson(resource("worldgen/structure", RUINED));
        assertEquals("dwm:skaro/dalek_trench/ruined_start", ruined.getString("start_pool"));
        JSONArray aliases = ruined.getJSONArray("pool_aliases");
        Map<String, String> byAlias = new LinkedHashMap<>();
        for (int i = 0; i < aliases.length(); i++) {
            byAlias.put(aliases.getJSONObject(i).getString("alias"), aliases.getJSONObject(i).getString("target"));
        }
        assertEquals(Map.of(
                DalekTrenchTemplates.SEGMENT_POOL, "dwm:skaro/dalek_trench/ruined_segments",
                DalekTrenchTemplates.BUNKER_POOL, "dwm:skaro/dalek_trench/ruined_bunkers"), byAlias);
        byAlias.forEach((alias, target) -> {
            assertExists(resource("worldgen/template_pool", alias));
            assertExists(resource("worldgen/template_pool", target));
        });
    }

    @Test
    void trenchBiomesAreSkaroOnlyAndExcludeThalPlateauAndMire() throws IOException {
        Set<String> skaro = tagValues("dwm:is_skaro");
        Set<String> intact = tagValues("dwm:has_structure/dalek_trench");
        Set<String> ruined = tagValues("dwm:has_structure/dalek_trench_ruined");

        assertEquals(Set.of("dwm:skaro_irradiated_wastes"), intact);
        assertEquals(Set.of("dwm:skaro_petrified_jungle", "dwm:skaro_drammankin_mountains"), ruined);
        for (Set<String> tag : List.of(intact, ruined)) {
            assertTrue(skaro.containsAll(tag), () -> tag + " must only list Skaro biomes");
            assertFalse(tag.contains("dwm:skaro_thal_plateau"), "No hostile trench on the Thal Plateau");
            assertFalse(tag.contains("dwm:skaro_drammankin_mire"), "The mire hosts nests and wreckage, not trenches");
        }
        Set<String> overlap = new HashSet<>(intact);
        overlap.retainAll(ruined);
        assertTrue(overlap.isEmpty(), "Intact and ruined trenches must not share a biome");
    }

    @Test
    void trenchesJoinTheMilitaryLandmarkSetSoThalSettlementsKeepClear() throws IOException {
        JSONArray structures = readJson(resource("worldgen/structure_set", "dwm:skaro_military_landmarks"))
                .getJSONArray("structures");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < structures.length(); i++) {
            ids.add(structures.getJSONObject(i).getString("structure"));
        }
        assertTrue(ids.containsAll(Set.of(INTACT, RUINED)));
        JSONObject thal = readJson(resource("worldgen/structure_set", "dwm:thal_settlement"));
        assertEquals("dwm:skaro_military_landmarks", thal.getJSONObject("placement").getJSONObject("exclusion_zone").getString("other_set"));
    }

    @Test
    void ruinedPoolsMirrorIntactPoolsWithRuinProcessors() throws IOException {
        for (String pool : POOLS) {
            JSONArray intact = elements(pool);
            JSONArray ruined = elements("ruined_" + pool);
            assertEquals(intact.length(), ruined.length(), pool);
            for (int i = 0; i < intact.length(); i++) {
                JSONObject a = intact.getJSONObject(i).getJSONObject("element");
                JSONObject b = ruined.getJSONObject(i).getJSONObject("element");
                assertEquals(intact.getJSONObject(i).getInt("weight"), ruined.getJSONObject(i).getInt("weight"));
                assertEquals(a.getString("location"), b.getString("location"));
                assertEquals(a.getString("projection"), b.getString("projection"));
                assertEquals(a.optInt("sink", 0), b.optInt("sink", 0));
                assertEquals(a.getJSONArray("garrison").toString(), b.getJSONArray("garrison").toString());
                assertEquals("minecraft:empty", a.getString("processors"));
                assertEquals(RUIN_PROCESSORS, b.getString("processors"));
            }
        }
    }

    @Test
    void terrainMatchedPiecesSinkIntoTheGroundAndRigidOnesDoNot() throws IOException {
        for (String pool : POOLS) {
            for (String prefix : List.of("", "ruined_")) {
                JSONArray elements = elements(prefix + pool);
                for (int i = 0; i < elements.length(); i++) {
                    JSONObject element = elements.getJSONObject(i).getJSONObject("element");
                    boolean terrainMatching = element.getString("projection").equals("terrain_matching");
                    assertEquals(terrainMatching ? SINK : 0, element.optInt("sink", 0), prefix + pool);
                }
            }
        }
    }

    @Test
    void templatesStayWithinCapsWithoutEntitiesAndResolveBlocksAndPools() throws IOException {
        for (Map.Entry<String, int[]> cap : SIZE_CAPS.entrySet()) {
            CompoundTag template = readTemplate(cap.getKey());
            ListTag size = template.getListOrEmpty("size");
            for (int axis = 0; axis < 3; axis++) {
                int extent = size.getIntOr(axis, -1);
                assertTrue(extent > 0 && extent <= cap.getValue()[axis], () -> cap.getKey() + " exceeds its documented size cap");
            }
            assertTrue(template.getListOrEmpty("entities").isEmpty(), () -> cap.getKey() + " must not embed entities");

            ListTag palette = template.getListOrEmpty("palette");
            for (int i = 0; i < palette.size(); i++) {
                String name = palette.getCompoundOrEmpty(i).getStringOr("Name", "");
                assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(name)), () -> cap.getKey() + " uses unregistered block " + name);
            }
            for (CompoundTag nbt : blockEntities(template)) {
                if (nbt.getStringOr("id", "").equals("minecraft:jigsaw")) {
                    String pool = nbt.getStringOr("pool", "");
                    if (!pool.equals("minecraft:empty")) {
                        assertExists(resource("worldgen/template_pool", pool));
                    }
                }
            }
        }
    }

    @Test
    void everyPoolElementPointsAtAnExistingTemplate() throws IOException {
        Set<String> used = new HashSet<>();
        for (String pool : POOLS) {
            JSONArray elements = elements(pool);
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i).getJSONObject("element");
                assertEquals(GARRISONED, element.getString("element_type"));
                String location = element.getString("location");
                used.add(location.substring("dwm:".length()));
                assertTrue(Files.isRegularFile(templateFile(location)), () -> "Missing template " + location);
            }
        }
        assertEquals(SIZE_CAPS.keySet(), used, "Every trench template is reachable from a pool");
    }

    @Test
    void onlyTheCheckpointHoldsOneLootChest() throws IOException {
        int containers = 0;
        for (String path : SIZE_CAPS.keySet()) {
            for (CompoundTag nbt : blockEntities(readTemplate(path))) {
                if (nbt.getStringOr("id", "").equals("minecraft:jigsaw")) {
                    continue;
                }
                containers++;
                assertEquals(DalekTrenchTemplates.CHECKPOINT, path, "Only the checkpoint may hold a container");
                assertEquals("minecraft:chest", nbt.getStringOr("id", ""));
                assertExists(resource("loot_table", nbt.getStringOr("LootTable", "")));
            }
        }
        assertEquals(1, containers, "The trench holds exactly one loot container");
    }

    @Test
    void garrisonsSpreadSeveralJobsAcrossTheTrench() throws IOException {
        Set<DalekJob> jobs = EnumSet.noneOf(DalekJob.class);
        Map<String, List<GarrisonPost>> start = garrisons("start");
        Map<String, List<GarrisonPost>> segments = garrisons("segments");
        Map<String, List<GarrisonPost>> bunkers = garrisons("bunkers");
        for (Map<String, List<GarrisonPost>> pool : List.of(start, segments, bunkers)) {
            for (List<GarrisonPost> posts : pool.values()) {
                posts.forEach(post -> jobs.add(post.job()));
            }
        }
        assertEquals(EnumSet.of(DalekJob.SENTRY, DalekJob.PATROL, DalekJob.LOOKOUT, DalekJob.TECHNICIAN,
                DalekJob.GUNNER, DalekJob.COURIER), jobs);

        List<GarrisonPost> checkpoint = start.get(DalekTrenchTemplates.CHECKPOINT);
        assertEquals(4, checkpoint.stream().filter(p -> p.job() == DalekJob.SENTRY && p.chance() == 1.0F).count(),
                "The checkpoint gates are always sentried");
        assertTrue(checkpoint.stream().anyMatch(p -> p.job() == DalekJob.TECHNICIAN));

        assertTrue(segments.get(DalekTrenchTemplates.END_CAP).stream().allMatch(p -> p.job() == DalekJob.GUNNER));
        assertTrue(bunkers.get(DalekTrenchTemplates.BUNKER).stream().anyMatch(p -> p.job() == DalekJob.GUNNER));
    }

    @Test
    void couriersRunRoutesInsideTheirOwnPieceAndWithinTheLeash() throws IOException {
        for (Map<String, List<GarrisonPost>> pool : List.of(garrisons("start"), garrisons("segments"))) {
            for (Map.Entry<String, List<GarrisonPost>> piece : pool.entrySet()) {
                int[] cap = SIZE_CAPS.get(piece.getKey());
                for (GarrisonPost post : piece.getValue()) {
                    if (post.job() != DalekJob.COURIER) {
                        continue;
                    }
                    assertFalse(post.workSpots().isEmpty(), "A courier needs a route");
                    for (var spot : post.workSpots()) {
                        assertTrue(spot.getX() < cap[0] && spot.getZ() < cap[2], "Courier route leaves " + piece.getKey());
                        assertTrue(Math.sqrt(post.pos().distSqr(spot)) < 36, "Courier route exceeds the leash");
                    }
                }
            }
        }
    }

    @Test
    void postsStandInsideTheirTemplateOnAirWithHeadroom() throws IOException {
        Map<String, TemplateLayout> layouts = new LinkedHashMap<>();
        DalekTrenchTemplates.all().forEach(layout -> layouts.put(layout.path(), layout));
        for (String pool : POOLS) {
            for (Map.Entry<String, List<GarrisonPost>> piece : garrisons(pool).entrySet()) {
                TemplateLayout layout = layouts.get(piece.getKey());
                for (GarrisonPost post : piece.getValue()) {
                    List<net.minecraft.core.BlockPos> spots = new ArrayList<>(post.workSpots());
                    spots.add(post.pos());
                    for (var spot : spots) {
                        // A Dalek is taller than two blocks: three air blocks above the floor, or open sky.
                        for (int y = spot.getY(); y < spot.getY() + 3 && y < layout.layers().size(); y++) {
                            char symbol = layout.layers().get(y).get(spot.getZ()).charAt(spot.getX());
                            assertTrue(layout.legend().get(symbol).state().isAir(),
                                    () -> piece.getKey() + " post " + spot + " is blocked by '" + symbol + "'");
                        }
                    }
                }
            }
        }
    }

    private static Map<String, List<GarrisonPost>> garrisons(String poolName) throws IOException {
        JSONArray elements = elements(poolName);
        Map<String, List<GarrisonPost>> result = new LinkedHashMap<>();
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i).getJSONObject("element");
            String template = element.getString("location").substring("dwm:".length());
            List<GarrisonPost> posts = new ArrayList<>();
            JSONArray garrison = element.getJSONArray("garrison");
            for (int g = 0; g < garrison.length(); g++) {
                JsonElement json = JsonParser.parseString(garrison.getJSONObject(g).toString());
                posts.add(GarrisonPost.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            }
            // Bunker variants share a template; keep the union so every variant is checked.
            result.computeIfAbsent(template, key -> new ArrayList<>()).addAll(posts);
        }
        return result;
    }

    private static JSONArray elements(String poolName) throws IOException {
        return readJson(resource("worldgen/template_pool", "dwm:skaro/dalek_trench/" + poolName)).getJSONArray("elements");
    }

    private static CompoundTag readTemplate(String path) throws IOException {
        return NbtIo.readCompressed(templateFile("dwm:" + path), NbtAccounter.unlimitedHeap());
    }

    private static Path templateFile(String id) {
        Identifier identifier = Identifier.parse(id);
        return DATA.resolve(identifier.getNamespace()).resolve("structure").resolve(identifier.getPath() + ".nbt");
    }

    private static List<CompoundTag> blockEntities(CompoundTag template) {
        List<CompoundTag> result = new ArrayList<>();
        template.getListOrEmpty("blocks").compoundStream()
                .forEach(block -> block.getCompound("nbt").ifPresent(result::add));
        return result;
    }

    private static Set<String> tagValues(String tag) throws IOException {
        JSONArray values = readJson(resource("tags/worldgen/biome", tag)).getJSONArray("values");
        Set<String> result = new HashSet<>();
        for (int i = 0; i < values.length(); i++) {
            result.add(values.getString(i));
        }
        return result;
    }

    private static Path resource(String registryDir, String id) {
        Identifier identifier = Identifier.parse(id);
        return DATA.resolve(identifier.getNamespace()).resolve(registryDir).resolve(identifier.getPath() + ".json");
    }

    private static void assertExists(Path path) {
        assertTrue(Files.isRegularFile(path), () -> "Missing resource " + path);
    }

    private static JSONObject readJson(Path path) throws IOException {
        assertExists(path);
        try (Reader reader = Files.newBufferedReader(path)) {
            return new JSONObject(new JSONTokener(reader));
        }
    }
}
