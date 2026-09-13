package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.JsonValidationHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintSchemaValidationTest {
    @Test
    void fixtureBlueprints_matchSchema() {
        assertTrue(JsonValidationHelpers.validateJsonFiles(
                "src/test/resources/schemas/blueprint.schema.json",
                "src/test/resources/blueprints"
        ));
    }
}
