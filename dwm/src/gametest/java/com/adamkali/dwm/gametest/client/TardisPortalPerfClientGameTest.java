package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.render.portal.PortalKey;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Portal visibility culling + performance: for BOTI (outside the open exterior door) and SOTO (inside, facing the
 * interior door) measures frame time and schedule/cull counts with the door closed (baseline), open but out of
 * view (culled) and open and in view (visible). The impact of a portal becoming visible is the
 * {@code visible - baseline} delta in {@code perf/portal_perf.json}.
 *
 * <p>Ghost-mesh GPU work is reported too: buffers / bytes uploaded by each portal's initial fill, by an edit
 * burst in one chunk (must upload far less than the fill: only the edited chunk is re-uploaded), and draw calls
 * per frame with the cached portal frame forced to redraw every tick. Leaving the world must close every buffer.
 */
public class TardisPortalPerfClientGameTest implements FabricClientGameTest {
    private static final int MEASURE_TICKS = 100;

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> SotoGhostMeshCache.resetBufferStats());
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            PortalPerfMeasure.Counters fillStart = PortalPerfMeasure.Counters.now();
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);
            List<PortalPerfMeasure.Sample> samples = new ArrayList<>();

            ClientGameTestSupport.stage(world.getServer(), "spectator", true);
            UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(world.getServer(), tardisPos);
            ClientGameTestSupport.waitForInteriorReady(world.getServer(), tardisPos, id);
            ClientGameTestSupport.toggleHud(context);

            // ---- BOTI: camera at (0, CAMERA_Y, 0); the exterior door faces north (towards the camera) ----
            context.getInput().lookAt(tardisPos.above());
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.BOTI, id);
            PortalPerfMeasure.Sample botiFill = PortalPerfMeasure.Counters.now().since("boti_fill", fillStart);
            ClientGameTestSupport.setDoorOpen(world.getServer(), id, false);
            PortalPerfMeasure.Sample botiBase = PortalPerfMeasure.measure(context, "boti_closed_baseline", MEASURE_TICKS);

            ClientGameTestSupport.setDoorOpen(world.getServer(), id, true);
            world.getServer().runCommand("tp @a 0 " + ClientGameTestSupport.CAMERA_Y + " 0 180 0");
            PortalPerfMeasure.Sample botiCulled = PortalPerfMeasure.measure(context, "boti_open_culled", MEASURE_TICKS);

            world.getServer().runCommand("tp @a 0 " + ClientGameTestSupport.CAMERA_Y + " 0 0 0");
            context.getInput().lookAt(tardisPos.above());
            PortalPerfMeasure.Sample botiVisible = PortalPerfMeasure.measure(context, "boti_open_visible", MEASURE_TICKS);
            ClientGameTestSupport.assertPortalsStillAvailable(context);
            ClientGameTestSupport.screenshot(context, "portal_perf_boti_visible");

            samples.add(botiBase);
            samples.add(botiCulled);
            samples.add(botiVisible);
            PortalPerfMeasure.assertCullingEffective("BOTI", botiBase, botiCulled, botiVisible);

            // ---- SOTO: viewer behind the interior door bank (z = 5.5), door faces south ----
            BlockPos plot = TardisPlotAllocator.plotOrigin(id);
            String tpInterior = String.format("execute in dwm:tardis run tp @a %.1f %.1f %.1f ",
                    plot.getX() + 5.5, plot.getY() + 1.0, plot.getZ() + 5.5);
            ClientGameTestSupport.setDoorOpen(world.getServer(), id, false);
            world.getServer().runCommand(tpInterior + "180 0");
            PortalPerfMeasure.Sample sotoBase = PortalPerfMeasure.measure(context, "soto_closed_baseline", MEASURE_TICKS);

            PortalPerfMeasure.Counters sotoFillStart = PortalPerfMeasure.Counters.now();
            ClientGameTestSupport.setDoorOpen(world.getServer(), id, true);
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.SOTO, id);
            context.waitTicks(40);
            PortalPerfMeasure.Sample sotoFill = PortalPerfMeasure.Counters.now().since("soto_fill", sotoFillStart);
            world.getServer().runCommand(tpInterior + "0 0");
            PortalPerfMeasure.Sample sotoCulled = PortalPerfMeasure.measure(context, "soto_open_culled", MEASURE_TICKS);

            world.getServer().runCommand(tpInterior + "180 0");
            PortalPerfMeasure.Sample sotoVisible = PortalPerfMeasure.measure(context, "soto_open_visible", MEASURE_TICKS);
            ClientGameTestSupport.assertPortalsStillAvailable(context);
            ClientGameTestSupport.screenshot(context, "portal_perf_soto_visible");

            PortalPerfMeasure.Sample sotoRedraw = PortalPerfMeasure.measure(
                    context, "soto_visible_redraw", MEASURE_TICKS, PortalKey.soto(id));
            if (sotoRedraw.drawCalls() <= 0) {
                throw new AssertionError("SOTO redraw issued no ghost-mesh draw calls: " + sotoRedraw);
            }

            // Edit burst: blocks inside one exterior chunk (away from its edges) re-upload only that chunk.
            PortalPerfMeasure.Counters editStart = PortalPerfMeasure.Counters.now();
            // Both streams' initial fills (the SOTO exterior stream warms up during the BOTI phase).
            PortalPerfMeasure.Sample allFill = editStart.since("all_fill_before_edit", fillStart);
            world.getServer().runCommand("fill 6 " + ClientGameTestSupport.CAMERA_Y + " 6 9 "
                    + ClientGameTestSupport.CAMERA_Y + " 9 minecraft:gold_block");
            // Commands bypass the stream's player-edit hooks; mark the edited blocks dirty like a player edit would.
            world.getServer().runOnServer(server -> {
                for (BlockPos pos : BlockPos.betweenClosed(6, ClientGameTestSupport.CAMERA_Y, 6,
                        9, ClientGameTestSupport.CAMERA_Y, 9)) {
                    PortalStreamSyncService.markDirtyAt(server.overworld(), pos.immutable());
                }
            });
            context.waitTicks(60);
            PortalPerfMeasure.Sample sotoEdit = PortalPerfMeasure.Counters.now().since("soto_edit_burst", editStart);
            if (sotoEdit.buffersCreated() <= 0 || sotoEdit.buffersCreated() >= allFill.buffersCreated()
                    || sotoEdit.bytesUploaded() * 4 >= allFill.bytesUploaded()) {
                throw new AssertionError("SOTO edit burst must re-upload only the edited chunk: edit="
                        + sotoEdit + " fill=" + allFill);
            }

            samples.add(sotoBase);
            samples.add(sotoCulled);
            samples.add(sotoVisible);
            samples.add(botiFill);
            samples.add(sotoFill);
            samples.add(allFill);
            samples.add(sotoRedraw);
            samples.add(sotoEdit);
            PortalPerfMeasure.assertCullingEffective("SOTO", sotoBase, sotoCulled, sotoVisible);

            PortalPerfMeasure.writeReport("portal_perf.json", samples);
            ClientGameTestSupport.toggleHud(context);
        }
        context.waitTicks(5);
        long created = SotoGhostMeshCache.buffersCreated();
        long closed = SotoGhostMeshCache.buffersClosed();
        System.out.println("[portal-perf] after world close: buffers created=" + created + " closed=" + closed);
        if (created != closed) {
            throw new AssertionError("Ghost mesh buffers leaked after leaving the world: created=" + created
                    + " closed=" + closed);
        }
    }
}
