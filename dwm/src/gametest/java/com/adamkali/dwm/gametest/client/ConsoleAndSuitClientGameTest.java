package com.adamkali.dwm.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

/**
 * Meshes that load from JSON model files: the First Doctor console with its controls
 * ({@code first_doctor_console}), the EVA suit on a mannequin ({@code eva_suit}, front and back) and the
 * radiation meter in an item frame and held ({@code radiation_meter}).
 */
public class ConsoleAndSuitClientGameTest implements FabricClientGameTest {
    private static final int Z = 4;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            var server = world.getServer();
            ClientGameTestSupport.stage(server, "creative");
            server.runCommand(String.format("setblock 0 %d %d dwm:first_doctor_console",
                    ClientGameTestSupport.PAD_Y, Z + 2));
            server.runCommand(String.format("tp @a 0 %d 0 0 35", ClientGameTestSupport.CAMERA_Y + 2));
            ClientGameTestSupport.toggleHud(context);
            context.getInput().lookAt(new BlockPos(0, ClientGameTestSupport.PAD_Y, Z + 2));
            context.waitTicks(30);
            ClientGameTestSupport.screenshot(context, "first_doctor_console");

            server.runCommand("setblock 0 " + ClientGameTestSupport.PAD_Y + " " + (Z + 2) + " air");
            server.runCommand(String.format("tp @a 0 %d 0 0 0", ClientGameTestSupport.CAMERA_Y));
            context.getInput().lookAt(new BlockPos(0, ClientGameTestSupport.PAD_Y + 1, Z));
            server.runCommand(String.format(
                    "summon mannequin -1.2 %d %d {Rotation:[180f,0f],hide_description:1b,immovable:1b,equipment:{"
                            + "head:{id:\"dwm:eva_suit_helmet\",count:1},chest:{id:\"dwm:eva_suit_chestplate\",count:1},"
                            + "legs:{id:\"dwm:eva_suit_leggings\",count:1},feet:{id:\"dwm:eva_suit_boots\",count:1}}}",
                    ClientGameTestSupport.PAD_Y, Z));
            server.runCommand(String.format(
                    "summon mannequin 1.2 %d %d {Rotation:[0f,0f],hide_description:1b,immovable:1b,"
                            + "main_hand:\"right\",equipment:{"
                            + "head:{id:\"dwm:eva_suit_helmet\",count:1},chest:{id:\"dwm:eva_suit_chestplate\",count:1},"
                            + "legs:{id:\"dwm:eva_suit_leggings\",count:1},feet:{id:\"dwm:eva_suit_boots\",count:1},"
                            + "mainhand:{id:\"dwm:radiation_meter\",count:1}}}",
                    ClientGameTestSupport.PAD_Y, Z));
            context.waitTicks(20);
            ClientGameTestSupport.screenshot(context, "eva_suit");

            server.runCommand(String.format("setblock 0 %d %d stone", ClientGameTestSupport.CAMERA_Y, Z + 3));
            server.runCommand(String.format(
                    "summon item_frame 0 %d %d {Facing:2b,Fixed:1b,Item:{id:\"dwm:radiation_meter\",count:1}}",
                    ClientGameTestSupport.CAMERA_Y, Z + 2));
            context.getInput().lookAt(new BlockPos(0, ClientGameTestSupport.CAMERA_Y, Z + 2));
            context.waitTicks(20);
            ClientGameTestSupport.screenshot(context, "radiation_meter");
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
