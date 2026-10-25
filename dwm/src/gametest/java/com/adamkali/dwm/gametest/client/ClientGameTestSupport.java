package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.config.DWMConfig;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared staging for client GameTests: a deterministic stone pad under a clear noon sky,
 * with the player at {@link #CAMERA_Y} on the pad's near edge looking toward +Z.
 */
final class ClientGameTestSupport {
    /** Top surface of the stage pad; things placed "on the pad" go at this Y. */
    static final int PAD_Y = 100;
    static final int CAMERA_Y = 101;

    private ClientGameTestSupport() {
    }

    /** Door portals off, clear weather, noon, spectator player at (0, {@link #CAMERA_Y}, 0) facing +Z, and a pad ahead of it. */
    static void stage(TestServerContext server, String gameMode) {
        // Door portals allocate extra FBOs/pipelines; none of the scenarios cover them yet, and the
        // toggle is process-wide, so set it here instead of relying on which test ran first.
        DWMConfig.setBoolean(DWMConfig.ENABLE_DOOR_PORTALS, false);
        // Keep chat clean for screenshots (advancement chat lines, "game mode updated", ...).
        server.runCommand("gamerule send_command_feedback false");
        server.runCommand("gamerule show_advancement_messages false");
        server.runCommand("time set noon");
        server.runCommand("weather clear");
        server.runCommand("gamemode " + gameMode + " @a");
        server.runCommand("tp @a 0 " + CAMERA_Y + " 0 0 0");
        buildPad(server, 0);
    }

    /** Stone pad (19 x 15) whose near edge is centred on {@code centerX}; the camera stands at z = 0. */
    static void buildPad(TestServerContext server, int centerX) {
        server.runOnServer(minecraft -> {
            var level = minecraft.overworld();
            for (int x = centerX - 9; x <= centerX + 9; x++) {
                for (int z = -2; z <= 12; z++) {
                    level.setBlock(new BlockPos(x, PAD_Y - 1, z), Blocks.STONE.defaultBlockState(), 3);
                }
            }
        });
    }

    /** F1: hides the HUD so command feedback in chat doesn't cover the scene. Call again to restore it. */
    static void toggleHud(ClientGameTestContext context) {
        context.getInput().pressKey(options -> options.keyToggleGui);
    }

    /** Dismisses any advancement/recipe toasts currently on screen. */
    static void clearToasts(ClientGameTestContext context) {
        context.runOnClient(client -> client.gui.toastManager().clear());
    }

    /** Takes a screenshot and fails if the file is missing or empty. */
    static Path screenshot(ClientGameTestContext context, String name) {
        Path screenshot = context.takeScreenshot(name);
        try {
            if (!Files.exists(screenshot) || Files.size(screenshot) == 0) {
                throw new AssertionError("Screenshot missing or empty: " + screenshot);
            }
        } catch (IOException e) {
            throw new AssertionError("Could not read screenshot " + screenshot, e);
        }
        return screenshot;
    }
}
