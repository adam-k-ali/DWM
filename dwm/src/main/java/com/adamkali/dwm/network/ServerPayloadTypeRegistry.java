package com.adamkali.dwm.network;

import com.adamkali.dwm.advancement.DWMCriteria;
import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.SonicFieldMode;
import com.adamkali.dwm.item.SonicStateLogic;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.logic.CircuitFittedLogic;
import com.adamkali.dwm.tardis.logic.ConsolePilotLogic;
import com.adamkali.dwm.tardis.logic.PlayerLocatorLogic;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSyncService;
import com.adamkali.dwm.tardis.soto.SotoExteriorIndex;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class ServerPayloadTypeRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(OpenTardisChameleonScreen.ID, OpenTardisChameleonScreen.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenWaypointScreen.ID, OpenWaypointScreen.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenPlayerLocatorScreen.ID, OpenPlayerLocatorScreen.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalMetaS2CPayload.ID, SyncPortalMetaS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalChunkS2CPayload.ID, SyncPortalChunkS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UnloadPortalChunkS2CPayload.ID, UnloadPortalChunkS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalEntitySpawnS2CPayload.ID, SyncPortalEntitySpawnS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalEntityUpdateS2CPayload.ID, SyncPortalEntityUpdateS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalEntityRemoveS2CPayload.ID, SyncPortalEntityRemoveS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPortalPerfS2CPayload.ID, SyncPortalPerfS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TravelAudioS2CPayload.ID, TravelAudioS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SonicPingRevealS2CPayload.ID, SonicPingRevealS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SonicScanS2CPayload.ID, SonicScanS2CPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(UpdateTardisChameleonC2SPayload.ID, UpdateTardisChameleonC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SaveWaypointC2SPayload.ID, SaveWaypointC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DeleteWaypointC2SPayload.ID, DeleteWaypointC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RenameWaypointC2SPayload.ID, RenameWaypointC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectWaypointC2SPayload.ID, SelectWaypointC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectPlayerC2SPayload.ID, SelectPlayerC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestPortalStreamC2SPayload.ID, RequestPortalStreamC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectSonicFieldModeC2SPayload.ID, SelectSonicFieldModeC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateTardisChameleonC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleChameleonUpdate(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(SaveWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSaveWaypoint(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(DeleteWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleDeleteWaypoint(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(RenameWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleRenameWaypoint(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(SelectWaypointC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSelectWaypoint(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(SelectPlayerC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSelectPlayer(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(RequestPortalStreamC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandlePortalStreamRequest(payload, context.player()));
        });
        ServerPlayNetworking.registerGlobalReceiver(SelectSonicFieldModeC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> safelyHandleSelectSonicFieldMode(payload, context.player()));
        });
    }

    /**
     * Applies an absolute field-mode selection on the sonic in either hand.
     * Returns true when the selection changed. Unit tests may pass a null player (no overlay/criterion).
     */
    public static boolean safelyHandleSelectSonicFieldMode(
            SelectSonicFieldModeC2SPayload payload,
            @Nullable ServerPlayer player
    ) {
        if (player == null || payload == null || payload.mode() == null) {
            return false;
        }
        ItemStack sonic = findHeldSonic(player);
        if (sonic.isEmpty()) {
            return false;
        }
        SonicFieldMode mode = payload.mode();
        if (!SonicStateLogic.isUnlocked(sonic, mode)) {
            player.sendOverlayMessage(Component.translatable(
                    SonicStateLogic.SETTING_NOT_INSTALLED_DETAIL_KEY,
                    Component.translatable(mode.translationKey())
            ));
            return false;
        }
        int unlockedBefore = SonicStateLogic.effective(sonic).unlockedCount();
        boolean changed = SonicStateLogic.select(sonic, mode);
        if (!changed) {
            return false;
        }
        if (unlockedBefore >= 2) {
            DWMCriteria.SONIC_CYCLE_SETTING.trigger(player);
        }
        player.sendOverlayMessage(Component.translatable(
                SonicStateLogic.SETTING_KEY,
                Component.translatable(mode.translationKey())
        ));
        return true;
    }

    private static ItemStack findHeldSonic(ServerPlayer player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.is(DWMItemTags.SONIC_SCREWDRIVERS)) {
            return main;
        }
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (off.is(DWMItemTags.SONIC_SCREWDRIVERS)) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    static boolean safelyHandleChameleonUpdate(UpdateTardisChameleonC2SPayload payload, ServerPlayer player) {
        return safelyHandleChameleonUpdate(payload, player == null ? null : player.getUUID(), player);
    }

    /**
     * Applies a chameleon variant update when the player owns the TARDIS.
     * Unit tests may pass {@code player} as null (no overlay).
     */
    static boolean safelyHandleChameleonUpdate(
            UpdateTardisChameleonC2SPayload payload,
            @Nullable UUID playerUuid,
            @Nullable ServerPlayer player
    ) {
        try {
            TardisDataModel tardis = TardisDataLoader.get(payload.tardisId());
            if (tardis == null) {
                String name = player != null ? player.getName().getString() : String.valueOf(playerUuid);
                LOGGER.warn("Rejected chameleon update for unknown tardisId {} from {}", payload.tardisId(), name);
                return false;
            }
            if (!ConsolePilotLogic.canPilot(tardis, playerUuid)) {
                if (player != null) {
                    player.sendOverlayMessage(Component.translatable(ConsolePilotLogic.NOT_OWNER_KEY));
                }
                return false;
            }
            if (CircuitFittedLogic.isBroken(tardis, TardisCircuit.CHAMELEON)) {
                return false;
            }

            TardisChameleonVariant variant = TardisChameleonVariant.fromId(payload.variantId());
            TardisLogic.setVariant(payload.tardisId(), variant);
            return true;
        } catch (IllegalArgumentException e) {
            String name = player != null ? player.getName().getString() : String.valueOf(playerUuid);
            LOGGER.warn("Rejected chameleon update with invalid variant {} from {}", payload.variantId(), name);
            return false;
        }
    }

    static boolean safelyHandleSaveWaypoint(SaveWaypointC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player.getUUID(), player)) {
            return false;
        }
        if (!requireCircuit(payload.tardisId(), TardisCircuit.WAYPOINTS)) {
            return false;
        }
        Optional<TardisWaypoint> saved = TardisLogic.saveWaypoint(payload.tardisId(), payload.name());
        TardisDataModel model = TardisDataLoader.get(payload.tardisId());
        if (saved.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_save_failed"));
            // Refresh so the client leaves create mode even on failure.
            if (model != null) {
                ServerPlayNetworking.send(player, OpenWaypointScreen.of(payload.tardisId(), model));
            }
            return false;
        }
        player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_saved", saved.get().name));
        if (model != null) {
            ServerPlayNetworking.send(player, OpenWaypointScreen.of(payload.tardisId(), model));
        }
        return true;
    }

    static boolean safelyHandleDeleteWaypoint(DeleteWaypointC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player.getUUID(), player)) {
            return false;
        }
        if (!requireCircuit(payload.tardisId(), TardisCircuit.WAYPOINTS)) {
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
        if (!validateConsoleAction(payload.tardisId(), player.getUUID(), player)) {
            return false;
        }
        if (!requireCircuit(payload.tardisId(), TardisCircuit.WAYPOINTS)) {
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
        return safelyHandleSelectWaypoint(payload, player.getUUID(), player);
    }

    /** Unit-test entry: {@code player} may be null (no overlays). */
    static boolean safelyHandleSelectWaypoint(
            SelectWaypointC2SPayload payload,
            UUID playerUuid,
            @Nullable ServerPlayer player
    ) {
        if (!validateConsoleAction(payload.tardisId(), playerUuid, player)) {
            return false;
        }
        if (!requireCircuit(payload.tardisId(), TardisCircuit.WAYPOINTS)) {
            return false;
        }
        boolean selected = TardisLogic.selectWaypoint(payload.tardisId(), payload.waypointId());
        if (!selected) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_select_failed"));
            }
            return false;
        }
        if (player != null) {
            if (payload.waypointId() == null) {
                player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_cleared"));
            } else {
                player.sendOverlayMessage(Component.translatable("dwm.console.waypoint_selected"));
            }
        }
        return true;
    }

    static boolean safelyHandleSelectPlayer(SelectPlayerC2SPayload payload, ServerPlayer player) {
        if (!validateConsoleAction(payload.tardisId(), player.getUUID(), player)) {
            return false;
        }
        if (!requireCircuit(payload.tardisId(), TardisCircuit.PLAYER_LOCATOR)) {
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

    private static boolean requireCircuit(UUID tardisId, TardisCircuit circuit) {
        TardisDataModel model = TardisDataLoader.get(tardisId);
        return CircuitFittedLogic.isFitted(model, circuit);
    }

    private static boolean validateConsoleAction(
            UUID tardisId,
            @Nullable UUID playerUuid,
            @Nullable ServerPlayer player
    ) {
        if (tardisId == null || playerUuid == null) {
            return false;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            String name = player != null ? player.getName().getString() : String.valueOf(playerUuid);
            LOGGER.warn("Rejected console payload for unknown tardisId {} from {}", tardisId, name);
            return false;
        }
        if (!ConsolePilotLogic.canPilot(model, playerUuid)) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable(ConsolePilotLogic.NOT_OWNER_KEY));
            }
            return false;
        }
        if (TardisTravelService.isTraveling(tardisId)) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("dwm.console.travel_in_flight"));
            }
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
