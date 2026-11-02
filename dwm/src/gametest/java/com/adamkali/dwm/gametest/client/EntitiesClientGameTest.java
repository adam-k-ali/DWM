package com.adamkali.dwm.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Screenshots of DWM mobs lined up facing the camera (AI off, so poses are stable):
 * {@code entities_time_lords}, {@code entities_flutterwings}, {@code entities_misc},
 * {@code entities_thals} (the four Thal variants),
 * {@code entities_cybermen} (CyberMondan, Cybus, Cybermaster, weapons-grade, close up, plus
 * {@code entities_cybermen_angled}: the same four turned 35 degrees to show the depth of the sculpted faces),
 * {@code entities_classic_daleks} (1963 and Time War casings, close up, plus
 * {@code entities_classic_daleks_night}: only the Time War eye and lights glow),
 * {@code entities_paradigm_daleks}, and the same Paradigm group at midnight
 * ({@code entities_paradigm_daleks_night}) to show the glowing eye and dome lights.
 */
public class EntitiesClientGameTest implements FabricClientGameTest {
    private static final int GROUP_SPACING = 48;
    private static final float FACING_CAMERA = 180.0F;
    private static final float ANGLED = 145.0F;
    private static final Framing DEFAULT = new Framing(7, 2.5);
    private static final Framing CLOSE_UP = new Framing(4, 1.6);

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
                    new Mob("mewing_dog", ""),
                    new Mob("broakir", ""),
                    new Mob("dalek_mutant", "")));
            lineUp(context, world, 4, "entities_classic_daleks", CLOSE_UP, List.of(
                    new Mob("dalek", "Variant:\"1963\""),
                    new Mob("dalek", "Variant:\"time_war_bronze\""),
                    new Mob("dalek", "Variant:\"time_war_bronze_dark\"")));
            nightShot(context, world, "entities_classic_daleks_night");
            // A 1963 and a Time War Dalek at the end for scale against the larger Paradigm casing.
            lineUp(context, world, 3, "entities_paradigm_daleks", List.of(
                    new Mob("paradigm_dalek", "Variant:\"paradigm_drone\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_strategist\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_scientist\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_eternal\""),
                    new Mob("paradigm_dalek", "Variant:\"paradigm_supreme\""),
                    new Mob("dalek", "Variant:\"1963\""),
                    new Mob("dalek", "Variant:\"time_war_bronze\"")));
            nightShot(context, world, "entities_paradigm_daleks_night");
            lineUp(context, world, 5, "entities_thals", List.of(
                    new Mob("thal", "Variant:\"thal_1\""),
                    new Mob("thal", "Variant:\"thal_2\""),
                    new Mob("thal", "Variant:\"thal_3\""),
                    new Mob("thal", "Variant:\"thal_4\"")));
            lineUp(context, world, 6, "entities_cybermen", CLOSE_UP, cybermen(FACING_CAMERA));
            lineUp(context, world, 7, "entities_cybermen_angled", CLOSE_UP, cybermen(ANGLED));

            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static List<Mob> cybermen(float yaw) {
        return List.of(
                new Mob("cybermondan", "", yaw),
                new Mob("cybus_cyberman", "", yaw),
                new Mob("cybermaster", "", yaw),
                new Mob("weapons_grade_cyberman", "", yaw));
    }

    /** Re-shoots the current group at midnight, then restores noon. */
    private static void nightShot(ClientGameTestContext context, TestSingleplayerContext world, String screenshotName) {
        world.getServer().runCommand("time set midnight");
        context.waitTicks(20);
        ClientGameTestSupport.screenshot(context, screenshotName);
        world.getServer().runCommand("time set noon");
    }

    private static void lineUp(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            int group,
            String screenshotName,
            List<Mob> entities
    ) {
        lineUp(context, world, group, screenshotName, DEFAULT, entities);
    }

    private static void lineUp(
            ClientGameTestContext context,
            TestSingleplayerContext world,
            int group,
            String screenshotName,
            Framing framing,
            List<Mob> entities
    ) {
        // Each group gets its own pad far from the others, so nothing has to be killed between
        // screenshots (death "poof" particles are random and would make the images non-deterministic).
        int centerX = group * GROUP_SPACING;
        ClientGameTestSupport.buildPad(world.getServer(), centerX);
        world.getServer().runCommand(String.format("tp @a %d %d 0 0 0", centerX, ClientGameTestSupport.CAMERA_Y));
        context.getInput().lookAt(new BlockPos(centerX, ClientGameTestSupport.CAMERA_Y, framing.distance() + 1));
        double spacing = framing.spacing();
        double startX = centerX - spacing * (entities.size() - 1) / 2.0;
        for (int i = 0; i < entities.size(); i++) {
            Mob mob = entities.get(i);
            world.getServer().runCommand(String.format(
                    "summon dwm:%s %.1f %d %d %s",
                    mob.type(), startX + i * spacing, ClientGameTestSupport.PAD_Y, framing.distance(), mob.summonNbt()));
        }
        context.waitTicks(40);
        ClientGameTestSupport.screenshot(context, screenshotName);
    }

    /** How far in front of the camera a group stands (blocks) and how far apart its mobs are. */
    private record Framing(int distance, double spacing) {
    }

    /**
     * A DWM entity to summon: registry path under {@code dwm:}, any extra NBT entries (no braces) and its
     * yaw ({@value #FACING_CAMERA} faces the camera).
     */
    private record Mob(String type, String extraNbt, float yaw) {
        Mob(String type, String extraNbt) {
            this(type, extraNbt, FACING_CAMERA);
        }

        /** AI off and silent so poses are stable, and turned to the given yaw. */
        String summonNbt() {
            String base = "NoAI:1b,Silent:1b,Rotation:[" + yaw + "f,0f]";
            return "{" + (extraNbt.isEmpty() ? base : extraNbt + "," + base) + "}";
        }
    }
}
