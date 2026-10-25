package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.config.DWMConfig;
import com.adamkali.dwm.render.portal.PortalKey;
import com.adamkali.dwm.render.portal.PortalScheduler;
import com.adamkali.dwm.render.portal.PortalSupport;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisInteriorPreloadService;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Predicate;

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

    /** Same as {@link #stage(TestServerContext, String, boolean)} with door portals off. */
    static void stage(TestServerContext server, String gameMode) {
        stage(server, gameMode, false);
    }

    /**
     * Door portals on/off, clear weather, noon, player at (0, {@link #CAMERA_Y}, 0) facing +Z, and a pad ahead of it.
     * <p>
     * Door portals allocate extra FBOs/pipelines, so only the portal scenarios turn them on. The toggle is
     * process-wide, so every scenario sets it explicitly instead of relying on which test ran first.
     */
    static void stage(TestServerContext server, String gameMode, boolean doorPortals) {
        DWMConfig.setBoolean(DWMConfig.ENABLE_DOOR_PORTALS, doorPortals);
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

    // ---- Door portals (BOTI = look into the exterior door, SOTO = look out of the interior door) ----

    /**
     * Places a TARDIS exterior at {@code pos}, force-builds its interior and opens the door fully.
     *
     * @return the TARDIS id
     */
    static UUID placeTardisWithOpenDoor(TestServerContext server, BlockPos pos) {
        return server.computeOnServer(minecraft -> {
            ServerLevel level = minecraft.overworld();
            level.setBlock(pos, DWMBlocks.TARDIS_BLOCK.defaultBlockState(), 3);
            if (!(level.getBlockEntity(pos) instanceof TardisBlockEntity shell)) {
                throw new AssertionError("Expected a TardisBlockEntity at " + pos);
            }
            UUID id = shell.getTardisId();
            if (TardisInteriorService.ensureInterior(level, shell) == null) {
                throw new AssertionError("Could not build the interior for TARDIS " + id);
            }
            TardisDataModel model = TardisDataLoader.get(id);
            if (model == null) {
                throw new AssertionError("Missing TARDIS data for " + id);
            }
            model.doorState.isOpen = true;
            model.doorState.doorSwing = 1.0f;
            model.setChanged();
            PortalStreamSyncService.setMetaChanged(id);
            return id;
        });
    }

    /** Waits until the interior is generated, preload jobs are done and the console room light has propagated. */
    static void waitForInteriorReady(TestServerContext server, BlockPos shellPos, UUID id) {
        waitOrFail("interior of TARDIS " + id + " to be built and lit", () -> server.waitFor(minecraft -> {
            ServerLevel interior = minecraft.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
            return interior != null
                    && minecraft.overworld().getBlockEntity(shellPos) instanceof TardisBlockEntity shell
                    && shell.isInteriorGenerated()
                    && shell.getInteriorEntrance() != null
                    && !TardisInteriorPreloadService.hasJob(id)
                    && BotiInteriorSampler.isFootprintLightReady(interior, TardisPlotAllocator.plotOrigin(id));
        }, PORTAL_WAIT_TICKS));
    }

    /**
     * Waits until the client has baked portal meshes and a portal composite texture has actually been rendered.
     * <p>
     * Until then the door shows a flat dark blue-grey placeholder (0xFF203040), which looks plausible in a
     * screenshot, so a picture alone is not proof that the portal works. The failure message says so.
     */
    static void waitForPortalRendered(ClientGameTestContext context, PortalStreamKind kind, UUID id) {
        PortalKey key = kind == PortalStreamKind.BOTI ? PortalKey.boti(id) : PortalKey.soto(id);
        waitOrFail(kind + " portal meshes for TARDIS " + id, () -> context.waitFor(
                (Predicate<net.minecraft.client.Minecraft>) client -> SotoGhostMeshCache.hasMeshes(kind, id),
                PORTAL_WAIT_TICKS));
        waitOrFail(kind + " portal composite texture for TARDIS " + id
                + " (a dark blue-grey door means the placeholder is showing)", () -> context.waitFor(
                (Predicate<net.minecraft.client.Minecraft>) client -> PortalScheduler.peekCompositeTexture(key).available(),
                PORTAL_WAIT_TICKS));
        // The block entity renderer reads the composite one frame after it is produced.
        context.waitTicks(2);
    }

    /** Fails if portal rendering was disabled for the session (e.g. the portal framebuffer was incomplete). */
    static void assertPortalsStillAvailable(ClientGameTestContext context) {
        if (!context.computeOnClient(client -> PortalSupport.isAvailable())) {
            throw new AssertionError("Door portal rendering was disabled for this session; "
                    + "check the client log for 'Disabling TARDIS door portal rendering'");
        }
    }

    private static final int PORTAL_WAIT_TICKS = 400;

    private static void waitOrFail(String what, Runnable wait) {
        try {
            wait.run();
        } catch (RuntimeException | AssertionError e) {
            throw new AssertionError("Timed out waiting for " + what, e);
        }
    }
}
