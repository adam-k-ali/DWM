package com.adamkali.dwm;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

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
     * Fabric remaps modded wood types to {@code dwm:textures/gui/hanging_signs/<id>.png}
     * for hanging-sign edit UI (not referenced from model JSON).
     */
    @Test
    public void hangingSignGuiTexturesExist() throws Exception {
        Path root = Path.of("src/client/resources/assets/dwm/textures/gui/hanging_signs");
        for (String woodTypeId : WOOD_TYPE_IDS) {
            Path texture = root.resolve(woodTypeId + ".png");
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "Fabric HangingSignEditScreen expects assets/dwm/textures/gui/hanging_signs/"
                            + woodTypeId + ".png for wood type dwm:" + woodTypeId
            );
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
