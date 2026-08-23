package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisWaypoint;
import com.adamkali.dwm.tardis.logic.WaypointLogic;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

public record OpenWaypointScreen(
        UUID tardisId,
        List<WaypointEntry> waypoints,
        boolean canSave,
        @Nullable UUID destinationWaypointId,
        @Nullable UUID locationWaypointId,
        @Nullable ExteriorLocation exteriorLocation
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenWaypointScreen> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.OPEN_WAYPOINT_SCREEN_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, WaypointEntry> ENTRY_CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, WaypointEntry::id,
            ByteBufCodecs.STRING_UTF8, WaypointEntry::name,
            ByteBufCodecs.STRING_UTF8, WaypointEntry::dimension,
            ByteBufCodecs.VAR_INT, WaypointEntry::x,
            ByteBufCodecs.VAR_INT, WaypointEntry::y,
            ByteBufCodecs.VAR_INT, WaypointEntry::z,
            ByteBufCodecs.VAR_INT, WaypointEntry::rotation,
            WaypointEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExteriorLocation> EXTERIOR_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ExteriorLocation::dimension,
            ByteBufCodecs.VAR_INT, ExteriorLocation::x,
            ByteBufCodecs.VAR_INT, ExteriorLocation::y,
            ByteBufCodecs.VAR_INT, ExteriorLocation::z,
            ExteriorLocation::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, @Nullable ExteriorLocation> NULLABLE_EXTERIOR_CODEC =
            StreamCodec.of(
                    (buf, location) -> {
                        buf.writeBoolean(location != null);
                        if (location != null) {
                            EXTERIOR_CODEC.encode(buf, location);
                        }
                    },
                    buf -> buf.readBoolean() ? EXTERIOR_CODEC.decode(buf) : null
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWaypointScreen> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, OpenWaypointScreen::tardisId,
            ENTRY_CODEC.apply(ByteBufCodecs.list()), OpenWaypointScreen::waypoints,
            ByteBufCodecs.BOOL, OpenWaypointScreen::canSave,
            DWMPacketCodecs.NULLABLE_UUID_PACKET_CODEC, OpenWaypointScreen::destinationWaypointId,
            DWMPacketCodecs.NULLABLE_UUID_PACKET_CODEC, OpenWaypointScreen::locationWaypointId,
            NULLABLE_EXTERIOR_CODEC, OpenWaypointScreen::exteriorLocation,
            OpenWaypointScreen::new
    );

    public static OpenWaypointScreen of(UUID tardisId, List<TardisWaypoint> waypoints, boolean canSave) {
        return of(tardisId, waypoints, canSave, null, null, null);
    }

    public static OpenWaypointScreen of(
            UUID tardisId,
            List<TardisWaypoint> waypoints,
            boolean canSave,
            @Nullable UUID destinationWaypointId,
            @Nullable UUID locationWaypointId
    ) {
        return of(tardisId, waypoints, canSave, destinationWaypointId, locationWaypointId, null);
    }

    public static OpenWaypointScreen of(
            UUID tardisId,
            List<TardisWaypoint> waypoints,
            boolean canSave,
            @Nullable UUID destinationWaypointId,
            @Nullable UUID locationWaypointId,
            @Nullable ExteriorLocation exteriorLocation
    ) {
        List<WaypointEntry> entries = new ArrayList<>(waypoints.size());
        for (TardisWaypoint waypoint : waypoints) {
            if (waypoint == null || waypoint.id == null) {
                continue;
            }
            entries.add(new WaypointEntry(
                    waypoint.id,
                    waypoint.name == null ? "" : waypoint.name,
                    waypoint.dimension == null ? "" : waypoint.dimension,
                    waypoint.x,
                    waypoint.y,
                    waypoint.z,
                    waypoint.rotation
            ));
        }
        return new OpenWaypointScreen(
                tardisId,
                List.copyOf(entries),
                canSave,
                destinationWaypointId,
                locationWaypointId,
                exteriorLocation
        );
    }

    public static OpenWaypointScreen of(UUID tardisId, @Nullable TardisDataModel model) {
        List<TardisWaypoint> waypoints = model == null ? List.of() : List.copyOf(model.getWaypoints());
        boolean canSave = model != null
                && model.hasExteriorLocation
                && model.getWaypoints().size() < WaypointLogic.MAX_WAYPOINTS;
        UUID destination = model == null ? null : model.selectedWaypointId;
        UUID location = WaypointLogic.findAtExterior(model).map(w -> w.id).orElse(null);
        return of(tardisId, waypoints, canSave, destination, location, exteriorLocationOf(model));
    }

    public static @Nullable ExteriorLocation exteriorLocationOf(@Nullable TardisDataModel model) {
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null
                || model.exteriorDimension.isBlank()) {
            return null;
        }
        return new ExteriorLocation(
                model.exteriorDimension,
                model.exteriorX,
                model.exteriorY,
                model.exteriorZ
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public record WaypointEntry(
            UUID id,
            String name,
            String dimension,
            int x,
            int y,
            int z,
            int rotation
    ) {
    }

    public record ExteriorLocation(
            String dimension,
            int x,
            int y,
            int z
    ) {
    }
}
