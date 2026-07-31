package com.adamkali.dwm.network;

import com.adamkali.dwm.DWMReference;
import net.minecraft.util.Identifier;

public class DWMPacketIds {
    public static final Identifier OPEN_TARDIS_CHAMELEON_SCREEN_ID = Identifier.of(DWMReference.MOD_ID, "open_tardis_chameleon_screen");
    public static final Identifier UPDATE_TARDIS_CHAMELEON_PACKET_ID = Identifier.of(DWMReference.MOD_ID, "update_tardis_chameleon");
    public static final Identifier REQUEST_BOTI_INTERIOR_PACKET_ID = Identifier.of(DWMReference.MOD_ID, "request_boti_interior");
    public static final Identifier SYNC_BOTI_INTERIOR_PACKET_ID = Identifier.of(DWMReference.MOD_ID, "sync_boti_interior");
}
