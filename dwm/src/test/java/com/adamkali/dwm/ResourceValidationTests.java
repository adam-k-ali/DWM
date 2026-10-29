package com.adamkali.dwm;

import com.adamkali.dwm.entity.DalekPatrolLogic;
import com.adamkali.dwm.entity.DalekVariant;
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

    @Test
    public void validateEntityDefinitions() {
        assertTrue(JsonValidationHelpers.validateJsonFiles(
                "src/test/resources/schemas/entity_definition.schema.json",
                "src/main/resources/data/dwm/entity/definition"
        ));
    }

    @Test
    public void validateEntityModels() {
        assertTrue(JsonValidationHelpers.validateJsonFiles(
                "src/test/resources/schemas/entity_model.schema.json",
                "src/client/resources/assets/dwm/models/entity"
        ));
    }

    /**
     * JSON entity meshes name their atlas with {@code texture} (Java Identifier form,
     * including {@code textures/} and {@code .png}), not a vanilla {@code textures} map.
     */
    @Test
    public void entityModelTexturesExist() throws Exception {
        Path models = Path.of("src/client/resources/assets/dwm/models/entity");
        Path assetsRoot = Path.of("src/client/resources/assets");
        assertTrue(Files.isDirectory(models), "Missing entity models directory: " + models);
        int checked = 0;
        try (var paths = Files.walk(models).filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))) {
            for (Path modelPath : (Iterable<Path>) paths::iterator) {
                JSONObject json = new JSONObject(new JSONTokener(Files.readString(modelPath)));
                if (!json.has("texture")) {
                    assertTrue(json.has("parent"), "Entity model needs texture or parent: " + modelPath);
                    continue;
                }
                if (!(json.get("texture") instanceof String textureId)) {
                    fail("Entity model texture must be a string in " + modelPath);
                    continue;
                }
                checked++;
                int colon = textureId.indexOf(':');
                if (colon < 0) {
                    fail("Entity model texture is not namespaced: " + textureId + " in " + modelPath);
                }
                String namespace = textureId.substring(0, colon);
                String path = textureId.substring(colon + 1);
                Path expected = assetsRoot.resolve(namespace).resolve(path);
                assertTrue(
                        Files.isRegularFile(expected) && Files.size(expected) > 0,
                        "Missing entity model texture " + textureId + " -> " + expected
                                + " referenced by " + modelPath
                );
            }
        }
        assertTrue(checked > 0, "No entity model declared a texture under " + models);
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

    /**
     * The handheld meter uses a component-aware special renderer for its live screen.
     */
    @Test
    public void radiationMeterItemModelIsSpecialRenderer() throws Exception {
        assertSpecialItemRenderer(
                "radiation_meter",
                "dwm:item/radiation_meter"
        );

        BufferedImage body = ImageIO.read(Path.of(
                "src/client/resources/assets/dwm/textures/entity/radiation_meter.png"
        ).toFile());
        assertEquals(64, body.getWidth());
        assertEquals(64, body.getHeight());

        BufferedImage digits = ImageIO.read(Path.of(
                "src/client/resources/assets/dwm/textures/entity/radiation_meter_digits.png"
        ).toFile());
        assertEquals(64, digits.getWidth());
        assertEquals(8, digits.getHeight());
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
     * Guards against {@code pruneDatagenItemModels} dropping EVA-suit item defs.
     */
    @Test
    public void generatedEvaSuitItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "eva_suit_helmet",
                "eva_suit_chestplate",
                "eva_suit_leggings",
                "eva_suit_boots",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated EVA-suit item model: " + item
            );
        }
    }

    /**
     * EVA suit worn atlases are 128×64 fabric maps; inventory icons are 16×16 with
     * transparent corners, fully opaque pixels, and a tight colour budget.
     */
    @Test
    public void evaSuitTexturesMatchContract() throws Exception {
        Path outer = Path.of(
                "src/client/resources/assets/dwm/textures/entity/equipment/humanoid/eva_suit.png"
        );
        Path leggings = Path.of(
                "src/client/resources/assets/dwm/textures/entity/equipment/humanoid_leggings/eva_suit.png"
        );
        assertAtlasSize(outer, 128, 64);
        assertAtlasSize(leggings, 128, 64);

        String[] itemIds = {
                "eva_suit_helmet",
                "eva_suit_chestplate",
                "eva_suit_leggings",
                "eva_suit_boots",
        };
        for (String id : itemIds) {
            Path png = Path.of("src/client/resources/assets/dwm/textures/item/" + id + ".png");
            assertTrue(Files.isRegularFile(png) && Files.size(png) > 0, "Missing item sprite: " + png);
            BufferedImage image = ImageIO.read(png.toFile());
            assertEquals(16, image.getWidth(), id + " width");
            assertEquals(16, image.getHeight(), id + " height");
            assertEquals(0, (image.getRGB(0, 0) >>> 24) & 0xFF, id + " top-left alpha");
            assertEquals(0, (image.getRGB(15, 0) >>> 24) & 0xFF, id + " top-right alpha");
            assertEquals(0, (image.getRGB(0, 15) >>> 24) & 0xFF, id + " bottom-left alpha");
            assertEquals(0, (image.getRGB(15, 15) >>> 24) & 0xFF, id + " bottom-right alpha");

            Set<Integer> opaqueRgb = new HashSet<>();
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    if (alpha == 0) {
                        continue;
                    }
                    assertEquals(255, alpha, id + " semi-transparent pixel at " + x + "," + y);
                    opaqueRgb.add(argb & 0xFFFFFF);
                }
            }
            assertFalse(opaqueRgb.isEmpty(), id + " has no opaque pixels");
            assertTrue(
                    opaqueRgb.size() <= 12,
                    id + " exceeds colour budget: " + opaqueRgb.size()
            );
        }
    }

    @Test
    public void evaSuitOverlayTextureMatchesContract() throws Exception {
        Path png = Path.of("src/client/resources/assets/dwm/textures/misc/eva_suit_overlay.png");
        assertAtlasSize(png, 256, 256);
        BufferedImage image = ImageIO.read(png.toFile());
        assertEquals(0, (image.getRGB(128, 128) >>> 24) & 0xFF, "overlay center must be transparent");
        boolean edgeHasAlpha = false;
        int[][] edgePixels = {{0, 0}, {255, 0}, {0, 255}, {255, 255}, {128, 0}, {0, 128}};
        for (int[] pixel : edgePixels) {
            if (((image.getRGB(pixel[0], pixel[1]) >>> 24) & 0xFF) > 0) {
                edgeHasAlpha = true;
                break;
            }
        }
        assertTrue(edgeHasAlpha, "overlay frame must have opaque edge pixels");
    }

    /**
     * The radiation fungus is a 3D toadstool cluster, not a cross: the generated model parents the
     * hand-authored template, whose boxes stay inside the block's outline (12 wide, 10 tall) and whose
     * gill planes glow.
     */
    @Test
    public void radiationFungusUsesToadstoolTemplate() throws Exception {
        JSONObject model = new JSONObject(Files.readString(
                Path.of("src/main/generated/assets/dwm/models/block/radiation_fungus.json")));
        assertEquals("dwm:block/template_radiation_fungus", model.getString("parent"));
        assertEquals("dwm:block/radiation_fungus", model.getJSONObject("textures").getString("texture"));

        JSONObject template = new JSONObject(Files.readString(
                Path.of("src/client/resources/assets/dwm/models/block/template_radiation_fungus.json")));
        var elements = template.getJSONArray("elements");
        assertFalse(elements.isEmpty(), "Toadstool template has no elements");
        int glowing = 0;
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            var from = element.getJSONArray("from");
            var to = element.getJSONArray("to");
            for (int axis : new int[]{0, 2}) {
                assertTrue(from.getDouble(axis) >= 2 && to.getDouble(axis) <= 14, "Element " + i + " outside outline");
            }
            assertTrue(from.getDouble(1) >= 0 && to.getDouble(1) <= 10, "Element " + i + " outside outline height");
            if (element.optInt("light_emission", 0) > 0) {
                glowing++;
            }
        }
        assertTrue(glowing > 0, "Toadstool gills should be emissive");
    }

    /** Radiation fungus item sprite and Thal crop stages: 16×16 cut-outs with a tight colour budget. */
    @Test
    public void skaroFloraSpritesMatchContract() throws Exception {
        String root = "src/client/resources/assets/dwm/textures/";
        assertPlantSprite(Path.of(root + "item/radiation_fungus.png"));
        for (int stage = 0; stage <= 3; stage++) {
            assertPlantSprite(Path.of(root + "block/thal_crop_stage" + stage + ".png"));
        }
        assertAtlasSize(Path.of(root + "block/radiation_fungus.png"), 16, 16);
    }

    private static void assertPlantSprite(Path png) throws Exception {
        assertAtlasSize(png, 16, 16);
        BufferedImage image = ImageIO.read(png.toFile());
        for (int[] corner : new int[][]{{0, 0}, {15, 0}, {0, 15}, {15, 15}}) {
            assertEquals(0, (image.getRGB(corner[0], corner[1]) >>> 24) & 0xFF, png + " corner must be transparent");
        }
        Set<Integer> opaqueRgb = new HashSet<>();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) != 0) {
                    opaqueRgb.add(argb & 0xFFFFFF);
                }
            }
        }
        assertFalse(opaqueRgb.isEmpty(), png + " has no opaque pixels");
        assertTrue(opaqueRgb.size() <= 12, png + " exceeds colour budget: " + opaqueRgb.size());
    }

    private static void assertAtlasSize(Path png, int width, int height) throws Exception {
        assertTrue(Files.isRegularFile(png) && Files.size(png) > 0, "Missing atlas: " + png);
        BufferedImage image = ImageIO.read(png.toFile());
        assertEquals(width, image.getWidth(), png + " width");
        assertEquals(height, image.getHeight(), png + " height");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
                assertTrue(alpha == 0 || alpha == 255, png + " soft alpha at " + x + "," + y);
            }
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping steel item defs
     * (allowlist must include {@code steel} substring).
     */
    @Test
    public void generatedSteelItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "steel_ingot",
                "steel_sword",
                "steel_shovel",
                "steel_pickaxe",
                "steel_axe",
                "steel_hoe",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated steel item model: " + item
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping dalekanium item defs
     * (allowlist must include {@code dalek} substring).
     */
    @Test
    public void generatedDalekaniumItemModelsExist() throws Exception {
        Path itemsDir = Path.of("src/main/generated/assets/dwm/items");
        assertTrue(Files.isDirectory(itemsDir), "Expected generated items dir at " + itemsDir);
        String[] ids = {
                "dalekanium_ore",
                "silver_dalekanium_ingot",
                "bronze_dalekanium_ingot",
                "silver_dalekanium_sword",
                "silver_dalekanium_shovel",
                "silver_dalekanium_pickaxe",
                "silver_dalekanium_axe",
                "silver_dalekanium_hoe",
                "bronze_dalekanium_sword",
                "bronze_dalekanium_shovel",
                "bronze_dalekanium_pickaxe",
                "bronze_dalekanium_axe",
                "bronze_dalekanium_hoe",
                "silver_dalekanium_panel",
                "silver_dalekanium_riveted_wall",
                "bronze_dalekanium_panel",
                "bronze_dalekanium_riveted_wall",
        };
        for (String id : ids) {
            Path item = itemsDir.resolve(id + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated dalekanium item model: " + item
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
    public void thalEntityTexturesExist() throws Exception {
        for (int i = 1; i <= 4; i++) {
            Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/thal/thal_" + i + ".png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "ThalRenderer expects assets/dwm/textures/entity/thal/thal_" + i + ".png"
            );
        }
    }

    @Test
    public void thalSpawnEggTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/item/thal_spawn_egg.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Spawn egg item model expects assets/dwm/textures/item/thal_spawn_egg.png"
        );
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Thal spawn egg item def
     * (allowlist must include {@code thal} substring).
     */
    @Test
    public void generatedThalSpawnEggItemModelExists() throws Exception {
        Path item = Path.of("src/main/generated/assets/dwm/items/thal_spawn_egg.json");
        assertTrue(
                Files.isRegularFile(item) && Files.size(item) > 0,
                "Missing generated Thal spawn egg item model: " + item
        );
    }

    @Test
    public void thalFearsTagListsTheDaleks() throws Exception {
        Path path = Path.of("src/main/generated/data/dwm/tags/entity_type/thal_fears.json");
        assertTrue(Files.isRegularFile(path), "Missing generated thal_fears entity tag: " + path);
        JSONObject tag = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        assertEquals(
                Set.of("dwm:dalek", "dwm:paradigm_dalek", "dwm:dalek_mutant"),
                new HashSet<>(tag.getJSONArray("values").toList().stream().map(Object::toString).toList())
        );
    }

    /** Residents come only from settlements: no biome may carry a Thal spawner entry. */
    @Test
    public void noBiomeSpawnsThals() throws Exception {
        Path biomeDir = Path.of("src/main/generated/data/dwm/worldgen/biome");
        try (var biomes = Files.list(biomeDir)) {
            for (Path biome : biomes.filter(f -> f.toString().endsWith(".json")).toList()) {
                assertFalse(
                        Files.readString(biome).contains("dwm:thal"),
                        biome.getFileName() + " must not list dwm:thal spawners"
                );
            }
        }
    }

    @Test
    public void gallifreyForestAndPlainsSpawnTimeLord() throws Exception {
        assertTrue(biomeHasCreatureSpawn("gallifrey_forest.json", "dwm:time_lord"));
        assertTrue(biomeHasCreatureSpawn("gallifrey_plains.json", "dwm:time_lord"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_wastes.json", "dwm:time_lord"));
        assertFalse(biomeHasCreatureSpawn("gallifrey_badlands.json", "dwm:time_lord"));
    }

    /**
     * The Paradigm glow overlay renders full-bright, so it may only light the eye and dome-light
     * cubes (UV swatches {@code [104, 8]} and {@code [112, 8]} in {@code paradigm_dalek.json}).
     */
    @Test
    public void paradigmGlowTextureCoversOnlyEyeAndLights() throws Exception {
        assertGlowCoversOnly("paradigm_dalek", "paradigm_glow", List.of(List.of(104, 8), List.of(112, 8)));
    }

    /**
     * Same rule for the Time War casing: only the lens ({@code [58, 55]}) and the dome lights
     * ({@code [64, 55]}) of {@code time_war_dalek.json} may glow.
     */
    @Test
    public void timeWarGlowTextureCoversOnlyLensAndLights() throws Exception {
        assertGlowCoversOnly("time_war_dalek", "time_war_glow", List.of(List.of(58, 55), List.of(64, 55)));
    }

    private static void assertGlowCoversOnly(String modelName, String textureName, List<List<Integer>> glowUvs)
            throws Exception {
        Path model = Path.of("src/client/resources/assets/dwm/models/entity/" + modelName + ".json");
        List<int[]> glowRects = new java.util.ArrayList<>();
        var parts = new JSONObject(new JSONTokener(Files.newBufferedReader(model))).getJSONArray("parts");
        for (int i = 0; i < parts.length(); i++) {
            collectGlowRects(parts.getJSONObject(i), glowUvs, glowRects);
        }
        assertEquals(3, glowRects.size(), "expected one eye cube and two dome-light cubes in " + model);

        Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/dalek/" + textureName + ".png");
        assertTrue(Files.isRegularFile(texture), "Missing " + texture);
        BufferedImage image = ImageIO.read(texture.toFile());
        assertEquals(128, image.getWidth(), texture + " width");
        assertEquals(64, image.getHeight(), texture + " height");
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                int px = x;
                int py = y;
                assertTrue(
                        glowRects.stream().anyMatch(r -> px >= r[0] && px < r[2] && py >= r[1] && py < r[3]),
                        "Glow texel outside the eye/light UVs at " + x + "," + y + " in " + texture
                );
            }
        }
        for (int[] rect : glowRects) {
            boolean lit = false;
            for (int y = rect[1]; y < rect[3] && !lit; y++) {
                for (int x = rect[0]; x < rect[2] && !lit; x++) {
                    lit = (image.getRGB(x, y) >>> 24) != 0;
                }
            }
            assertTrue(lit, "Glow texture leaves the cube at uv " + rect[0] + "," + rect[1] + " dark");
        }
    }

    private static void collectGlowRects(JSONObject part, List<List<Integer>> glowUvs, List<int[]> out) {
        var cubes = part.optJSONArray("cubes");
        for (int i = 0; cubes != null && i < cubes.length(); i++) {
            JSONObject cube = cubes.getJSONObject(i);
            var uv = cube.getJSONArray("uv");
            int u = uv.getInt(0);
            int v = uv.getInt(1);
            if (!glowUvs.contains(List.of(u, v))) {
                continue;
            }
            var size = cube.getJSONArray("size");
            int w = (int) Math.ceil(size.getDouble(0));
            int h = (int) Math.ceil(size.getDouble(1));
            int d = (int) Math.ceil(size.getDouble(2));
            out.add(new int[]{u, v, u + 2 * (d + w), v + d + h});
        }
        var children = part.optJSONArray("children");
        for (int i = 0; children != null && i < children.length(); i++) {
            collectGlowRects(children.getJSONObject(i), glowUvs, out);
        }
    }

    @Test
    public void dalekVariantTexturesExist() throws Exception {
        for (DalekVariant variant : DalekVariant.values()) {
            Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/dalek/"
                    + variant.getSerializedName() + ".png");
            assertTrue(Files.isRegularFile(texture) && Files.size(texture) > 0, "Missing " + texture);
            BufferedImage image = ImageIO.read(texture.toFile());
            assertEquals(128, image.getWidth(), texture + " width");
            assertEquals(64, image.getHeight(), texture + " height");
        }
    }

    /**
     * Classic skins are painted per cube rather than as flat swatches. Samples the 1963 layout in
     * {@code dalek.json} and the Time War layout in {@code time_war_dalek.json}.
     */
    @Test
    public void classicDalekSkinsArePaintedPerCube() throws Exception {
        BufferedImage sixties = readDalekSkin("1963");
        // Bottom skirt tier front (island [56, 0], 11x3x13): silver casing.
        int casing = sixties.getRGB(69, 13);
        assertTrue(isGrey(casing) && channel(casing, 16) > 100, "1963 casing should be silver");
        // Hemisphere front (island [112, 42]): the Shawcraft blue.
        int hemisphere = sixties.getRGB(113, 43);
        assertTrue(channel(hemisphere, 0) > channel(hemisphere, 16) + 40, "1963 hemispheres should be blue");
        // Neck rings are casing-coloured over a dark mesh core.
        assertTrue(luma(sixties.getRGB(105, 38)) > luma(sixties.getRGB(109, 5)) + 60, "1963 neck ring vs mesh");

        for (String bronze : List.of("time_war_bronze", "time_war_bronze_dark")) {
            BufferedImage skin = readDalekSkin(bronze);
            // Hemisphere front (island [40, 55]): gold, warmer than the casing.
            int gold = skin.getRGB(41, 56);
            assertTrue(channel(gold, 16) > channel(gold, 0) + 60, bronze + " hemispheres should be gold");
            // Shoulder slats (island [22, 55]) stand out against the dark shoulder mesh (island [0, 33]).
            assertTrue(luma(skin.getRGB(23, 56)) > luma(skin.getRGB(9, 42)) + 40, bronze + " slats vs mesh");
        }

        for (String skin : List.of("1963", "time_war_bronze", "time_war_bronze_dark")) {
            BufferedImage image = readDalekSkin(skin);
            java.util.Set<Integer> colours = new java.util.HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    if ((argb >>> 24) != 0) {
                        colours.add(argb);
                    }
                }
            }
            assertTrue(colours.size() >= 20, skin + " looks flat: only " + colours.size() + " colours");
        }
    }

    private static BufferedImage readDalekSkin(String name) throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/entity/dalek/" + name + ".png");
        assertTrue(Files.isRegularFile(texture) && Files.size(texture) > 0, "Missing " + texture);
        return ImageIO.read(texture.toFile());
    }

    private static int channel(int argb, int shift) {
        return (argb >> shift) & 0xFF;
    }

    private static int luma(int argb) {
        return (channel(argb, 16) * 299 + channel(argb, 8) * 587 + channel(argb, 0) * 114) / 1000;
    }

    private static boolean isGrey(int argb) {
        int r = channel(argb, 16);
        int g = channel(argb, 8);
        int b = channel(argb, 0);
        return Math.abs(r - g) < 16 && Math.abs(g - b) < 16;
    }

    @Test
    public void dalekSpawnEggTextureExists() throws Exception {
        for (String egg : new String[]{"dalek_spawn_egg", "paradigm_dalek_spawn_egg"}) {
            Path texture = Path.of("src/client/resources/assets/dwm/textures/item/" + egg + ".png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "Spawn egg item model expects assets/dwm/textures/item/" + egg + ".png"
            );
        }
    }

    /**
     * Guards against {@code pruneDatagenItemModels} dropping the Dalek spawn egg item def
     * (allowlist must include {@code dalek} substring).
     */
    @Test
    public void generatedDalekSpawnEggItemModelExists() throws Exception {
        for (String egg : new String[]{"dalek_spawn_egg", "paradigm_dalek_spawn_egg"}) {
            Path item = Path.of("src/main/generated/assets/dwm/items/" + egg + ".json");
            assertTrue(
                    Files.isRegularFile(item) && Files.size(item) > 0,
                    "Missing generated Dalek spawn egg item model: " + item
            );
        }
    }

    @Test
    public void gallifreyBiomesDoNotSpawnDalek() throws Exception {
        for (String biome : new String[]{
                "gallifrey_forest.json",
                "gallifrey_plains.json",
                "gallifrey_wastes.json",
                "gallifrey_badlands.json"
        }) {
            for (String dalek : new String[]{"dwm:dalek", "dwm:paradigm_dalek"}) {
                assertFalse(biomeHasCreatureSpawn(biome, dalek));
                assertFalse(biomeHasMonsterSpawn(biome, dalek));
            }
        }
    }

    @Test
    public void dalekIsFallDamageImmune() throws Exception {
        Path path = Path.of("src/main/generated/data/minecraft/tags/entity_type/fall_damage_immune.json");
        assertTrue(Files.isRegularFile(path), "Missing generated fall_damage_immune entity tag: " + path);
        JSONObject tag = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
        var values = tag.getJSONArray("values").toList();
        assertTrue(values.contains("dwm:dalek"), "fall_damage_immune should include dwm:dalek");
        assertTrue(values.contains("dwm:paradigm_dalek"), "fall_damage_immune should include dwm:paradigm_dalek");
    }

    @Test
    public void dalekSoundFilesExist() throws Exception {
        String[] names = {"exterminate", "ambient", "hurt", "death", "shoot"};
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
                "entity.dalek.exterminate",
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
                Path clip = Path.of("src/client/resources/assets/dwm/sounds/" + name.substring("dwm:".length()) + ".ogg");
                assertTrue(
                        Files.isRegularFile(clip) && Files.size(clip) > 0,
                        event + " lists a clip with no file: " + clip
                );
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
                if ("monster".equals(category)) {
                    continue;
                }
                assertEquals(
                        0,
                        spawners.getJSONArray(category).length(),
                        biomeFile + " must have empty spawners." + category
                );
            }
            boolean sparse = "skaro_irradiated_wastes.json".equals(biomeFile)
                    || "skaro_thal_plateau.json".equals(biomeFile);
            assertDalekMonsterPatrol(biome, biomeFile, sparse);
            String blob = Files.readString(path);
            // Skaro's own ash scrub shares the "dwm:ash" prefix with the Gallifrey ash tree; mask it for the guard.
            String gallifreyGuardBlob = blob.replace("dwm:ash_scrub", "dwm:skaro_scrub");
            for (String forbidden : forbiddenFeatureSubstrings) {
                assertFalse(
                        gallifreyGuardBlob.contains(forbidden),
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

    private static JSONObject readJson(String path) throws Exception {
        return new JSONObject(new JSONTokener(Files.newBufferedReader(Path.of(path))));
    }

    @Test
    public void thalSettlementStructureIsPlateauOnlyAndSuppressesMonsterSpawns() throws Exception {
        JSONObject structure = readJson("src/main/resources/data/dwm/worldgen/structure/thal_settlement.json");
        assertEquals("dwm:thal_settlement", structure.getString("type"));
        assertEquals("#dwm:has_structure/thal_settlement", structure.getString("biomes"));
        assertEquals("surface_structures", structure.getString("step"));
        JSONObject monster = structure.getJSONObject("spawn_overrides").getJSONObject("monster");
        assertEquals("full", monster.getString("bounding_box"));
        assertEquals(0, monster.getJSONArray("spawns").length(), "Dalek patrols must be suppressed inside settlements");

        JSONObject tag = readJson("src/main/resources/data/dwm/tags/worldgen/biome/has_structure/thal_settlement.json");
        assertFalse(tag.optBoolean("replace", false), "settlement biome tag must not replace");
        assertEquals(
                List.of("dwm:skaro_thal_plateau"),
                tag.getJSONArray("values").toList().stream().map(Object::toString).toList(),
                "only the Thal Plateau may host a settlement"
        );
    }

    @Test
    public void thalSettlementStructureSetIsRareAndUsesAUniqueSalt() throws Exception {
        JSONObject set = readJson("src/main/resources/data/dwm/worldgen/structure_set/thal_settlement.json");
        var structures = set.getJSONArray("structures");
        assertEquals(1, structures.length());
        assertEquals("dwm:thal_settlement", structures.getJSONObject(0).getString("structure"));
        JSONObject placement = set.getJSONObject("placement");
        assertEquals("minecraft:random_spread", placement.getString("type"));
        assertTrue(placement.getInt("spacing") >= 32, "settlements must stay rare");
        assertTrue(placement.getInt("separation") < placement.getInt("spacing"));

        // Settlements keep clear of hostile Dalek military landmarks (outposts and later trenches/wreckage).
        JSONObject exclusion = placement.getJSONObject("exclusion_zone");
        assertEquals("dwm:skaro_military_landmarks", exclusion.getString("other_set"));
        assertTrue(Files.exists(Path.of("src/main/resources/data/dwm/worldgen/structure_set/skaro_military_landmarks.json")),
                "excluded structure set must exist");
        int chunkCount = exclusion.getInt("chunk_count");
        assertTrue(chunkCount >= 1 && chunkCount <= 16, "vanilla exclusion_zone chunk_count must be 1..16, got " + chunkCount);

        Set<Integer> salts = new HashSet<>();
        try (var sets = Files.list(Path.of("src/main/resources/data/dwm/worldgen/structure_set"))) {
            for (Path file : sets.filter(f -> f.toString().endsWith(".json")).toList()) {
                JSONObject other = readJson(file.toString());
                assertTrue(
                        salts.add(other.getJSONObject("placement").getInt("salt")),
                        "duplicate structure set salt in " + file.getFileName()
                );
            }
        }
    }

    private static void assertDalekMonsterPatrol(JSONObject biome, String biomeFile, boolean sparse) {
        var monsters = biome.getJSONObject("spawners").getJSONArray("monster");
        assertEquals(2, monsters.length(), biomeFile + " must spawn only Dalek and Paradigm Dalek monsters");
        var spawn = monsters.getJSONObject(0);
        assertEquals("dwm:dalek", spawn.getString("type"), biomeFile + " monster spawn must be dwm:dalek");
        int expectedWeight = sparse ? DalekPatrolLogic.SPARSE_SPAWN_WEIGHT : DalekPatrolLogic.STANDARD_SPAWN_WEIGHT;
        int expectedMin = sparse ? DalekPatrolLogic.SPARSE_MIN_COUNT : DalekPatrolLogic.STANDARD_MIN_COUNT;
        int expectedMax = sparse ? DalekPatrolLogic.SPARSE_MAX_COUNT : DalekPatrolLogic.STANDARD_MAX_COUNT;
        double expectedCharge = sparse ? DalekPatrolLogic.SPARSE_SPAWN_CHARGE : DalekPatrolLogic.STANDARD_SPAWN_CHARGE;
        double expectedBudget = sparse
                ? DalekPatrolLogic.SPARSE_SPAWN_ENERGY_BUDGET
                : DalekPatrolLogic.STANDARD_SPAWN_ENERGY_BUDGET;
        assertEquals(expectedWeight, spawn.getInt("weight"), biomeFile + " Dalek weight");
        assertEquals(expectedMin, spawn.getInt("minCount"), biomeFile + " Dalek minCount");
        assertEquals(expectedMax, spawn.getInt("maxCount"), biomeFile + " Dalek maxCount");
        var costs = biome.getJSONObject("spawn_costs").getJSONObject("dwm:dalek");
        assertEquals(expectedCharge, costs.getDouble("charge"), 1e-9, biomeFile + " Dalek spawn charge");
        assertEquals(expectedBudget, costs.getDouble("energy_budget"), 1e-9, biomeFile + " Dalek energy budget");

        var paradigm = monsters.getJSONObject(1);
        assertEquals("dwm:paradigm_dalek", paradigm.getString("type"), biomeFile + " second monster spawn");
        int expectedParadigmWeight = sparse
                ? DalekPatrolLogic.PARADIGM_SPARSE_SPAWN_WEIGHT
                : DalekPatrolLogic.PARADIGM_STANDARD_SPAWN_WEIGHT;
        assertEquals(expectedParadigmWeight, paradigm.getInt("weight"), biomeFile + " Paradigm weight");
        assertTrue(expectedParadigmWeight < expectedWeight, biomeFile + " Paradigm Daleks must be rarer than Daleks");
        assertEquals(DalekPatrolLogic.PARADIGM_MIN_COUNT, paradigm.getInt("minCount"), biomeFile + " Paradigm minCount");
        assertEquals(DalekPatrolLogic.PARADIGM_MAX_COUNT, paradigm.getInt("maxCount"), biomeFile + " Paradigm maxCount");
        var paradigmCosts = biome.getJSONObject("spawn_costs").getJSONObject("dwm:paradigm_dalek");
        assertEquals(DalekPatrolLogic.PARADIGM_SPAWN_CHARGE, paradigmCosts.getDouble("charge"), 1e-9,
                biomeFile + " Paradigm spawn charge");
        assertEquals(DalekPatrolLogic.PARADIGM_SPAWN_ENERGY_BUDGET, paradigmCosts.getDouble("energy_budget"), 1e-9,
                biomeFile + " Paradigm energy budget");
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
