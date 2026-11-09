package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
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
 */
public class TardisPortalPerfClientGameTest implements FabricClientGameTest {
    private static final int MEASURE_TICKS = 100;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            BlockPos tardisPos = new BlockPos(0, ClientGameTestSupport.PAD_Y, 4);
            List<PortalPerfMeasure.Sample> samples = new ArrayList<>();

            ClientGameTestSupport.stage(world.getServer(), "spectator", true);
            UUID id = ClientGameTestSupport.placeTardisWithOpenDoor(world.getServer(), tardisPos);
            ClientGameTestSupport.waitForInteriorReady(world.getServer(), tardisPos, id);
            ClientGameTestSupport.toggleHud(context);

            // ---- BOTI: camera at (0, CAMERA_Y, 0); the exterior door faces north (towards the camera) ----
            context.getInput().lookAt(tardisPos.above());
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.BOTI, id);
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

            ClientGameTestSupport.setDoorOpen(world.getServer(), id, true);
            ClientGameTestSupport.waitForPortalRendered(context, PortalStreamKind.SOTO, id);
            context.waitTicks(40);
            world.getServer().runCommand(tpInterior + "0 0");
            PortalPerfMeasure.Sample sotoCulled = PortalPerfMeasure.measure(context, "soto_open_culled", MEASURE_TICKS);

            world.getServer().runCommand(tpInterior + "180 0");
            PortalPerfMeasure.Sample sotoVisible = PortalPerfMeasure.measure(context, "soto_open_visible", MEASURE_TICKS);
            ClientGameTestSupport.assertPortalsStillAvailable(context);
            ClientGameTestSupport.screenshot(context, "portal_perf_soto_visible");

            samples.add(sotoBase);
            samples.add(sotoCulled);
            samples.add(sotoVisible);
            PortalPerfMeasure.assertCullingEffective("SOTO", sotoBase, sotoCulled, sotoVisible);

            PortalPerfMeasure.writeReport("portal_perf.json", samples);
            ClientGameTestSupport.toggleHud(context);
        }
    }
}
