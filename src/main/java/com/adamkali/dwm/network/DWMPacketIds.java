package com.adamkali.dwm.network;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;

public class DWMPacketIds {
    public static final Identifier OPEN_TARDIS_CHAMELEON_SCREEN_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "open_tardis_chameleon_screen");
    public static final Identifier UPDATE_TARDIS_CHAMELEON_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "update_tardis_chameleon");
    public static final Identifier REQUEST_BOTI_INTERIOR_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "request_boti_interior");
    public static final Identifier SYNC_BOTI_INTERIOR_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_boti_interior");
    public static final Identifier REQUEST_SOTO_EXTERIOR_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "request_soto_exterior");
    public static final Identifier SYNC_SOTO_EXTERIOR_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_soto_exterior");
    public static final Identifier REQUEST_SOTO_GHOST_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "request_soto_ghost");
    public static final Identifier SYNC_SOTO_EXTERIOR_CHUNK_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_soto_exterior_chunk");
    public static final Identifier UNLOAD_SOTO_EXTERIOR_CHUNK_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "unload_soto_exterior_chunk");
    public static final Identifier SYNC_SOTO_EXTERIOR_ENTITY_SPAWN_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_soto_exterior_entity_spawn");
    public static final Identifier SYNC_SOTO_EXTERIOR_ENTITY_UPDATE_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_soto_exterior_entity_update");
    public static final Identifier SYNC_SOTO_EXTERIOR_ENTITY_REMOVE_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sync_soto_exterior_entity_remove");
    public static final Identifier TRAVEL_AUDIO_PACKET_ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "travel_audio");
}
