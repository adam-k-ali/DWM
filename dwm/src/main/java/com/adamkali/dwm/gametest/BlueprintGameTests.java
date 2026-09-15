package com.adamkali.dwm.gametest;

import com.adamkali.dwm.blueprint.BlueprintLoader;
import com.adamkali.dwm.blueprint.BlueprintPlacer;
import com.adamkali.dwm.blueprint.model.Blueprint;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Smoke: load a tiny blueprint from the world {@code blueprints/} folder and place it.
 */
public class BlueprintGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void placeRectangleBlueprintFromWorldFolder(GameTestHelper context) throws Exception {
        Path previous = BlueprintLoader.blueprintsDirectory;
        try {
            Path blueprintsDir = context.getLevel()
                    .getServer()
                    .getWorldPath(LevelResource.ROOT)
                    .resolve("gametest_blueprints");
            Files.createDirectories(blueprintsDir);
            Files.writeString(blueprintsDir.resolve("smoke_rect.json"), """
                    {
                      "shapes": [
                        {
                          "type": "rectangle",
                          "from": {"x": 0, "y": 0, "z": 0},
                          "to": {"x": 1, "y": 0, "z": 0},
                          "block": {"id": "minecraft:gold_block"},
                          "fill": true
                        }
                      ]
                    }
                    """);
            BlueprintLoader.blueprintsDirectory = blueprintsDir;

            Blueprint blueprint = BlueprintLoader.load("smoke_rect");
            BlockPos originRel = new BlockPos(1, 1, 1);
            BlockPos originAbs = context.absolutePos(originRel);
            int placed = BlueprintPlacer.place(context.getLevel(), originAbs, blueprint);
            if (placed != 2) {
                throw new AssertionError("Expected 2 blocks placed, got " + placed);
            }

            context.assertBlockPresent(Blocks.GOLD_BLOCK, originRel);
            context.assertBlockPresent(Blocks.GOLD_BLOCK, originRel.offset(1, 0, 0));
            context.succeed();
        } finally {
            BlueprintLoader.blueprintsDirectory = previous;
        }
    }
}
