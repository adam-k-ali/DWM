package com.adamkali.dwm.network;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;

public class DWMPacketIds {
    public static final Identifier OPEN_TARDIS_CHAMELEON_SCREEN_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "open_tardis_chameleon_screen");
    public static final Identifier UPDATE_TARDIS_CHAMELEON_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "update_tardis_chameleon");

    public static final Identifier OPEN_WAYPOINT_SCREEN_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "open_waypoint_screen");
    public static final Identifier OPEN_PLAYER_LOCATOR_SCREEN_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "open_player_locator_screen");
    public static final Identifier SAVE_WAYPOINT_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "save_waypoint");
    public static final Identifier DELETE_WAYPOINT_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "delete_waypoint");
    public static final Identifier RENAME_WAYPOINT_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "rename_waypoint");
    public static final Identifier SELECT_WAYPOINT_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "select_waypoint");
    public static final Identifier SELECT_PLAYER_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "select_player");

    public static final Identifier REQUEST_PORTAL_STREAM_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "request_portal_stream");
    public static final Identifier SYNC_PORTAL_META_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_portal_meta");
    public static final Identifier SYNC_PORTAL_CHUNK_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_portal_chunk");
    public static final Identifier UNLOAD_PORTAL_CHUNK_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "unload_portal_chunk");
    public static final Identifier SYNC_PORTAL_ENTITY_SPAWN_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_portal_entity_spawn");
    public static final Identifier SYNC_PORTAL_ENTITY_UPDATE_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_portal_entity_update");
    public static final Identifier SYNC_PORTAL_ENTITY_REMOVE_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_portal_entity_remove");

    public static final Identifier TRAVEL_AUDIO_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "travel_audio");
}
