package com.adamkali.dwm;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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
     * Globe is a BER EntityModel prop; inventory must use the special renderer, not a
     * {@code minecraft:model} sprite pointing at the missing block atlas.
     */
    @Test
    public void tardisGlobeItemModelIsSpecialRenderer() throws Exception {
        Path itemDef = Path.of("src/client/resources/assets/dwm/items/tardis_globe.json");
        JSONObject def = readJson(itemDef);
        JSONObject model = def.getJSONObject("model");
        assertEquals("minecraft:special", model.getString("type"));
        assertEquals("dwm:item/tardis_globe", model.getString("base"));
        assertEquals("dwm:tardis_globe", model.getJSONObject("model").getString("type"));

        Path baseModel = Path.of("src/client/resources/assets/dwm/models/item/tardis_globe.json");
        JSONObject base = readJson(baseModel);
        assertEquals("minecraft:builtin/entity", base.getString("parent"));
        assertEquals("side", base.getString("gui_light"));
        assertEquals("dwm:entity/tardis_globe", base.getJSONObject("textures").getString("particle"));
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
}
