package com.adamkali.dwm.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;

/**
 * Dalek Laser Gun in every display context: {@code laser_gun_first_person} (main and off hand),
 * {@code laser_gun_inventory} (slot icons plus the player preview holding it),
 * {@code laser_gun_third_person} (mannequins: right hand side-on, facing the camera, left hand side-on) and
 * {@code laser_gun_displays} (in an item frame and dropped on the ground).
 */
public class LaserGunClientGameTest implements FabricClientGameTest {
    private static final String GUN = "{id:\"dwm:laser_gun\",count:1}";
    private static final int DISPLAY_PAD_X = 48;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            var server = world.getServer();
            ClientGameTestSupport.stage(server, "survival");
            server.runCommand("item replace entity @a weapon.mainhand with dwm:laser_gun");
            server.runCommand("item replace entity @a weapon.offhand with dwm:laser_gun");
            context.waitTicks(20);
            ClientGameTestSupport.clearToasts(context);
            context.waitTicks(2);
            ClientGameTestSupport.screenshot(context, "laser_gun_first_person");

            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitForScreen(InventoryScreen.class);
            context.waitTicks(5);
            ClientGameTestSupport.screenshot(context, "laser_gun_inventory");
            context.setScreen(() -> null);

            ClientGameTestSupport.toggleHud(context);
            int z = 3;
            context.getInput().lookAt(new BlockPos(0, ClientGameTestSupport.PAD_Y, z));
            // Yaw 90 faces west, so the right hand is on the camera side; yaw -90 shows the left hand.
            mannequin(server, -1.4, z, 90, "right");
            mannequin(server, 0, z, 180, "right");
            mannequin(server, 1.4, z, -90, "left");
            context.waitTicks(20);
            ClientGameTestSupport.screenshot(context, "laser_gun_third_person");

            // Item frame on a block (fixed context) and a dropped item that can't be picked up (ground context).
            ClientGameTestSupport.buildPad(server, DISPLAY_PAD_X);
            server.runCommand(String.format("tp @a %d %d 0 0 0", DISPLAY_PAD_X, ClientGameTestSupport.CAMERA_Y));
            context.getInput().lookAt(new BlockPos(DISPLAY_PAD_X, ClientGameTestSupport.PAD_Y, z));
            server.runCommand(String.format("setblock %d %d %d stone", DISPLAY_PAD_X - 1, ClientGameTestSupport.CAMERA_Y, z + 1));
            server.runCommand(String.format(
                    "summon item_frame %d %d %d {Facing:2b,Fixed:1b,Item:%s}",
                    DISPLAY_PAD_X - 1, ClientGameTestSupport.CAMERA_Y, z, GUN));
            server.runCommand(String.format(
                    "summon item %.1f %d %.1f {PickupDelay:32767s,Motion:[0d,0d,0d],Item:%s}",
                    DISPLAY_PAD_X + 1.0, ClientGameTestSupport.PAD_Y, z + 0.5, GUN));
            context.waitTicks(20);
            ClientGameTestSupport.screenshot(context, "laser_gun_displays");
            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void mannequin(TestServerContext server, double x, int z, int yaw, String mainHand) {
        server.runCommand(String.format(
                "summon mannequin %.1f %d %d {Rotation:[%df,0f],main_hand:\"%s\",hide_description:1b,immovable:1b,"
                        + "equipment:{mainhand:%s}}",
                x, ClientGameTestSupport.PAD_Y, z, yaw, mainHand, GUN));
    }
}
