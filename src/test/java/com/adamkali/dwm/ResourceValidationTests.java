package com.adamkali.dwm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceValidationTests {
    @Test
    public void validateItemModels() {
        assertTrue(JsonValidationHelpers.validateJsonFiles("src/test/resources/schemas/item_model.schema.json", "src/client/resources/assets/dwm/models/item"));
    }


}
