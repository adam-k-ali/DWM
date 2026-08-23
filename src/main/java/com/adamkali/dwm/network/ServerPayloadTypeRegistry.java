package com.adamkali.dwm.network;

import com.adamkali.dwm.platform.DwmServices;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.logic.PlayerLocatorLogic;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class ServerPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        var platform = DwmServices.get();

        platform.registerClientboundPayload(OpenTardisChameleonScreen.ID, OpenTardisChameleonScreen.CODEC);
        platform.registerClientboundPayload(OpenWaypointScreen.ID, OpenWaypointScreen.CODEC);
        platform.registerClientboundPayload(OpenPlayerLocatorScreen.ID, OpenPlayerLocatorScreen.CODEC);
        platform.registerClientboundPayload(SyncPortalMetaS2CPayload.ID, SyncPortalMetaS2CPayload.CODEC);
        platform.registerClientboundPayload(SyncPortalChunkS2CPayload.ID, SyncPortalChunkS2CPayload.CODEC);
        platform.registerClientboundPayload(UnloadPortalChunkS2CPayload.ID, UnloadPortalChunkS2CPayload.CODEC);
        platform.registerClientboundPayload(SyncPortalEntitySpawnS2CPayload.ID, SyncPortalEntitySpawnS2CPayload.CODEC);
        platform.registerClientboundPayload(SyncPortalEntityUpdateS2CPayload.ID, SyncPortalEntityUpdateS2CPayload.CODEC);
        platform.registerClientboundPayload(SyncPortalEntityRemoveS2CPayload.ID, SyncPortalEntityRemoveS2CPayload.CODEC);
        platform.registerClientboundPayload(SyncPortalPerfS2CPayload.ID, SyncPortalPerfS2CPayload.CODEC);
        platform.registerClientboundPayload(TravelAudioS2CPayload.ID, TravelAudioS2CPayload.CODEC);

        platform.registerServerboundPayload(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);
        platform.registerServerboundPayload(SaveWaypointC2SPayload.ID, SaveWaypointC2SPayload.CODEC);
        platform.registerServerboundPayload(DeleteWaypointC2SPayload.ID, DeleteWaypointC2SPayload.CODEC);
        platform.registerServerboundPayload(RenameWaypointC2SPayload.ID, RenameWaypointC2SPayload.CODEC);
        platform.registerServerboundPayload(SelectWaypointC2SPayload.ID, SelectWaypointC2SPayload.CODEC);
        platform.registerServerboundPayload(SelectPlayerC2SPayload.ID, SelectPlayerC2SPayload.CODEC);
        platform.registerServerboundPayload(RequestPortalStreamC2SPayload.ID, RequestPortalStreamC2SPayload.CODEC);

        platform.registerServerboundHandler(UpdateTardisChameleonC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleChameleonUpdate(
                    payload,
                    context.player().getName().getString()
            ));
        });
        platform.registerServerboundHandler(SaveWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSaveWaypoint(payload, context.player()));
        });
        platform.registerServerboundHandler(DeleteWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleDeleteWaypoint(payload, context.player()));
        });
        platform.registerServerboundHandler(RenameWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleRenameWaypoint(payload, context.player()));
        });
        platform.registerServerboundHandler(SelectWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSelectWaypoint(payload, context.player()));
        });
        platform.registerServerboundHandler(SelectPlayerC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSelectPlayer(payload, context.player()));
        });
        platform.registerServerboundHandler(RequestPortalStreamC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandlePortalStreamRequest(payload, context.player()));
        });
    }

    static boolean safelyHandleChameleonUpdate(UpdateTardisChameleonC2SPayload payload, String playerName) {
        try {
            TardisDataModel tardis = TardisDataLoader.get(payload.tardisId());
            if (tardis == null) {
                LOGGER.warn("Rejected chameleon update for unknown tardisId {} from {}", payload.tardisId(), playerName);
                return false;
            }

            TardisChameleonVariant variant = TardisChameleonVariant.fromId(payload.variantId());
            TardisLogic.setVariant(payload.tardisId(), variant);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Rejected chameleon update with invalid variant {} from {}", payload.variantId(), playerName);
            return false;
        }
    }

    static boolean safelyHandleSaveWaypoint(SaveWaypointC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player)) {
            return false;
        }
        Optional<TardisWaypoint> saved = TardisLogic.saveWaypoint(payload.tardisId(), payload.name());
        TardisDataModel model = TardisDataLoader.get(payload.tardisId());
        if (saved.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_save_failed"));
            // Refresh so the client leaves create mode even on failure.
            if (model != null) {
                DwmServices.get().sendToPlayer(player, OpenWaypointScreen.of(payload.tardisId(), model));
            }
            return false;
        }
        player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_saved", saved.get().name));
        if (model != null) {
            DwmServices.get().sendToPlayer(player, OpenWaypointScreen.of(payload.tardisId(), model));
        }
        return true;
    }

    static boolean safelyHandleDeleteWaypoint(DeleteWaypointC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player)) {
            return false;
        }
        boolean deleted = TardisLogic.deleteWaypoint(payload.tardisId(), payload.waypointId());
        if (!deleted) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_delete_failed"));
            return false;
        }
        player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_deleted"));
        return true;
    }

    static boolean safelyHandleRenameWaypoint(RenameWaypointC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player)) {
            return false;
        }
        boolean renamed = TardisLogic.renameWaypoint(payload.tardisId(), payload.waypointId(), payload.name());
        if (!renamed) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_rename_failed"));
            return false;
        }
        player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_renamed", payload.name().trim()));
        return true;
    }

    static boolean safelyHandleSelectWaypoint(SelectWaypointC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player)) {
            return false;
        }
        boolean selected = TardisLogic.selectWaypoint(payload.tardisId(), payload.waypointId());
        if (!selected) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_select_failed"));
            return false;
        }
        if (payload.waypointId() == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_cleared"));
        } else {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_selected"));
        }
        return true;
    }

    static boolean safelyHandleSelectPlayer(SelectPlayerC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player)) {
            return false;
        }
        if (payload.playerUuid() != null
                && !PlayerLocatorLogic.isOnline(player.level().getServer(), payload.playerUuid())) {
            player.sendOverlayMessage(Component.translatable("dwm.console.player_locator_offline"));
            return false;
        }
        boolean selected = TardisLogic.selectPlayer(
                payload.tardisId(),
                payload.playerUuid(),
                player.level().getServer()
        );
        if (!selected) {
            player.sendOverlayMessage(Component.translatable("dwm.console.player_locator_select_failed"));
            return false;
        }
        if (payload.playerUuid() == null) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_cleared"));
        } else {
            player.sendOverlayMessage(Component.translatable("dwm.console.player_locator_selected"));
        }
        return true;
    }

    private static boolean validateConsoleAction(UUID tardisId, ServerPlayer player) {
        if (tardisId == null || player == null) {
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            LOGGER.warn("Rejected console payload for unknown tardisId {} from {}", tardisId, player.getName().getString());
            return false;
        }
        if (TardisTravelService.isTraveling(tardisId)) {
            player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            return false;
        }
        return true;
    }

    static boolean safelyHandlePortalStreamRequest(RequestPortalStreamC2SPayload payload, ServerPlayer player) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null || player == null) {
            LOGGER.debug("Rejected portal stream request: null payload, kind, tardisId, or player");
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(payload.tardisId());
        if (model != null) {
            if (payload.kind() == PortalStreamKind.SOTO) {
                SotoExteriorIndex.register(payload.tardisId(), model);
            } else {
                BotiPlotIndex.register(payload.tardisId());
            }
        }
        return PortalStreamSyncService.subscribe(player, payload.kind(), payload.tardisId());
    }
}
