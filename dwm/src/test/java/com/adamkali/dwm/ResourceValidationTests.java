package com.adamkali.dwm;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ResourceValidationTests {
    /** Wood type path ids registered in {@link com.adamkali.dwm.block.DWMWoodTypes}. */
    private static final String[] WOOD_TYPE_IDS = {"ash", "dark_ash", "cardinal"};

    /** Suffixes for datagen-owned wood family item defs under {@code assets/dwm/items/}. */
    private static final String[] WOOD_ITEM_SUFFIXES = {
            "boat",
            "button",
            "door",
            "fence",
            "fence_gate",
            "hanging_sign",
            "leaves",
            "log",
            "planks",
            "pressure_plate",
            "sapling",
            "sign",
            "slab",
            "stairs",
            "trapdoor",
            "wood",
    };

    @Test
    public void validateItemModels() {
        assertTrue(JsonValidationHelpers.validateJsonFiles(
                "src/test/resources/schemas/item_model.schema.json",
                "src/client/resources/assets/dwm/models/item"
        ));
    }

    /**
     * Every concrete {@code dwm:} texture in block/item model {@code textures} maps must
     * resolve to a non-empty PNG under client assets. Scans hand-maintained and datagen models.
     */
    @Test
    public void modelDefinedTexturesExist() throws Exception {
        Path textureRoot = Path.of("src/client/resources/assets/dwm/textures");
        List<ModelTextureValidationHelpers.MissingTexture> missing =
                ModelTextureValidationHelpers.collectMissingModelTextures(
                        textureRoot,
                        Path.of("src/client/resources/assets/dwm/models"),
                        Path.of("src/main/generated/assets/dwm/models")
                );
        if (!missing.isEmpty()) {
            fail(ModelTextureValidationHelpers.formatMissingReport(missing));
        }
    }

    /**
     * {@code BoatRenderer} loads {@code textures/entity/boat/<id>.png} from each wood
     * family's model layer. Guards against Dark Ash / Cardinal sharing a copied Ash atlas.
     */
    @Test
    public void boatEntityTexturesExistAndAreDistinct() throws Exception {
        Path root = Path.of("src/client/resources/assets/dwm/textures/entity/boat");
        Set<String> hashes = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String woodTypeId : WOOD_TYPE_IDS) {
            Path texture = root.resolve(woodTypeId + ".png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "BoatRenderer expects assets/dwm/textures/entity/boat/"
                            + woodTypeId + ".png for wood type dwm:" + woodTypeId
            );
            String hex = HexFormat.of().formatHex(sha256.digest(Files.readAllBytes(texture)));
            assertTrue(
                    hashes.add(hex),
                    "Boat entity texture must be unique per wood type, but "
                            + texture + " duplicates another family's atlas"
            );
        }
    }

    /**
     * Minecraft 26.2 hanging signs bake wood from {@code textures/block/{id}_hanging_sign.png}.
     * Guards against missing or copied-Ash atlases (Cardinal previously shared Ash).
     */
    @Test
    public void hangingSignBlockTexturesExistAndAreDistinct() throws Exception {
        Path root = Path.of("src/client/resources/assets/dwm/textures/block");
        Set<String> hashes = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String woodTypeId : WOOD_TYPE_IDS) {
            Path texture = root.resolve(woodTypeId + "_hanging_sign.png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "Hanging sign block models expect assets/dwm/textures/block/"
                            + woodTypeId + "_hanging_sign.png"
            );
            String hex = HexFormat.of().formatHex(sha256.digest(Files.readAllBytes(texture)));
            assertTrue(
                    hashes.add(hex),
                    "Hanging sign block texture must be unique per wood type, but "
                            + texture + " duplicates another family's atlas"
            );
        }
    }

    /**
     * Ceiling/wall hanging signs must parent vanilla 26.2 templates and sample
     * {@code dwm:block/{id}_hanging_sign}. Particle-only models would fail this.
     */
    @Test
    public void generatedHangingSignModelsUseBlockTemplates() throws Exception {
        Path models = Path.of("src/main/generated/assets/dwm/models/block");
        for (String woodTypeId : WOOD_TYPE_IDS) {
            String allTexture = "dwm:block/" + woodTypeId + "_hanging_sign";
            assertHangingSignModel(
                    models.resolve(woodTypeId + "_hanging_sign_rot_0.json"),
                    "minecraft:block/template_hanging_sign_rot_0",
                    allTexture
            );
            assertHangingSignModel(
                    models.resolve(woodTypeId + "_hanging_sign_attached_rot_0.json"),
                    "minecraft:block/template_attached_hanging_sign_rot_0",
                    allTexture
            );
            assertHangingSignModel(
                    models.resolve(woodTypeId + "_wall_hanging_sign.json"),
                    "minecraft:block/template_wall_hanging_sign",
                    allTexture
            );
        }
    }

    private static void assertHangingSignModel(Path model, String expectedParent, String expectedAll)
            throws Exception {
        assertTrue(Files.isRegularFile(model) && Files.size(model) > 0, "Missing hanging sign model: " + model);
        JSONObject json = new JSONObject(new JSONTokener(Files.readString(model)));
        assertEquals(expectedParent, json.optString("parent"), "Unexpected parent in " + model);
        JSONObject textures = json.getJSONObject("textures");
        assertEquals(expectedAll, textures.getString("all"), "Unexpected #all texture in " + model);
        assertTrue(
                textures.getString("particle").startsWith("dwm:block/stripped_"),
                "Hanging sign particle should be the stripped log in " + model
        );
    }

    /**
     * Minecraft 26.2 standing/wall signs bake wood from {@code textures/block/{id}_sign.png}.
     * Guards against Cardinal sharing a copied Ash atlas.
     */
    @Test
    public void standingSignBlockTexturesExistAndAreDistinct() throws Exception {
        assertWoodFamilyPngsExistAndAreDistinct(
                Path.of("src/client/resources/assets/dwm/textures/block"),
                "%s_sign.png",
                "Standing/wall sign block models expect assets/dwm/textures/block/%s_sign.png",
                "Standing sign block texture must be unique per wood type, but %s duplicates another family's atlas"
        );
    }

    /**
     * Fabric remaps modded wood types to {@code dwm:textures/gui/signs/<id>.png}
     * for standing-sign edit UI (not referenced from model JSON).
     */
    @Test
    public void standingSignGuiTexturesExistAndAreDistinct() throws Exception {
        assertWoodFamilyPngsExistAndAreDistinct(
                Path.of("src/client/resources/assets/dwm/textures/gui/signs"),
                "%s.png",
                "Fabric SignEditScreen expects assets/dwm/textures/gui/signs/%s.png for wood type dwm:%s",
                "Standing sign GUI texture must be unique per wood type, but %s duplicates another family's atlas"
        );
    }

    /**
     * Fabric remaps modded wood types to {@code dwm:textures/gui/hanging_signs/<id>.png}
     * for hanging-sign edit UI (not referenced from model JSON).
     */
    @Test
    public void hangingSignGuiTexturesExistAndAreDistinct() throws Exception {
        assertWoodFamilyPngsExistAndAreDistinct(
                Path.of("src/client/resources/assets/dwm/textures/gui/hanging_signs"),
                "%s.png",
                "Fabric HangingSignEditScreen expects assets/dwm/textures/gui/hanging_signs/%s.png for wood type dwm:%s",
                "Hanging sign GUI texture must be unique per wood type, but %s duplicates another family's atlas"
        );
    }

    /**
     * Inventory icons for standing signs must exist and not share a copied Ash sprite.
     */
    @Test
    public void standingSignItemTexturesExistAndAreDistinct() throws Exception {
        assertWoodFamilyPngsExistAndAreDistinct(
                Path.of("src/client/resources/assets/dwm/textures/item"),
                "%s_sign.png",
                "Standing sign items expect assets/dwm/textures/item/%s_sign.png",
                "Standing sign item texture must be unique per wood type, but %s duplicates another family's atlas"
        );
    }

    /**
     * Inventory icons for hanging signs must exist and not share a copied Ash sprite.
     */
    @Test
    public void hangingSignItemTexturesExistAndAreDistinct() throws Exception {
        assertWoodFamilyPngsExistAndAreDistinct(
                Path.of("src/client/resources/assets/dwm/textures/item"),
                "%s_hanging_sign.png",
                "Hanging sign items expect assets/dwm/textures/item/%s_hanging_sign.png",
                "Hanging sign item texture must be unique per wood type, but %s duplicates another family's atlas"
        );
    }

    private static void assertWoodFamilyPngsExistAndAreDistinct(
            Path directory,
            String filenameFormat,
            String missingTemplate,
            String duplicateTemplate
    ) throws Exception {
        Set<String> hashes = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (String woodTypeId : WOOD_TYPE_IDS) {
            Path texture = directory.resolve(filenameFormat.formatted(woodTypeId));
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    missingTemplate.formatted(woodTypeId, woodTypeId)
            );
            String hex = HexFormat.of().formatHex(sha256.digest(Files.readAllBytes(texture)));
            assertTrue(hashes.add(hex), duplicateTemplate.formatted(texture));
        }
    }

    /**
     * Datagen emits wood-family item defs under {@code src/main/generated/assets/dwm/items/}.
     * Guards against {@code pruneDatagenItemModels} deleting a newly registered family.
     */
    @Test
    public void generatedWoodFamilyItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        for (String woodTypeId : WOOD_TYPE_IDS) {
            for (String suffix : WOOD_ITEM_SUFFIXES) {
                Path item = itemsDir.resolve(woodTypeId + "_" + suffix + ".json");
                assertTrue(
                        Files.isRegularFile(item) && Files.size(item) > 0,
                        "Missing generated wood item model: " + item
                );
            }
            for (String stripped : List.of("stripped_" + woodTypeId + "_log", "stripped_" + woodTypeId + "_wood")) {
                Path item = itemsDir.resolve(stripped + ".json");
                assertTrue(
                        Files.isRegularFile(item) && Files.size(item) > 0,
                        "Missing generated wood item model: " + item
                );
            }
        }
    }

    /**
     * Custom wood doors use stacked 3D item models textured from {@code textures/block/{id}_door.png},
     * not {@code item/generated} sprites.
     */
    @Test
    public void customDoorItemModelsAre3dBlockTextured() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        Path modelsDir = Path.of("src/main/generated/assets/dwm/models");
        for (String woodTypeId : WOOD_TYPE_IDS) {
            Path itemDef = itemsDir.resolve(woodTypeId + "_door.json");
            assertTrue(Files.isRegularFile(itemDef), "Missing generated door item def: " + itemDef);
            JSONObject def = readJson(itemDef);
            String modelId = def.getJSONObject("model").getString("model");
            assertEquals("dwm:item/" + woodTypeId + "_door", modelId);

            Path wrapperPath = modelsDir.resolve("item/" + woodTypeId + "_door.json");
            JSONObject wrapper = readJson(wrapperPath);
            assertNotEquals("minecraft:item/generated", wrapper.optString("parent"));
            assertEquals("dwm:block/" + woodTypeId + "_door", wrapper.getJSONObject("textures").getString("door"));

            String parent = wrapper.getString("parent");
            assertTrue(parent.startsWith("dwm:item/template_"), "Door item should parent a stacked template: " + parent);
            Path templatePath = modelsDir.resolve(parent.substring("dwm:".length()) + ".json");
            JSONObject template = readJson(templatePath);
            assertEquals("front", template.getString("gui_light"));
            assertTrue(template.has("elements"));
            JSONObject display = template.getJSONObject("display");
            assertFalse(display.has("thirdperson_lefthand"));
            for (String key : List.of(
                    "gui", "fixed", "ground", "thirdperson_righthand", "firstperson_righthand", "firstperson_lefthand"
            )) {
                assertTrue(display.has(key), "Missing display context " + key + " in " + templatePath);
            }
        }
    }

    /**
     * Sonic screwdrivers use a 16×16 GUI sprite via {@code display_context}, with the
     * thin 3D mesh as fallback for hands / ground / frames. UV atlases must not be
     * reused as inventory icons.
     */
    @Test
    public void sonicItemModelsUseGuiSprites() throws Exception {
        String[] ids = {
                "sonic_second_doctor",
                "sonic_third_doctor",
                "sonic_fourth_doctor",
                "sonic_fifth_doctor",
        };
        Set<String> guiHashes = new HashSet<>();
        Set<String> atlasHashes = new HashSet<>();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        Path textures = Path.of("src/client/resources/assets/dwm/textures/item");
        for (String id : ids) {
            Path itemDef = Path.of("src/client/resources/assets/dwm/items/" + id + ".json");
            JSONObject def = readJson(itemDef);
            JSONObject model = def.getJSONObject("model");
            assertEquals("minecraft:select", model.getString("type"), id);
            assertEquals("minecraft:display_context", model.getString("property"), id);

            var cases = model.getJSONArray("cases");
            assertEquals(1, cases.length(), id + " should have one display_context case");
            JSONObject guiCase = cases.getJSONObject(0);
            assertEquals("gui", guiCase.getString("when"), id);
            assertEquals(
                    "dwm:item/" + id + "_gui",
                    guiCase.getJSONObject("model").getString("model"),
                    id
            );
            JSONObject fallback = model.getJSONObject("fallback");
            assertEquals("minecraft:model", fallback.getString("type"), id);
            assertEquals("dwm:item/" + id, fallback.getString("model"), id);

            Path guiModelPath = Path.of("src/client/resources/assets/dwm/models/item/" + id + "_gui.json");
            JSONObject guiModel = readJson(guiModelPath);
            assertEquals("minecraft:item/generated", guiModel.getString("parent"), id);
            assertEquals(
                    "dwm:item/" + id + "_gui",
                    guiModel.getJSONObject("textures").getString("layer0"),
                    id
            );

            Path guiPng = textures.resolve(id + "_gui.png");
            assertTrue(Files.isRegularFile(guiPng) && Files.size(guiPng) > 0, "Missing GUI sprite: " + guiPng);
            BufferedImage image = ImageIO.read(guiPng.toFile());
            assertEquals(16, image.getWidth(), "GUI sprite width for " + id);
            assertEquals(16, image.getHeight(), "GUI sprite height for " + id);
            String guiHex = HexFormat.of().formatHex(sha256.digest(Files.readAllBytes(guiPng)));
            assertTrue(guiHashes.add(guiHex), "GUI sprite must be unique, but " + guiPng + " duplicates another");

            Path atlasPng = textures.resolve(id + ".png");
            assertTrue(Files.isRegularFile(atlasPng) && Files.size(atlasPng) > 0, "Missing 3D UV atlas: " + atlasPng);
            String atlasHex = HexFormat.of().formatHex(sha256.digest(Files.readAllBytes(atlasPng)));
            atlasHashes.add(atlasHex);
            assertNotEquals(guiHex, atlasHex, id + " GUI sprite must not be a copy of the 3D UV atlas");
        }
        for (String guiHex : guiHashes) {
            assertFalse(atlasHashes.contains(guiHex), "GUI sprite hash collides with a sonic UV atlas");
        }
    }

    /**
     * Globe is a BER EntityModel prop; inventory must use the special renderer, not a
     * {@code minecraft:model} sprite pointing at the missing block atlas.
     */
    @Test
    public void tardisGlobeItemModelIsSpecialRenderer() throws Exception {
        assertSpecialItemRenderer(
                "tardis_globe",
                "dwm:entity/tardis_globe"
        );
    }

    /**
     * First Doctor console is a BER EntityModel; inventory must use the special renderer
     * instead of a flat wall-sprite placeholder.
     */
    @Test
    public void firstDoctorConsoleItemModelIsSpecialRenderer() throws Exception {
        assertSpecialItemRenderer(
                "first_doctor_console",
                "dwm:entity/first_white_base_console"
        );
    }

    /**
     * TARDIS exterior is a BER EntityModel; inventory must use the special renderer
     * instead of a flat item sprite.
     */
    @Test
    public void tardisBlockItemModelIsSpecialRenderer() throws Exception {
        assertSpecialItemRenderer(
                "tardis_block",
                "dwm:entity/first_doctor_box"
        );
    }

    private static void assertSpecialItemRenderer(String id, String expectedParticle) throws Exception {
        Path itemDef = Path.of("src/client/resources/assets/dwm/items/" + id + ".json");
        JSONObject def = readJson(itemDef);
        JSONObject model = def.getJSONObject("model");
        assertEquals("minecraft:special", model.getString("type"));
        assertEquals("dwm:item/" + id, model.getString("base"));
        assertEquals("dwm:" + id, model.getJSONObject("model").getString("type"));

        Path baseModel = Path.of("src/client/resources/assets/dwm/models/item/" + id + ".json");
        JSONObject base = readJson(baseModel);
        assertEquals("minecraft:builtin/entity", base.getString("parent"));
        assertEquals("side", base.getString("gui_light"));
        assertEquals(expectedParticle, base.getJSONObject("textures").getString("particle"));
        assertFalse(base.getJSONObject("display").has("thirdperson_lefthand"));
    }

    private static JSONObject readJson(Path path) throws Exception {
        return new JSONObject(new JSONTokener(Files.readString(path)));
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping orange sand family item defs
     * (allowlist must include {@code orange_sand} substring).
     */
    @Test
    public void generatedOrangeSandFamilyItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "orange_sand",
                "orange_sandstone",
                "orange_sandstone_stairs",
                "orange_sandstone_slab",
                "orange_sandstone_wall",
                "cut_orange_sandstone",
                "cut_orange_sandstone_slab",
                "chiseled_orange_sandstone",
                "smooth_orange_sandstone",
                "smooth_orange_sandstone_stairs",
                "smooth_orange_sandstone_slab",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated orange sand item model: " + item
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping Gallifrey plant item defs
     * (IDs lack a shared {@code gallifrey} substring).
     */
    @Test
    public void generatedGallifreyPlantItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "flower_of_remembrance",
                "moonlight_bloom",
                "saccharine_cane",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated Gallifrey plant item model: " + item
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping azbantium item defs
     * (allowlist must include {@code azbantium} substring).
     */
    @Test
    public void generatedAzbantiumItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "azbantium_ore",
                "azbantium_block",
                "azbantium",
                "azbantium_sword",
                "azbantium_shovel",
                "azbantium_pickaxe",
                "azbantium_axe",
                "azbantium_hoe",
                "azbantium_helmet",
                "azbantium_chestplate",
                "azbantium_leggings",
                "azbantium_boots",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated azbantium item model: " + item
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping zeiton/ferrite item defs
     * (allowlist must include {@code zeiton} and {@code ferrite} substrings).
     */
    @Test
    public void generatedZeitonItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "zeiton_ore",
                "zeiton_crystals",
                "zeiton_powder",
                "ferrite_powder",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated zeiton item model: " + item
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Stattenheim remote item def
     * (allowlist must include {@code stattenheim} substring).
     */
    @Test
    public void generatedStattenheimRemoteItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/stattenheim_remote.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Stattenheim remote item model: " + item
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping circuit item defs
     * (allowlist must include {@code circuit} substring).
     */
    @Test
    public void generatedCircuitItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "circuit_stabilisers",
                "circuit_waypoints",
                "circuit_fast_return",
                "circuit_coordinate_locks",
                "circuit_planet_locator",
                "circuit_telepathic",
                "circuit_cloak",
                "circuit_chameleon",
                "circuit_remote_summon",
                "circuit_player_locator",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated circuit item model: " + item
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping Gallifrey vanilla ore item defs
     * (allowlist must include {@code gallifrey} substring).
     */
    @Test
    public void generatedGallifreyVanillaOreItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "gallifrey_coal_ore",
                "gallifrey_iron_ore",
                "gallifrey_gold_ore",
                "gallifrey_diamond_ore",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated Gallifrey vanilla ore item model: " + item
            );
        }
    }

    @Test
    public void broakirEntityTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/broakir.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "BroakirRenderer expects assets/dwm/textures/entity/broakir.png"
        );
    }

    @Test
    public void broakirSpawnEggTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/item/broakir_spawn_egg.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Spawn egg item model expects assets/dwm/textures/item/broakir_spawn_egg.png"
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Broakir spawn egg item def
     * (allowlist must include {@code broakir} substring).
     */
    @Test
    public void generatedBroakirSpawnEggItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/broakir_spawn_egg.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Broakir spawn egg item model: " + item
        );
    }

    @Test
    public void gallifreyForestAndPlainsSpawnBroakir() throws Exception {
        assertTrue(biomeHasCreatureSpawn("gallifrey_forest.json", "dwm:broakir"));
        assertTrue(biomeHasCreatureSpawn("gallifrey_plains.json", "dwm:broakir"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_wastes.json", "dwm:broakir"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_badlands.json", "dwm:broakir"));
    }

    @Test
    public void flutterwingEntityTexturesExist() throws Exception {
        String[] variants = {"blue_crystal", "madrigal", "silverband", "wild_endeavour"};
        for (String variant : variants) {
            Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/flutterwing/" + variant + ".png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "FlutterwingRenderer expects assets/dwm/textures/entity/flutterwing/" + variant + ".png"
            );
        }
    }

    @Test
    public void flutterwingSpawnEggTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/item/flutterwing_spawn_egg.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Spawn egg item model expects assets/dwm/textures/item/flutterwing_spawn_egg.png"
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Flutterwing spawn egg item def
     * (allowlist must include {@code flutterwing} substring).
     */
    @Test
    public void generatedFlutterwingSpawnEggItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/flutterwing_spawn_egg.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Flutterwing spawn egg item model: " + item
        );
    }

    @Test
    public void gallifreyForestAndPlainsSpawnFlutterwing() throws Exception {
        assertTrue(biomeHasCreatureSpawn("gallifrey_forest.json", "dwm:flutterwing"));
        assertTrue(biomeHasCreatureSpawn("gallifrey_plains.json", "dwm:flutterwing"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_wastes.json", "dwm:flutterwing"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_badlands.json", "dwm:flutterwing"));
    }

    @Test
    public void flutterwingIsFallDamageImmune() throws Exception {
        Path path = Path.of("src/main/generated/data/minecraft/tags/entity_type/fall_damage_immune.json");
        assertTrue(Files.isRegularFile(path), "Missing generated fall_damage_immune entity tag: " + path);
        JSONObject tag = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        var values = tag.getJSONArray("values");
        boolean found = false;
        for (int i = 0; i < values.length(); i++) {
            if ("dwm:flutterwing".equals(values.getString(i))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "fall_damage_immune should include dwm:flutterwing");
    }

    /**
     * Minecraft 26.2 prepends {@code textures/} and appends {@code .png} to advancement
     * background IDs. The Doctor Who root must use {@code dwm:block/gallifrey_stone}, not
     * the pre-26.2 {@code textures/...png} form that resolves to a doubled path.
     */
    @Test
    public void doctorWhoAdvancementBackgroundUsesModernTextureId() throws Exception {
        Path rootAdvancement = Path.of("src/main/generated/data/minecraft/advancement/dwm/root.json");
        assertTrue(Files.isRegularFile(rootAdvancement), "Missing generated root advancement: " + rootAdvancement);
        JSONObject root = new JSONObject(new JSONTokener(Files.newBufferedReader(rootAdvancement)));
        String background = root.getJSONObject("display").getString("background");
        assertEquals("dwm:block/gallifrey_stone", background);
        assertFalse(background.contains("textures/"), "background must not include textures/ prefix: " + background);
        assertFalse(background.endsWith(".png"), "background must not include .png suffix: " + background);

        Path texture = Path.of("src/client/resources/assets/dwm/textures/block/gallifrey_stone.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Advancement tab background expects " + texture
        );
    }

    /**
     * Parent obtain-sonic and child Knock Knock must use distinct icons so adjacent
     * Doctor Who tab tiles are not visually identical.
     */
    @Test
    public void sonicAdvancementIconsAreDistinct() throws Exception {
        Path obtainSonic = Path.of("src/main/generated/data/minecraft/advancement/dwm/sonic_screwdriver.json");
        Path knockKnock = Path.of("src/main/generated/data/minecraft/advancement/dwm/sonic_iron_door.json");
        assertTrue(Files.isRegularFile(obtainSonic), "Missing generated advancement: " + obtainSonic);
        assertTrue(Files.isRegularFile(knockKnock), "Missing generated advancement: " + knockKnock);

        String obtainIcon = new JSONObject(new JSONTokener(Files.newBufferedReader(obtainSonic)))
                .getJSONObject("display").getJSONObject("icon").getString("id");
        String knockIcon = new JSONObject(new JSONTokener(Files.newBufferedReader(knockKnock)))
                .getJSONObject("display").getJSONObject("icon").getString("id");

        assertEquals("dwm:sonic_third_doctor", obtainIcon);
        assertEquals("minecraft:iron_door", knockIcon);
        assertNotEquals(obtainIcon, knockIcon);
    }

    @Test
    public void flutterwingSoundFilesExist() throws Exception {
        String[] names = {"ambient", "ambient_2", "hurt", "death"};
        for (String name : names) {
            Path sound = Path.of("src/client/resources/assets/dwm/sounds/entity/flutterwing/" + name + ".ogg");
            assertTrue(
                    Files.isRegularFile(sound) && Files.size(sound) > 0,
                    "Flutterwing sound event expects " + sound
            );
        }
        byte[] ambient = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/flutterwing/ambient.ogg"));
        byte[] hurt = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/flutterwing/hurt.ogg"));
        byte[] death = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/flutterwing/death.ogg"));
        assertFalse(Arrays.equals(ambient, hurt), "ambient and hurt clips should differ");
        assertFalse(Arrays.equals(hurt, death), "hurt and death clips should differ");
        assertFalse(Arrays.equals(ambient, death), "ambient and death clips should differ");
    }

    @Test
    public void flutterwingSoundsAreCustomNotBeeAliases() throws Exception {
        Path path = Path.of("src/main/resources/assets/dwm/sounds.json");
        JSONObject sounds = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        String[] events = {
                "entity.flutterwing.ambient",
                "entity.flutterwing.hurt",
                "entity.flutterwing.death"
        };
        String json = Files.readString(path);
        assertFalse(json.contains("minecraft:entity.bee"), "Flutterwing must not alias bee sounds");
        for (String event : events) {
            assertTrue(sounds.has(event), "Missing sound event: " + event);
            var entries = sounds.getJSONObject(event).getJSONArray("sounds");
            assertTrue(entries.length() > 0, event + " should list at least one clip");
            for (int i = 0; i < entries.length(); i++) {
                String name = entries.getJSONObject(i).getString("name");
                assertTrue(name.startsWith("dwm:entity/flutterwing/"), event + " should use a custom dwm clip, got " + name);
            }
        }
    }

    @Test
    public void mewingDogEntityTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/mewing_dog.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "MewingDogRenderer expects assets/dwm/textures/entity/mewing_dog.png"
        );
    }

    @Test
    public void mewingDogSpawnEggTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/item/mewing_dog_spawn_egg.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Spawn egg item model expects assets/dwm/textures/item/mewing_dog_spawn_egg.png"
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Mewing Dog spawn egg item def
     * (allowlist must include {@code mewing_dog} substring).
     */
    @Test
    public void generatedMewingDogSpawnEggItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/mewing_dog_spawn_egg.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Mewing Dog spawn egg item model: " + item
        );
    }

    @Test
    public void gallifreyForestOnlySpawnsMewingDog() throws Exception {
        assertTrue(biomeHasCreatureSpawn("gallifrey_forest.json", "dwm:mewing_dog"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_plains.json", "dwm:mewing_dog"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_wastes.json", "dwm:mewing_dog"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_badlands.json", "dwm:mewing_dog"));
    }

    @Test
    public void timeLordEntityTexturesExist() throws Exception {
        for (int i = 1; i <= 4; i++) {
            Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/time_lord_" + i + ".png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "TimeLordRenderer expects assets/dwm/textures/entity/time_lord_" + i + ".png"
            );
        }
    }

    @Test
    public void timeLordSpawnEggTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/item/time_lord_spawn_egg.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Spawn egg item model expects assets/dwm/textures/item/time_lord_spawn_egg.png"
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Time Lord spawn egg item def
     * (allowlist must include {@code time_lord} substring).
     */
    @Test
    public void generatedTimeLordSpawnEggItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/time_lord_spawn_egg.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Time Lord spawn egg item model: " + item
        );
    }

    @Test
    public void gallifreyForestAndPlainsSpawnTimeLord() throws Exception {
        assertTrue(biomeHasCreatureSpawn("gallifrey_forest.json", "dwm:time_lord"));
        assertTrue(biomeHasCreatureSpawn("gallifrey_plains.json", "dwm:time_lord"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_wastes.json", "dwm:time_lord"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_badlands.json", "dwm:time_lord"));
    }

    @Test
    public void dalekLootTableDropsDalekaniumIngot() throws Exception {
        Path loot = Path.of("src/main/generated/data/dwm/loot_table/entities/dalek.json");
        assertTrue(Files.isRegularFile(loot), "Missing Dalek entity loot table: " + loot);
        String json = Files.readString(loot);
        assertTrue(
                json.contains("dwm:dalekanium_ingot"),
                "Dalek loot should drop dalekanium_ingot, got: " + json
        );
    }

    @Test
    public void dalekEntityTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/dalek/1963.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "DalekRenderer expects assets/dwm/textures/entity/dalek/1963.png"
        );
        BufferedImage image = ImageIO.read(texture.toFile());
        assertEquals(64, image.getWidth(), "Dalek atlas width");
        assertEquals(64, image.getHeight(), "Dalek atlas height");
        // New UV slots from DalekModel texOffs / fix_dalek_less.py SWATCHES.
        assertRgb(image, 1, 1, 158, 160, 156, "casing (0,0)");
        assertRgb(image, 1, 17, 22, 22, 22, "band (0,16)");
        assertRgb(image, 37, 17, 48, 50, 54, "gun (36,16)");
        assertRgb(image, 1, 33, 28, 28, 30, "plunger (0,32)");
        assertRgb(image, 17, 33, 18, 18, 20, "eyestalk (16,32)");
        assertRgb(image, 32, 32, 232, 236, 240, "lens (32,32)");
        assertRgb(image, 41, 33, 214, 214, 210, "stud (40,32)");
        assertRgb(image, 49, 33, 36, 176, 196, "light (48,32)");
        // Old layout put the black band at (32,0); that cell is now casing silver.
        assertRgb(image, 40, 8, 158, 160, 156, "former band cell is now casing");
    }

    private static void assertRgb(
            BufferedImage image,
            int x,
            int y,
            int red,
            int green,
            int blue,
            String slot
    ) {
        int argb = image.getRGB(x, y);
        assertEquals(255, (argb >>> 24) & 0xFF, slot + " alpha");
        assertEquals(red, (argb >> 16) & 0xFF, slot + " red");
        assertEquals(green, (argb >> 8) & 0xFF, slot + " green");
        assertEquals(blue, argb & 0xFF, slot + " blue");
    }

    @Test
    public void dalekSpawnEggTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/item/dalek_spawn_egg.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Spawn egg item model expects assets/dwm/textures/item/dalek_spawn_egg.png"
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Dalek spawn egg item def
     * (allowlist must include {@code dalek} substring).
     */
    @Test
    public void generatedDalekSpawnEggItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/dalek_spawn_egg.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Dalek spawn egg item model: " + item
        );
    }

    @Test
    public void gallifreyBiomesDoNotSpawnDalek() throws Exception {
        for (String biome : new String[]{
                "gallifrey_forest.json",
                "gallifrey_plains.json",
                "gallifrey_wastes.json",
                "gallifrey_badlands.json"
        }) {
            assertFalse(biomeHasCreatureSpawn(biome, "dwm:dalek"));
            assertFalse(biomeHasMonsterSpawn(biome, "dwm:dalek"));
        }
    }

    @Test
    public void dalekIsFallDamageImmune() throws Exception {
        Path path = Path.of("src/main/generated/data/minecraft/tags/entity_type/fall_damage_immune.json");
        assertTrue(Files.isRegularFile(path), "Missing generated fall_damage_immune entity tag: " + path);
        JSONObject tag = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        var values = tag.getJSONArray("values");
        boolean found = false;
        for (int i = 0; i < values.length(); i++) {
            if ("dwm:dalek".equals(values.getString(i))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "fall_damage_immune should include dwm:dalek");
    }

    @Test
    public void dalekSoundFilesExist() throws Exception {
        String[] names = {"ambient", "hurt", "death", "shoot"};
        for (String name : names) {
            Path sound = Path.of("src/client/resources/assets/dwm/sounds/entity/dalek/" + name + ".ogg");
            assertTrue(
                    Files.isRegularFile(sound) && Files.size(sound) > 0,
                    "Dalek sound event expects " + sound
            );
        }
        byte[] ambient = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/dalek/ambient.ogg"));
        byte[] hurt = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/dalek/hurt.ogg"));
        byte[] death = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/dalek/death.ogg"));
        byte[] shoot = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/dalek/shoot.ogg"));
        assertFalse(Arrays.equals(ambient, hurt), "ambient and hurt clips should differ");
        assertFalse(Arrays.equals(hurt, death), "hurt and death clips should differ");
        assertFalse(Arrays.equals(ambient, death), "ambient and death clips should differ");
        assertFalse(Arrays.equals(shoot, ambient), "shoot and ambient clips should differ");
    }

    @Test
    public void dalekSoundsAreCustomNotVanillaAliases() throws Exception {
        Path path = Path.of("src/main/resources/assets/dwm/sounds.json");
        JSONObject sounds = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        String[] events = {
                "entity.dalek.ambient",
                "entity.dalek.hurt",
                "entity.dalek.death",
                "entity.dalek.shoot"
        };
        for (String event : events) {
            assertTrue(sounds.has(event), "Missing sound event: " + event);
            var entries = sounds.getJSONObject(event).getJSONArray("sounds");
            assertTrue(entries.length() > 0, event + " should list at least one clip");
            for (int i = 0; i < entries.length(); i++) {
                String name = entries.getJSONObject(i).getString("name");
                assertTrue(name.startsWith("dwm:entity/dalek/"), event + " should use a custom dwm clip, got " + name);
            }
        }
    }

    @Test
    public void mewingDogAmbientSoundFilesExist() throws Exception {
        for (String name : new String[]{"ambient", "ambient_2"}) {
            Path sound = Path.of("src/client/resources/assets/dwm/sounds/entity/mewing_dog/" + name + ".ogg");
            assertTrue(
                    Files.isRegularFile(sound) && Files.size(sound) > 0,
                    "Mewing Dog ambient expects " + sound
            );
        }
        byte[] a = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/mewing_dog/ambient.ogg"));
        byte[] b = Files.readAllBytes(Path.of("src/client/resources/assets/dwm/sounds/entity/mewing_dog/ambient_2.ogg"));
        assertFalse(Arrays.equals(a, b), "ambient and ambient_2 clips should differ");
    }

    @Test
    public void mewingDogAmbientIsCustomNotWolfAlias() throws Exception {
        Path path = Path.of("src/main/resources/assets/dwm/sounds.json");
        JSONObject sounds = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        assertTrue(sounds.has("entity.mewing_dog.ambient"), "Missing entity.mewing_dog.ambient");
        var entries = sounds.getJSONObject("entity.mewing_dog.ambient").getJSONArray("sounds");
        assertTrue(entries.length() > 0, "ambient should list at least one clip");
        for (int i = 0; i < entries.length(); i++) {
            String name = entries.getJSONObject(i).getString("name");
            assertTrue(
                    name.startsWith("dwm:entity/mewing_dog/"),
                    "ambient should use a custom dwm clip, got " + name
            );
            assertFalse(name.contains("minecraft:entity.wolf"), "ambient must not alias wolf sounds");
        }
    }

    @Test
    public void skaroBiomesExistWithFiveIdContractAndNoGallifreyContent() throws Exception {
        String[] biomeFiles = {
                "skaro_irradiated_wastes.json",
                "skaro_petrified_jungle.json",
                "skaro_drammankin_mire.json",
                "skaro_drammankin_mountains.json",
                "skaro_thal_plateau.json"
        };
        Path biomeDir = Path.of("src/main/generated/data/dwm/worldgen/biome");
        Path tag = Path.of("src/main/resources/data/dwm/tags/worldgen/biome/is_skaro.json");
        assertTrue(Files.isRegularFile(tag), "Missing hand-maintained #dwm:is_skaro tag: " + tag);
        JSONObject tagJson = new JSONObject(new JSONTokener(Files.newBufferedReader(tag)));
        var values = tagJson.getJSONArray("values");
        assertEquals(5, values.length(), "is_skaro must list exactly five biomes");
        Set<String> tagged = new HashSet<>();
        for (int i = 0; i < values.length(); i++) {
            tagged.add(values.getString(i));
        }
        assertEquals(
                Set.of(
                        "dwm:skaro_irradiated_wastes",
                        "dwm:skaro_petrified_jungle",
                        "dwm:skaro_drammankin_mire",
                        "dwm:skaro_drammankin_mountains",
                        "dwm:skaro_thal_plateau"
                ),
                tagged
        );

        String[] forbiddenFeatureSubstrings = {
                "dwm:gallifrey",
                "dwm:azbantium",
                "dwm:zeiton",
                "dwm:ash",
                "dwm:dark_ash",
                "dwm:cardinal",
                "dwm:saccharine"
        };
        String[] spawnCategories = {
                "ambient",
                "axolotls",
                "creature",
                "misc",
                "monster",
                "underground_water_creature",
                "water_ambient",
                "water_creature"
        };

        for (String biomeFile : biomeFiles) {
            Path path = biomeDir.resolve(biomeFile);
            assertTrue(Files.isRegularFile(path), "Missing generated Skaro biome: " + path);
            JSONObject biome = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
            var spawners = biome.getJSONObject("spawners");
            for (String category : spawnCategories) {
                assertEquals(
                        0,
                        spawners.getJSONArray(category).length(),
                        biomeFile + " must have empty spawners." + category
                );
            }
            String blob = Files.readString(path);
            for (String forbidden : forbiddenFeatureSubstrings) {
                assertFalse(
                        blob.contains(forbidden),
                        biomeFile + " must not include Gallifrey-only generated content: " + forbidden
                );
            }
            if ("skaro_petrified_jungle.json".equals(biomeFile)) {
                assertTrue(
                        blob.contains("dwm:petrified_jungle_trees"),
                        biomeFile + " must include petrified jungle trees"
                );
                assertTrue(
                        blob.contains("dwm:petrified_jungle_snags"),
                        biomeFile + " must include petrified jungle snags"
                );
                assertTrue(
                        blob.contains("dwm:fallen_petrified_jungle_trees"),
                        biomeFile + " must include fallen petrified jungle trees"
                );
            } else {
                assertFalse(
                        blob.contains("dwm:petrified_jungle_trees")
                                || blob.contains("dwm:petrified_jungle_snags")
                                || blob.contains("dwm:fallen_petrified_jungle_trees"),
                        biomeFile + " must not include petrified jungle vegetation"
                );
            }
        }

        assertPetrifiedTreeConfiguredFeaturesAreLogOnly();

        Path noise = Path.of("src/main/generated/data/dwm/worldgen/noise_settings/skaro.json");
        assertTrue(Files.isRegularFile(noise) && Files.size(noise) > 0, "Missing generated Skaro noise settings: " + noise);
        assertTrue(
                Files.isRegularFile(Path.of("src/main/resources/data/dwm/dimension/skaro.json")),
                "Missing hand-maintained dimension/skaro.json"
        );
        assertTrue(
                Files.isRegularFile(Path.of("src/main/resources/data/dwm/dimension_type/skaro.json")),
                "Missing hand-maintained dimension_type/skaro.json"
        );
    }

    private static void assertPetrifiedTreeConfiguredFeaturesAreLogOnly() throws Exception {
        Path configuredDir = Path.of("src/main/generated/data/dwm/worldgen/configured_feature");
        String[] petrifiedFeatures = {
                "petrified_tree.json",
                "petrified_snag.json",
                "fallen_petrified_tree.json"
        };
        for (String featureFile : petrifiedFeatures) {
            Path path = configuredDir.resolve(featureFile);
            assertTrue(Files.isRegularFile(path), "Missing generated configured feature: " + path);
            String blob = Files.readString(path);
            assertTrue(blob.contains("dwm:petrified_log"), featureFile + " must use petrified_log");
            assertFalse(blob.contains("leaves"), featureFile + " must not reference leaves");
            assertFalse(blob.contains("sapling"), featureFile + " must not reference saplings");
        }
    }

    private static boolean biomeHasCreatureSpawn(String biomeFile, String entityId) throws Exception {
        Path path = Path.of("src/main/generated/data/dwm/worldgen/biome").resolve(biomeFile);
        assertTrue(Files.isRegularFile(path), "Missing generated biome: " + path);
        JSONObject biome = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        var creatures = biome.getJSONObject("spawners").getJSONArray("creature");
        for (int i = 0; i < creatures.length(); i++) {
            if (entityId.equals(creatures.getJSONObject(i).getString("type"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean biomeHasMonsterSpawn(String biomeFile, String entityId) throws Exception {
        Path path = Path.of("src/main/generated/data/dwm/worldgen/biome").resolve(biomeFile);
        assertTrue(Files.isRegularFile(path), "Missing generated biome: " + path);
        JSONObject biome = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        var monsters = biome.getJSONObject("spawners").getJSONArray("monster");
        for (int i = 0; i < monsters.length(); i++) {
            if (entityId.equals(monsters.getJSONObject(i).getString("type"))) {
                return true;
            }
        }
        return false;
    }
}
