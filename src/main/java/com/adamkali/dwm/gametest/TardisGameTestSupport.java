package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;

import java.util.UUID;

/**
 * Shared setup helpers for TARDIS GameTests that need exterior shells, pads, and models.
 */
public final class TardisGameTestSupport {
    private TardisGameTestSupport() {
    }

    public static void configureSaveDirectory(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");
    }

    /** Stone pad + air shell/door column suitable for {@link com.adamkali.dwm.tardis.logic.LandingSiteLogic}. */
    public static void placeLandingPad(GameTestHelper context, BlockPos shellRel, Direction doorFacing) {
        context.setBlock(shellRel.below(), Blocks.STONE);
        context.setBlock(shellRel, Blocks.AIR);
        context.setBlock(shellRel.above(), Blocks.AIR);
        BlockPos doorRel = shellRel.relative(doorFacing);
        context.setBlock(doorRel, Blocks.AIR);
        context.setBlock(doorRel.above(), Blocks.AIR);
    }

    public static TardisBlockEntity placeExteriorShell(GameTestHelper context, BlockPos shellRel) {
        configureSaveDirectory(context);
        placeLandingPad(context, shellRel, Direction.NORTH);
        context.setBlock(shellRel, DWMBlocks.TARDIS_BLOCK);
        BlockPos shellAbs = context.absolutePos(shellRel);
        if (!(context.getLevel().getBlockEntity(shellAbs) instanceof TardisBlockEntity exterior)) {
            throw new AssertionError("Expected TardisBlockEntity at " + shellAbs);
        }
        UUID tardisId = exterior.getTardisId();
        TardisDataModel model = TardisDataLoader.getOrCreate(tardisId);
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                shellAbs.getX(),
                shellAbs.getY(),
                shellAbs.getZ(),
                0
        );
        return exterior;
    }

    public static void forceDoorsFullyOpen(UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Missing TARDIS model for " + tardisId);
        }
        model.doorState.isOpen = true;
        model.doorState.doorSwing = 1.0f;
        model.setChanged();
    }

    public static void forceDoorsClosed(UUID tardisId) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Missing TARDIS model for " + tardisId);
        }
        model.doorState.isOpen = false;
        model.doorState.doorSwing = 0.0f;
        model.setChanged();
    }

    /**
     * GameTest {@link GameTestHelper#makeMockPlayer} is not a {@link ServerPlayer}.
     * {@link GameTestHelper#makeMockServerPlayerInLevel()} wires an embedded connection so
     * teleport and {@code ServerPlayNetworking.send} paths do not NPE.
     */
    @SuppressWarnings("removal")
    public static ServerPlayer mockServerPlayer(GameTestHelper context) {
        return context.makeMockServerPlayerInLevel();
    }

    /**
     * Seeds a waypoint destination mode aimed at {@code targetAbs} in the GameTest world.
     */
    public static TardisWaypoint selectWaypointDestination(TardisDataModel model, BlockPos targetAbs, int rotation) {
        TardisWaypoint waypoint = new TardisWaypoint(
                UUID.randomUUID(),
                "GameTest Pad",
                model.exteriorDimension,
                targetAbs.getX(),
                targetAbs.getY(),
                targetAbs.getZ(),
                rotation
        );
        model.getWaypoints().add(waypoint);
        model.selectedWaypointId = waypoint.id;
        model.setDestinationMode(DestinationMode.WAYPOINT);
        model.setChanged();
        return waypoint;
    }

    public static ServerLevel tardisDimensionOrNull(GameTestHelper context) {
        return context.getLevel().getServer()
                .getLevel(com.adamkali.dwm.tardis.interior.TardisDimensions.TARDIS_WORLD_KEY);
    }
}
