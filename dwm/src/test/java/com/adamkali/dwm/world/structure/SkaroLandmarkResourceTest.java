package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.entity.DalekJob;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Resource contract for Skaro landmarks (DWM-069): the modular Dalek outpost. */
class SkaroLandmarkResourceTest {
    private static final Path DATA = Path.of("src/main/resources/data");
    private static final Path GENERATED_DATA = Path.of("src/main/generated/data");

    private static final String INTACT = "dwm:dalek_outpost";
    private static final String RUINED = "dwm:dalek_outpost_ruined";
    private static final String RUIN_PROCESSORS = "dwm:skaro/dalek_outpost_ruined";
    private static final String GARRISONED = "dwm:garrisoned_single_pool_element";

    /** Documented per-piece size caps (X, Y, Z) from feature-skaro-dimension.md. */
    private static final Map<String, int[]> SIZE_CAPS = Map.of(
            DalekOutpostTemplates.CORE, new int[] {11, 7, 11},
            DalekOutpostTemplates.WATCHTOWER, new int[] {5, 14, 5},
            DalekOutpostTemplates.BARRICADE, new int[] {7, 3, 3},
            DalekOutpostTemplates.SENSOR_MAST, new int[] {3, 8, 3}
    );

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        // Loading the registry classes registers DWM's blocks and items for the id lookups below.
        assertNotNull(DWMBlocks.BRONZE_DALEKANIUM_PANEL);
        assertNotNull(DWMItems.RADIATION_METER);
    }

    @Test
    void outpostStructuresLockPlacementValuesAndResolveReferences() throws IOException {
        for (String structure : List.of(INTACT, RUINED)) {
            JSONObject json = readJson(resource("worldgen/structure", structure));
            assertEquals("minecraft:jigsaw", json.getString("type"), structure);
            assertEquals("surface_structures", json.getString("step"), structure);
            assertEquals("beard_thin", json.getString("terrain_adaptation"), structure);
            assertEquals(1, json.getInt("size"), structure);
            assertEquals(0, json.getJSONObject("start_height").getInt("absolute"), structure);
            assertEquals("WORLD_SURFACE_WG", json.getString("project_start_to_heightmap"), structure);
            assertEquals(32, json.getInt("max_distance_from_center"), structure);
            assertFalse(json.getBoolean("use_expansion_hack"), structure);
            assertTrue(json.getJSONObject("spawn_overrides").isEmpty(), structure + " must not override spawns");
            assertExists(resource("worldgen/template_pool", json.getString("start_pool")));
            String biomes = json.getString("biomes");
            assertTrue(biomes.startsWith("#"), structure + " biomes must be a tag");
            assertExists(resource("tags/worldgen/biome", biomes.substring(1)));
        }

        assertEquals("dwm:skaro/dalek_outpost/core",
                readJson(resource("worldgen/structure", INTACT)).getString("start_pool"));

        JSONObject ruined = readJson(resource("worldgen/structure", RUINED));
        assertEquals("dwm:skaro/dalek_outpost/ruined_core", ruined.getString("start_pool"));
        JSONArray aliases = ruined.getJSONArray("pool_aliases");
        assertEquals(1, aliases.length());
        JSONObject alias = aliases.getJSONObject(0);
        assertEquals("minecraft:direct", alias.getString("type"));
        assertEquals(DalekOutpostTemplates.WING_POOL, alias.getString("alias"));
        assertEquals("dwm:skaro/dalek_outpost/ruined_wings", alias.getString("target"));
        assertExists(resource("worldgen/template_pool", alias.getString("alias")));
        assertExists(resource("worldgen/template_pool", alias.getString("target")));
    }

    @Test
    void outpostBiomesAreSkaroOnlyAndExcludeThalPlateauAndMire() throws IOException {
        Set<String> skaro = tagValues("dwm:is_skaro");
        Set<String> intact = tagValues("dwm:has_structure/dalek_outpost");
        Set<String> ruined = tagValues("dwm:has_structure/dalek_outpost_ruined");

        assertEquals(Set.of("dwm:skaro_irradiated_wastes", "dwm:skaro_drammankin_mountains"), intact);
        assertEquals(Set.of("dwm:skaro_petrified_jungle"), ruined);
        for (Set<String> tag : List.of(intact, ruined)) {
            assertTrue(skaro.containsAll(tag), () -> tag + " must only list Skaro biomes");
            assertFalse(tag.contains("dwm:skaro_thal_plateau"), "No hostile outpost on the Thal Plateau");
            assertFalse(tag.contains("dwm:skaro_drammankin_mire"), "The mire hosts nests and wreckage, not outposts");
        }
        Set<String> overlap = new HashSet<>(intact);
        overlap.retainAll(ruined);
        assertTrue(overlap.isEmpty(), "Intact and ruined outposts must not share a biome");
    }

    @Test
    void militaryLandmarkSetLocksSpreadAndUsesUniqueSalt() throws IOException {
        JSONObject set = readJson(resource("worldgen/structure_set", "dwm:skaro_military_landmarks"));
        JSONArray structures = set.getJSONArray("structures");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < structures.length(); i++) {
            JSONObject entry = structures.getJSONObject(i);
            ids.add(entry.getString("structure"));
            assertEquals(1, entry.getInt("weight"));
            assertExists(resource("worldgen/structure", entry.getString("structure")));
        }
        assertEquals(Set.of(INTACT, RUINED), ids);

        JSONObject placement = set.getJSONObject("placement");
        assertEquals("minecraft:random_spread", placement.getString("type"));
        assertEquals(32, placement.getInt("spacing"));
        assertEquals(12, placement.getInt("separation"));
        int salt = placement.getInt("salt");
        assertEquals(1709435291, salt);

        try (Stream<Path> others = Files.list(DATA.resolve("dwm/worldgen/structure_set"))) {
            for (Path other : others.toList()) {
                if (!other.getFileName().toString().equals("skaro_military_landmarks.json")) {
                    assertFalse(readJson(other).getJSONObject("placement").getInt("salt") == salt,
                            () -> other + " reuses the military landmark salt");
                }
            }
        }
    }

    @Test
    void ruinedPoolsMirrorIntactPoolsWithRuinProcessors() throws IOException {
        assertPoolPair("core", "ruined_core");
        assertPoolPair("wings", "ruined_wings");

        JSONArray processors = readJson(resource("worldgen/processor_list", RUIN_PROCESSORS)).getJSONArray("processors");
        JSONObject rot = processors.getJSONObject(0);
        assertEquals("minecraft:block_rot", rot.getString("processor_type"));
        assertEquals("#dwm:silver_dalekanium_architecture", rot.getString("rottable_blocks"),
                "Only silver walls rot, so floors, the chest and the doorway stay intact");
        assertExists(GENERATED_DATA.resolve("dwm/tags/block/silver_dalekanium_architecture.json"));
        assertEquals("minecraft:rule", processors.getJSONObject(1).getString("processor_type"));
    }

    @Test
    void templatesStayWithinCapsWithoutEntitiesAndResolveBlocksAndPools() throws IOException {
        for (Map.Entry<String, int[]> cap : SIZE_CAPS.entrySet()) {
            CompoundTag template = readTemplate(cap.getKey());
            ListTag size = template.getListOrEmpty("size");
            for (int axis = 0; axis < 3; axis++) {
                int extent = size.getIntOr(axis, -1);
                int limit = cap.getValue()[axis];
                assertTrue(extent > 0 && extent <= limit, () -> cap.getKey() + " exceeds its documented size cap");
            }
            assertTrue(template.getListOrEmpty("entities").isEmpty(), () -> cap.getKey() + " must not embed entities");

            ListTag palette = template.getListOrEmpty("palette");
            for (int i = 0; i < palette.size(); i++) {
                String name = palette.getCompoundOrEmpty(i).getStringOr("Name", "");
                assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(name)),
                        () -> cap.getKey() + " uses unregistered block " + name);
            }

            for (CompoundTag nbt : blockEntities(template)) {
                if (nbt.getStringOr("id", "").equals("minecraft:jigsaw")) {
                    String pool = nbt.getStringOr("pool", "");
                    if (!pool.equals("minecraft:empty")) {
                        assertExists(resource("worldgen/template_pool", pool));
                    }
                    assertTrue(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(nbt.getStringOr("final_state", ""))));
                }
            }
        }
    }

    @Test
    void onlyTheCoreHoldsOneLootChest() throws IOException {
        int containers = 0;
        for (String path : SIZE_CAPS.keySet()) {
            for (CompoundTag nbt : blockEntities(readTemplate(path))) {
                if (nbt.getStringOr("id", "").equals("minecraft:jigsaw")) {
                    continue;
                }
                containers++;
                assertEquals(DalekOutpostTemplates.CORE, path, "Only the core checkpoint may hold a container");
                assertEquals("minecraft:chest", nbt.getStringOr("id", ""));
                assertEquals(DalekOutpostTemplates.LOOT_TABLE, nbt.getStringOr("LootTable", ""));
            }
        }
        assertEquals(1, containers, "The outpost holds exactly one loot container");
        assertExists(resource("loot_table", DalekOutpostTemplates.LOOT_TABLE));
    }

    @Test
    void lootTableOnlyUsesRegisteredItems() throws IOException {
        JSONObject table = readJson(resource("loot_table", DalekOutpostTemplates.LOOT_TABLE));
        assertEquals("minecraft:chest", table.getString("type"));
        JSONArray pools = table.getJSONArray("pools");
        int entries = 0;
        for (int p = 0; p < pools.length(); p++) {
            JSONArray poolEntries = pools.getJSONObject(p).getJSONArray("entries");
            for (int e = 0; e < poolEntries.length(); e++) {
                JSONObject entry = poolEntries.getJSONObject(e);
                assertEquals("minecraft:item", entry.getString("type"));
                String item = entry.getString("name");
                assertTrue(BuiltInRegistries.ITEM.containsKey(Identifier.parse(item)), () -> "Unregistered loot item " + item);
                entries++;
            }
        }
        assertTrue(entries > 0);
    }

    @Test
    void intactPoolsGarrisonEveryPieceWithTheRightJobs() throws IOException {
        Map<String, List<GarrisonPost>> core = garrisons("core");
        List<GarrisonPost> coreSentries = core.get(DalekOutpostTemplates.CORE).stream()
                .filter(post -> post.job() == DalekJob.SENTRY).toList();
        assertEquals(2, coreSentries.size(), "The core is guarded by two sentries");
        assertTrue(coreSentries.stream().allMatch(post -> post.chance() == 1.0F), "Core sentries always spawn");
        assertEquals(1, core.get(DalekOutpostTemplates.CORE).stream()
                .filter(post -> post.job() == DalekJob.TECHNICIAN).count());

        Map<String, List<GarrisonPost>> wings = garrisons("wings");
        assertEquals(List.of(DalekJob.LOOKOUT), jobs(wings.get(DalekOutpostTemplates.WATCHTOWER)));
        assertEquals(List.of(DalekJob.SENTRY), jobs(wings.get(DalekOutpostTemplates.BARRICADE)));
        assertEquals(List.of(DalekJob.TECHNICIAN), jobs(wings.get(DalekOutpostTemplates.SENSOR_MAST)));
        assertTrue(wings.get(DalekOutpostTemplates.WATCHTOWER).getFirst().pos().getY() > 8,
                "The lookout stands on the watchtower deck");
    }

    @Test
    void watchtowerLookoutHasHeadroomAboveTheDeck() throws IOException {
        GarrisonPost lookout = garrisons("wings").get(DalekOutpostTemplates.WATCHTOWER).getFirst();
        TemplateLayout watchtower = DalekOutpostTemplates.watchtower();
        int x = lookout.pos().getX();
        int z = lookout.pos().getZ();
        // A Dalek is taller than two blocks; it must not stand inside the roof.
        for (int y = lookout.pos().getY(); y < lookout.pos().getY() + 4; y++) {
            char symbol = watchtower.layers().get(y).get(z).charAt(x);
            assertTrue(watchtower.legend().get(symbol).state().isAir(),
                    "Watchtower block above the lookout at y=" + y + " must be air, was '" + symbol + "'");
        }
    }

    @Test
    void ruinedPoolsOnlyKeepASmallRemnant() throws IOException {
        int total = 0;
        for (String pool : List.of("ruined_core", "ruined_wings")) {
            for (List<GarrisonPost> posts : garrisons(pool).values()) {
                total += posts.size();
                for (GarrisonPost post : posts) {
                    assertTrue(post.job() == DalekJob.SENTRY || post.job() == DalekJob.PATROL,
                            () -> "Ruined outposts have no " + post.job());
                }
            }
        }
        assertTrue(total >= 1 && total <= 3, "Ruined garrison must be 1-3 Daleks, was " + total);
    }

    @Test
    void garrisonPostCodecRoundTripsAndValidates() {
        GarrisonPost post = new GarrisonPost(Identifier.parse("dwm:paradigm_dalek"), DalekJob.TECHNICIAN,
                new BlockPos(5, 1, 5), Direction.NORTH, 0.5F, Optional.of("paradigm_scientist"), 9,
                List.of(new BlockPos(5, 1, 4)));
        JsonElement json = GarrisonPost.CODEC.encodeStart(JsonOps.INSTANCE, post).getOrThrow();
        assertEquals(post, GarrisonPost.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());

        JsonObject minimal = new JsonObject();
        minimal.addProperty("entity", "dwm:dalek");
        minimal.addProperty("job", "sentry");
        minimal.add("pos", JsonParser.parseString("[1,2,3]"));
        GarrisonPost parsed = GarrisonPost.CODEC.parse(JsonOps.INSTANCE, minimal).getOrThrow();
        assertEquals(Direction.SOUTH, parsed.facing());
        assertEquals(1.0F, parsed.chance());
        assertEquals(GarrisonPost.DEFAULT_PATROL_RADIUS, parsed.radius());

        minimal.addProperty("facing", "up");
        assertTrue(GarrisonPost.CODEC.parse(JsonOps.INSTANCE, minimal).result().isEmpty(), "Facing must be horizontal");
    }

    /** Garrison posts keyed by the template each element places. */
    private static Map<String, List<GarrisonPost>> garrisons(String poolName) throws IOException {
        JSONArray elements = readJson(resource("worldgen/template_pool", "dwm:skaro/dalek_outpost/" + poolName))
                .getJSONArray("elements");
        Map<String, List<GarrisonPost>> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i).getJSONObject("element");
            if (!element.getString("element_type").equals(GARRISONED)) {
                continue;
            }
            String template = element.getString("location").substring("dwm:".length());
            List<GarrisonPost> posts = new ArrayList<>();
            JSONArray garrison = element.getJSONArray("garrison");
            for (int g = 0; g < garrison.length(); g++) {
                JsonElement json = JsonParser.parseString(garrison.getJSONObject(g).toString());
                posts.add(GarrisonPost.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            }
            result.put(template, posts);
        }
        return result;
    }

    private static List<DalekJob> jobs(List<GarrisonPost> posts) {
        return posts.stream().map(GarrisonPost::job).toList();
    }

    private static void assertPoolPair(String intactName, String ruinedName) throws IOException {
        JSONArray intact = readJson(resource("worldgen/template_pool", "dwm:skaro/dalek_outpost/" + intactName)).getJSONArray("elements");
        JSONArray ruined = readJson(resource("worldgen/template_pool", "dwm:skaro/dalek_outpost/" + ruinedName)).getJSONArray("elements");
        assertEquals(intact.length(), ruined.length(), ruinedName + " must mirror " + intactName);
        for (int i = 0; i < intact.length(); i++) {
            JSONObject a = intact.getJSONObject(i);
            JSONObject b = ruined.getJSONObject(i);
            assertEquals(a.getInt("weight"), b.getInt("weight"));
            JSONObject elementA = a.getJSONObject("element");
            JSONObject elementB = b.getJSONObject("element");
            assertEquals(elementA.getString("element_type"), elementB.getString("element_type"));
            if (!elementA.getString("element_type").equals(GARRISONED)) {
                continue;
            }
            assertEquals(elementA.getString("location"), elementB.getString("location"));
            assertEquals(elementA.getString("projection"), elementB.getString("projection"));
            assertEquals("minecraft:empty", elementA.getString("processors"));
            assertEquals(RUIN_PROCESSORS, elementB.getString("processors"));
            assertExists(templateFile(elementA.getString("location")));
        }
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

    /** Resolves {@code namespace:path} under {@code data/<namespace>/<registryDir>/<path>.json}. */
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
