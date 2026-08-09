package com.adamkali.dwm;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceValidationTests {
    @Test
    public void validateItemModels() {
        assertTrue(JsonValidationHelpers.validateJsonFiles("src/test/resources/schemas/item_model.schema.json", "src/client/resources/assets/dwm/models/item"));
    }

    /**
     * Fabric remaps modded wood type {@code dwm:ash} hanging-sign edit UI to
     * {@code dwm:textures/gui/hanging_signs/ash.png} (not the entity atlas).
     */
    @Test
    public void ashHangingSignGuiTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/gui/hanging_signs/ash.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Fabric HangingSignEditScreen expects assets/dwm/textures/gui/hanging_signs/ash.png for wood type dwm:ash"
        );
    }

    /**
     * Fabric remaps modded wood type {@code dwm:dark_ash} hanging-sign edit UI to
     * {@code dwm:textures/gui/hanging_signs/dark_ash.png} (not the entity atlas).
     */
    @Test
    public void darkAshHangingSignGuiTextureExists() throws Exception {
        Path texture = Path.of("src/client/resources/assets/dwm/textures/gui/hanging_signs/dark_ash.png");
        assertTrue(
                Files.isRegularFile(texture) && Files.size(texture) > 0,
                "Fabric HangingSignEditScreen expects assets/dwm/textures/gui/hanging_signs/dark_ash.png for wood type dwm:dark_ash"
        );
    }

    /**
     * Custom softetch door models use a single block atlas ({@code block/<id>_door}) plus an
     * inventory icon ({@code item/<id>_door}). Missing PNGs render as purple/black placeholders.
     */
    @Test
    public void ashAndDarkAshDoorTexturesExist() throws Exception {
        String[] required = {
                "textures/item/ash_door.png",
                "textures/item/dark_ash_door.png",
                "textures/block/ash_door.png",
                "textures/block/dark_ash_door.png",
        };
        Path root = Path.of("src/client/resources/assets/dwm");
        for (String relative : required) {
            Path texture = root.resolve(relative);
            assertTrue(
                    Files.isRegularFile(texture) && Files.size(texture) > 0,
                    "Door models require assets/dwm/" + relative
            );
        }
    }

    /**
     * Custom softetch trapdoor models use shared {@code template_ash_trapdoor_*} geometry plus
     * per-wood wrappers and block atlases. Inventory icons parent the bottom block model.
     */
    @Test
    public void ashAndDarkAshTrapdoorAssetsExist() throws Exception {
        String[] required = {
                "textures/block/ash_trapdoor.png",
                "textures/block/dark_ash_trapdoor.png",
                "blockstates/ash_trapdoor.json",
                "blockstates/dark_ash_trapdoor.json",
                "models/block/template_ash_trapdoor_bottom.json",
                "models/block/template_ash_trapdoor_top.json",
                "models/block/template_ash_trapdoor_open.json",
                "models/block/ash_trapdoor_bottom.json",
                "models/block/ash_trapdoor_top.json",
                "models/block/ash_trapdoor_open.json",
                "models/block/dark_ash_trapdoor_bottom.json",
                "models/block/dark_ash_trapdoor_top.json",
                "models/block/dark_ash_trapdoor_open.json",
        };
        Path root = Path.of("src/client/resources/assets/dwm");
        for (String relative : required) {
            Path asset = root.resolve(relative);
            assertTrue(
                    Files.isRegularFile(asset) && Files.size(asset) > 0,
                    "Trapdoor models require assets/dwm/" + relative
            );
        }
    }
}
