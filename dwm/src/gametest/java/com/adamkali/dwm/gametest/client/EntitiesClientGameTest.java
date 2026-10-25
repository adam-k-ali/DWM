package com.adamkali.dwm.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Screenshots of DWM mobs lined up facing the camera (AI off, so poses are stable):
 * {@code entities_time_lords}, {@code entities_flutterwings}, {@code entities_misc},
 * {@code entities_paradigm_daleks}.
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
                    new Mob("time_lord", "Variant:\"time_lord_1\""),
                    new Mob("time_lord", "Variant:\"time_lord_2\""),
                    new Mob("time_lord", "Variant:\"time_lord_3\""),
                    new Mob("time_lord", "Variant:\"time_lord_4\"")));
            lineUp(context, world, 1, "entities_flutterwings", List.of(
                    new Mob("flutterwing", "Variant:\"blue_crystal\""),
                    new Mob("flutterwing", "Variant:\"madrigal\""),
                    new Mob("flutterwing", "Variant:\"silverband\""),
                    new Mob("flutterwing", "Variant:\"wild_endeavour\"")));
            lineUp(context, world, 2, "entities_misc", List.of(
                    new Mob("dalek", "Variant:\"1963\""),
                    new Mob("dalek", "Variant:\"time_war_bronze\""),
                    new Mob("dalek", "Variant:\"time_war_bronze_dark\""),
                    new Mob("mewing_dog", ""),
                    new Mob("broakir", "")));
            // A 1963 Dalek at the end for scale against the larger Paradigm casing.
            lineUp(context, world, 3, "entities_paradigm_daleks", List.of(
                    new Mob("paradigm_dalek", "Variant:\"paradigm_drone\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_strategist\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_scientist\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_eternal\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_supreme\""),
                    new Mob("dalek", "Variant:\"1963\"")));

            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void lineUp(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            int group,
            String screenshotName,
            List<Mob> entities
    ) {
        // Each group gets its own pad far from the others, so nothing has to be killed between
        // screenshots (death "poof" particles are random and would make the images non-deterministic).
        int centerX = group * GROUP_SPACING;
        ClientGameTestSupport.buildPad(world.getServer(), centerX);
        world.getServer().runCommand(String.format("tp @a %d %d 0 0 0", centerX, ClientGameTestSupport.CAMERA_Y));
        context.getInput().lookAt(new BlockPos(centerX, ClientGameTestSupport.CAMERA_Y, 8));
        double spacing = 2.5;
        double startX = centerX - spacing * (entities.size() - 1) / 2.0;
        for (int i = 0; i < entities.size(); i++) {
            Mob mob = entities.get(i);
            world.getServer().runCommand(String.format(
                    "summon dwm:%s %.1f %d 7 %s",
                    mob.type(), startX + i * spacing, ClientGameTestSupport.PAD_Y, mob.summonNbt()));
        }
        context.waitTicks(40);
        ClientGameTestSupport.screenshot(context, screenshotName);
    }

    /** A DWM entity to summon: registry path under {@code dwm:} plus any extra NBT entries (no braces). */
    private record Mob(String type, String extraNbt) {
        /** AI off and silent so poses are stable, and turned to face the camera. */
        String summonNbt() {
            String base = "NoAI:1b,Silent:1b," + FACING_CAMERA;
            return "{" + (extraNbt.isEmpty() ? base : extraNbt + "," + base) + "}";
        }
    }
}
