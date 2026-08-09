package com.adamkali.dwm;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ResourceValidationTests {
    /** Wood type path ids registered in {@link com.adamkali.dwm.block.DWMWoodTypes}. */
    private static final String[] WOOD_TYPE_IDS = {"ash", "dark_ash", "cardinal"};

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
}
