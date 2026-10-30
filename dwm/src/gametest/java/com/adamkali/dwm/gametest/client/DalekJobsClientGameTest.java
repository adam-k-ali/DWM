package com.adamkali.dwm.gametest.client;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekJob;
import com.adamkali.dwm.entity.DalekJobFx;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.List;

/**
 * Screenshot {@code dalek_jobs.png}: an on-duty sentry (eyestalk mid-sweep) beside an on-duty technician
 * (plunger working) and an off-duty sentry for contrast.
 *
 * <p>The Daleks are frozen (NoAI, silent) with their duty flag set by hand, since the job goals that normally
 * set it do not run without AI. The blend itself still eases on the client, so the wait must outlast it.
 */
public class DalekJobsClientGameTest implements FabricClientGameTest {
    private static final int SPAWN_Z = 5;
    private static final double SPACING = 2.5;
    /** Long enough for the pose blend to settle at full strength. */
    private static final int BLEND_TICKS = (int) Math.ceil(1.0F / DalekJobFx.BLEND_PER_TICK) + 20;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            ClientGameTestSupport.stage(world.getServer(), "spectator");
            ClientGameTestSupport.toggleHud(context);
            try {
                world.getServer().runOnServer(server -> {
                    ServerLevel level = server.overworld();
                    spawn(level, -SPACING, DalekJob.SENTRY, true);
                    spawn(level, 0, DalekJob.TECHNICIAN, true);
                    spawn(level, SPACING, DalekJob.SENTRY, false);
                });
                world.getServer().runCommand("tp @a 0 " + ClientGameTestSupport.CAMERA_Y + " 0 0 0");
                context.waitTicks(5);
                context.getInput().lookAt(new BlockPos(0, ClientGameTestSupport.CAMERA_Y, SPAWN_Z));
                context.waitTicks(BLEND_TICKS);
                ClientGameTestSupport.screenshot(context, "dalek_jobs");
            } finally {
                world.getServer().runOnServer(server -> {
                    ServerLevel level = server.overworld();
                    for (DalekEntity dalek : level.getEntities(DWMEntityTypes.DALEK, dalek -> true)) {
                        dalek.discard();
                    }
                });
            }
            ClientGameTestSupport.toggleHud(context);
        }
    }

    private static void spawn(ServerLevel level, double x, DalekJob job, boolean onDuty) {
        DalekEntity dalek = DWMEntityTypes.DALEK.create(level, EntitySpawnReason.COMMAND);
        if (dalek == null) {
            throw new AssertionError("Could not create a Dalek");
        }
        BlockPos at = BlockPos.containing(x, ClientGameTestSupport.PAD_Y, SPAWN_Z);
        dalek.setPos(x, ClientGameTestSupport.PAD_Y, SPAWN_Z);
        dalek.setYRot(180.0F);
        dalek.setNoAi(true);
        dalek.setSilent(true);
        dalek.setPersistenceRequired();
        dalek.assignJob(job, at, 180.0F, 0, List.of());
        dalek.setOnDuty(onDuty);
        level.addFreshEntity(dalek);
    }
}
