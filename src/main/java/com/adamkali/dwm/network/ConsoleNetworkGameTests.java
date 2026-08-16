package com.adamkali.dwm.network;

import com.adamkali.dwm.gametest.TardisGameTestSupport;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.DestinationMode;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Console C2S handler GameTests (package-private handlers in {@link ServerPayloadTypeRegistry}).
 */
public class ConsoleNetworkGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void saveWaypoint_RejectedWhileTraveling(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        TardisGameTestSupport.configureSaveDirectory(context);
        TardisDataModel model = TardisDataLoader.create();
        BlockPos shellAbs = context.absolutePos(new BlockPos(2, 2, 2));
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                shellAbs.getX(), shellAbs.getY(), shellAbs.getZ(), 0);

        // Mark traveling without needing a full demat loop.
        model.setTravelPhase(TardisTravelPhase.IN_FLIGHT);
        model.setChanged();

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean saved = ServerPayloadTypeRegistry.safelyHandleSaveWaypoint(
                new SaveWaypointC2SPayload(model.uuid, "Should Fail"),
                player
        );
        if (saved) {
            throw new AssertionError("Waypoint save must be rejected while traveling");
        }
        if (!model.getWaypoints().isEmpty()) {
            throw new AssertionError("No waypoint should be persisted when travel_in_flight");
        }

        model.setTravelPhase(TardisTravelPhase.IDLE);
        TardisTravelService.clearActiveForTests();
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void saveWaypoint_PersistsWhenIdleWithExterior(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        TardisGameTestSupport.configureSaveDirectory(context);
        TardisDataModel model = TardisDataLoader.create();
        BlockPos shellAbs = context.absolutePos(new BlockPos(2, 2, 2));
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                shellAbs.getX(), shellAbs.getY(), shellAbs.getZ(), 0);

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean saved = ServerPayloadTypeRegistry.safelyHandleSaveWaypoint(
                new SaveWaypointC2SPayload(model.uuid, "GameTest Pad"),
                player
        );
        if (!saved) {
            throw new AssertionError("Expected waypoint save to succeed when idle with exterior");
        }
        if (model.getWaypoints().size() != 1) {
            throw new AssertionError("Expected one waypoint after save, got " + model.getWaypoints().size());
        }
        TardisWaypoint wp = model.getWaypoints().getFirst();
        if (!"GameTest Pad".equals(wp.name)) {
            throw new AssertionError("Unexpected waypoint name: " + wp.name);
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void selectWaypoint_RejectedForUnknownTardis(GameTestHelper context) {
        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean selected = ServerPayloadTypeRegistry.safelyHandleSelectWaypoint(
                new SelectWaypointC2SPayload(UUID.randomUUID(), UUID.randomUUID()),
                player
        );
        if (selected) {
            throw new AssertionError("Select waypoint must reject unknown tardisId");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void selectPlayer_RejectedWhileTraveling(GameTestHelper context) {
        TardisTravelService.clearActiveForTests();
        TardisGameTestSupport.configureSaveDirectory(context);
        TardisDataModel model = TardisDataLoader.create();
        model.setTravelPhase(TardisTravelPhase.DEMATERIALISING);
        model.setChanged();

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean selected = ServerPayloadTypeRegistry.safelyHandleSelectPlayer(
                new SelectPlayerC2SPayload(model.uuid, player.getUUID()),
                player
        );
        if (selected) {
            throw new AssertionError("Select player must be rejected while traveling");
        }
        if (model.getDestinationMode() == DestinationMode.PLAYER) {
            throw new AssertionError("Destination mode must not flip to PLAYER when rejected");
        }

        model.setTravelPhase(TardisTravelPhase.IDLE);
        TardisTravelService.clearActiveForTests();
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void portalStreamRequest_RejectedOnNullKind(GameTestHelper context) {
        TardisGameTestSupport.configureSaveDirectory(context);
        TardisDataModel model = TardisDataLoader.create();
        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);

        boolean accepted = ServerPayloadTypeRegistry.safelyHandlePortalStreamRequest(
                new RequestPortalStreamC2SPayload(null, model.uuid),
                player
        );
        if (accepted) {
            throw new AssertionError("Portal stream request with null kind must be rejected");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void portalStreamRequest_SubscribesWhenExteriorIndexed(GameTestHelper context) {
        TardisGameTestSupport.configureSaveDirectory(context);
        var exterior = TardisGameTestSupport.placeExteriorShell(context, new BlockPos(2, 2, 2));
        UUID tardisId = exterior.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        SotoExteriorIndex.register(tardisId, model);

        ServerPlayer player = TardisGameTestSupport.mockServerPlayer(context);
        boolean accepted = ServerPayloadTypeRegistry.safelyHandlePortalStreamRequest(
                new RequestPortalStreamC2SPayload(PortalStreamKind.SOTO, tardisId),
                player
        );
        if (!accepted) {
            throw new AssertionError("Expected SOTO portal stream subscribe to succeed with indexed exterior");
        }
        context.succeed();
    }
}
