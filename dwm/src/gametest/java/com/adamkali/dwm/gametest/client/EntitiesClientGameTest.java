package com.adamkali.dwm.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import java.util.List;

/**
 * Screenshots of DWM mobs lined up facing the camera (AI off, so poses are stable):
 * {@code entities_time_lords}, {@code entities_flutterwings}, {@code entities_misc}.
 */
public class EntitiesClientGameTest implements FabricClientGameTest {
    private static final int GROUP_SPACING = 48;
    private static final String FACING_CAMERA = "Rotation:[180f,0f]";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            ClientGameTestSupport.toggleHud(context);

            lineUp(context, world, 0, "entities_time_lords", List.of(
                    "time_lord {Variant:\"time_lord_1\"}",
                    "time_lord {Variant:\"time_lord_2\"}",
                    "time_lord {Variant:\"time_lord_3\"}",
                    "time_lord {Variant:\"time_lord_4\"}"));
            lineUp(context, world, 1, "entities_flutterwings", List.of(
                    "flutterwing {Variant:\"blue_crystal\"}",
                    "flutterwing {Variant:\"madrigal\"}",
                    "flutterwing {Variant:\"silverband\"}",
                    "flutterwing {Variant:\"wild_endeavour\"}"));
            lineUp(context, world, 2, "entities_misc", List.of(
                    "dalek {Variant:\"1963\"}",
                    "mewing_dog {}",
                    "broakir {}"));

            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void lineUp(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            int group,
            String screenshotName,
            List<String> entities
    ) {
        // Each group gets its own pad far from the others, so nothing has to be killed between
        // screenshots (death "poof" particles are random and would make the images non-deterministic).
        int centerX = group * GROUP_SPACING;
        ClientGameTestSupport.buildPad(world.getServer(), centerX);
        world.getServer().runCommand(String.format("tp @a %d %d 0 0 0", centerX, ClientGameTestSupport.CAMERA_Y));
        context.getInput().lookAt(new net.minecraft.core.BlockPos(centerX, ClientGameTestSupport.CAMERA_Y, 8));
        double spacing = 2.5;
        double startX = centerX - spacing * (entities.size() - 1) / 2.0;
        for (int i = 0; i < entities.size(); i++) {
            String typeAndNbt = entities.get(i);
            int split = typeAndNbt.indexOf(' ');
            String type = typeAndNbt.substring(0, split);
            String nbt = typeAndNbt.substring(split + 1).trim();
            String merged = nbt.equals("{}")
                    ? "{NoAI:1b,Silent:1b," + FACING_CAMERA + "}"
                    : nbt.substring(0, nbt.length() - 1) + ",NoAI:1b,Silent:1b," + FACING_CAMERA + "}";
            world.getServer().runCommand(String.format(
                    "summon dwm:%s %.1f %d 7 %s", type, startX + i * spacing, ClientGameTestSupport.PAD_Y, merged));
        }
        context.waitTicks(40);
        ClientGameTestSupport.screenshot(context, screenshotName);
    }
}
