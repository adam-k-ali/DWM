package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.gui.ArsScreen;
import com.adamkali.dwm.network.ArsGrowC2SPayload;
import com.adamkali.dwm.network.ServerPayloadTypeRegistry;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.interior.ars.ArsCell;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.logic.ArsLayoutLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * The ARS console control and map screen on a real TARDIS interior, at the smallest common GUI size
 * (427×240). Saves {@code ars_console_control}, {@code ars_screen_empty}, {@code ars_screen_ghost},
 * {@code ars_screen_layout} (console room centred, pieces on both sides), {@code ars_world_console_doorway}
 * (east), {@code ars_world_west_doorway}, {@code ars_world_east_lobby} and {@code ars_world_library}. The first
 * piece is grown by clicking the screen (real C2S path); the rest go through the same server handler on the
 * server thread.
 */
public class TardisArsConsoleClientGameTest implements FabricClientGameTest {
    private static final BlockPos SHELL_POS = new BlockPos(0, ClientGameTestSupport.PAD_Y, 6);
    private static final Identifier STRAIGHT = Identifier.fromNamespaceAndPath("dwm", "corridor_straight");
    private static final Identifier CROSS = Identifier.fromNamespaceAndPath("dwm", "corridor_cross");
    private static final Identifier BEDROOM = Identifier.fromNamespaceAndPath("dwm", "bedroom");
    private static final Identifier LIBRARY = Identifier.fromNamespaceAndPath("dwm", "library");
    private static final Identifier DEAD_END = Identifier.fromNamespaceAndPath("dwm", "corridor_dead_end");
    private static final double EYE_HEIGHT = 1.62;
    private static final int LIGHT_WAIT_TICKS = 200;
    private static final int RENDER_TICKS = 40;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            TestServerContext server = world.getServer();
            ClientGameTestSupport.stage(server, "creative");
            UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(server, SHELL_POS);
            ClientGameTestSupport.waitForInteriorReady(server, SHELL_POS, id);
            BlockPos origin = TardisPlotAllocator.plotOrigin(id);
            BlockPos grid = ArsGrid.gridOrigin(origin);
            BlockPos consolePos = origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
            server.runOnServer(minecraft -> {
                ServerPlayer player = minecraft.getPlayerList().getPlayers().getFirst();
                if (!TardisOwnershipLogic.tryClaimOnEnter(id, player.getUUID())) {
                    throw new AssertionError("Expected the test player to claim TARDIS " + id);
                }
            });
            ensureSmallestGui(context);
            ClientGameTestSupport.toggleHud(context);

            // 1. The ARS control on Panel5's bottom row, seen from just outside that panel.
            Direction facing = server.computeOnServer(minecraft -> interior(minecraft)
                    .getBlockState(consolePos).getValue(FirstDoctorConsoleBlock.FACING));
            FirstDoctorConsoleControls.InteractionPose pose =
                    FirstDoctorConsoleControls.interactionPose(LookTarget.ARS, consolePos, facing);
            Vec3 control = pose.position().add(0, pose.height() / 2, 0);
            Vec3 outward = new Vec3(control.x - (consolePos.getX() + 0.5), 0, control.z - (consolePos.getZ() + 0.5))
                    .normalize();
            Vec3 eye = control.add(outward.scale(1.3)).add(0, 0.55, 0);
            aim(context, server, eye.subtract(0, EYE_HEIGHT, 0), control);
            context.waitTicks(RENDER_TICKS);
            ClientGameTestSupport.screenshot(context, "ars_console_control");

            // 2. Use the control: the real block handler sends OpenArsScreen.
            InteractionResult result = server.computeOnServer(minecraft -> FirstDoctorConsoleBlock.activateControl(
                    LookTarget.ARS, interior(minecraft), consolePos, minecraft.getPlayerList().getPlayers().getFirst()));
            if (result != InteractionResult.SUCCESS) {
                throw new AssertionError("ARS control should open the screen for the owner, got " + result);
            }
            context.waitForScreen(ArsScreen.class);
            ArsScreen screen = context.computeOnClient(client -> (ArsScreen) client.gui.screen());
            moveCursor(context, -1000, -1000);
            context.waitTicks(10);
            ClientGameTestSupport.screenshot(context, "ars_screen_empty");

            // 3. Pick the straight corridor, rotate it east-west (R) and hover the east root cell.
            clickAt(context, context.computeOnClient(client -> screen.paletteRowCenter(STRAIGHT)), GLFW.GLFW_MOUSE_BUTTON_LEFT);
            context.getInput().pressKey(GLFW.GLFW_KEY_R);
            moveCursor(context, context.computeOnClient(client -> screen.cellCenter(new ArsCell(5, 4))));
            context.waitTicks(5);
            boolean ghostOk = context.computeOnClient(client -> STRAIGHT.equals(screen.state().selectedPiece())
                    && screen.state().rotation() == 1
                    && screen.placementAt(new ArsCell(5, 4)) == ArsLayoutLogic.PlaceResult.OK
                    && screen.placementAt(new ArsCell(2, 4)) == ArsLayoutLogic.PlaceResult.OK);
            if (!ghostOk) {
                throw new AssertionError("Expected a valid east-west straight corridor ghost on both root cells");
            }
            ClientGameTestSupport.screenshot(context, "ars_screen_ghost");

            // 4. Grow it by clicking (C2S), then the rest through the server handler on both sides of the
            // console room; select the library.
            context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            waitForLayout(context, screen, 1);
            grow(server, new ArsGrowC2SPayload(id, CROSS, 6, 4, 0));
            grow(server, new ArsGrowC2SPayload(id, BEDROOM, 6, 5, 0));
            grow(server, new ArsGrowC2SPayload(id, STRAIGHT, 2, 4, 1));
            grow(server, new ArsGrowC2SPayload(id, CROSS, 1, 4, 0));
            grow(server, new ArsGrowC2SPayload(id, LIBRARY, 1, 5, 0));
            waitForLayout(context, screen, 6);
            if (context.computeOnClient(client -> client.gui.screen()) != screen) {
                throw new AssertionError("The ARS screen should update in place, not be replaced");
            }
            clickAt(context, context.computeOnClient(client -> screen.cellCenter(new ArsCell(2, 6))), GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            moveCursor(context, -1000, -1000);
            context.waitTicks(5);
            if (context.computeOnClient(client -> screen.state().selectedCell()) == null) {
                throw new AssertionError("Right-clicking the library should select it for jettison");
            }
            ClientGameTestSupport.screenshot(context, "ars_screen_layout");
            context.setScreen(() -> null);

            // The reply to a request made after closing is a refresh: it must not reopen the screen.
            grow(server, new ArsGrowC2SPayload(id, DEAD_END, 6, 3, 2));
            context.waitTicks(10);
            if (context.computeOnClient(client -> client.gui.screen()) != null) {
                throw new AssertionError("A grow reply reopened a closed screen: "
                        + context.computeOnClient(client -> client.gui.screen()));
            }

            // 5-8. In the world, in spectator so the camera can stand anywhere. Positions are grid-local: the console
            // room spans x 27..37, z 27..43; its doorways are at z 34..35 in the east (x 37) and west (x 27) walls.
            server.runCommand("gamemode spectator @a");
            shoot(context, server, grid, new Vec3(34.0, 1, 35.0), new Vec3(38.5, 2.2, 35.0), "ars_world_console_doorway");
            shoot(context, server, grid, new Vec3(31.0, 1, 35.0), new Vec3(18.0, 2.2, 36.0), "ars_world_west_doorway");
            shoot(context, server, grid, new Vec3(39.0, 1, 36.0), new Vec3(53.0, 2.4, 36.0), "ars_world_east_lobby");
            shoot(context, server, grid, new Vec3(12.0, 1, 42.0), new Vec3(20.0, 3.0, 52.0), "ars_world_library");
            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static ServerLevel interior(net.minecraft.server.MinecraftServer minecraft) {
        ServerLevel level = minecraft.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (level == null) {
            throw new AssertionError("dwm:tardis is not loaded");
        }
        return level;
    }

    /** Client GameTests run in an 854×480 window; at GUI scale 2 that is the 427×240 the screen is sized for. */
    private static void ensureSmallestGui(ClientGameTestContext context) {
        context.getInput().resizeWindow(854, 480);
        context.waitTicks(2);
        int[] size = context.computeOnClient(client -> new int[]{
                client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight()});
        if (size[0] != 427 || size[1] != 240) {
            throw new AssertionError("Expected a 427x240 scaled GUI, got " + size[0] + "x" + size[1]);
        }
    }

    private static void grow(TestServerContext server, ArsGrowC2SPayload payload) {
        boolean grown = server.computeOnServer(minecraft -> ServerPayloadTypeRegistry.safelyHandleArsGrow(
                payload, minecraft.getPlayerList().getPlayers().getFirst()));
        if (!grown) {
            throw new AssertionError("Expected " + payload + " to grow");
        }
    }

    private static void waitForLayout(ClientGameTestContext context, ArsScreen screen, int pieces) {
        context.waitFor((Predicate<Minecraft>) client -> screen.state().layout().size() == pieces, 100);
    }

    private static void moveCursor(ClientGameTestContext context, int[] guiPoint) {
        moveCursor(context, guiPoint[0], guiPoint[1]);
    }

    private static void moveCursor(ClientGameTestContext context, double guiX, double guiY) {
        double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
        context.getInput().setCursorPos(guiX * scale, guiY * scale);
    }

    private static void clickAt(ClientGameTestContext context, int[] guiPoint, int button) {
        if (guiPoint == null) {
            throw new AssertionError("Nothing to click");
        }
        moveCursor(context, guiPoint);
        context.waitTicks(1);
        context.getInput().pressMouse(button);
        context.waitTicks(2);
    }

    /** Teleports to a grid-local feet position in dwm:tardis, waits for block light there, aims and shoots. */
    private static void shoot(ClientGameTestContext context, TestServerContext server, BlockPos grid,
                              Vec3 localFeet, Vec3 localTarget, String name) {
        Vec3 feet = Vec3.atLowerCornerOf(grid).add(localFeet);
        BlockPos probe = BlockPos.containing(feet.add(0, 1, 0));
        server.waitFor(minecraft -> interior(minecraft).getBrightness(LightLayer.BLOCK, probe) > 0, LIGHT_WAIT_TICKS);
        aim(context, server, feet, Vec3.atLowerCornerOf(grid).add(localTarget));
        context.waitTicks(RENDER_TICKS);
        ClientGameTestSupport.screenshot(context, name);
    }

    /** Teleports into dwm:tardis, waits for the position packet to land, then aims at {@code target}. */
    private static void aim(ClientGameTestContext context, TestServerContext server, Vec3 feet, Vec3 target) {
        server.runCommand("execute in " + TardisDimensions.TARDIS_WORLD_KEY.identifier()
                + " run tp @a " + feet.x + " " + feet.y + " " + feet.z);
        context.waitTicks(5);
        Vec3 delta = target.subtract(feet.add(0, EYE_HEIGHT, 0));
        float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
        context.getInput().lookAt(yaw, pitch);
    }
}
