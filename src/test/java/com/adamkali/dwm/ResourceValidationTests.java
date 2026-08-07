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
}
