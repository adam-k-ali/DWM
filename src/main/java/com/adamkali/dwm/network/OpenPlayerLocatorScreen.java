package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.logic.PlayerLocatorLogic;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenPlayerLocatorScreen(
        UUID tardisId,
        List<PlayerEntry> players
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenPlayerLocatorScreen> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.OPEN_PLAYER_LOCATOR_SCREEN_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerEntry> ENTRY_CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, PlayerEntry::uuid,
            ByteBufCodecs.STRING_UTF8, PlayerEntry::name,
            PlayerEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPlayerLocatorScreen> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, OpenPlayerLocatorScreen::tardisId,
            ENTRY_CODEC.apply(ByteBufCodecs.list()), OpenPlayerLocatorScreen::players,
            OpenPlayerLocatorScreen::new
    );

    public static OpenPlayerLocatorScreen of(UUID tardisId, List<PlayerLocatorLogic.PlayerEntry> players) {
        List<PlayerEntry> entries = new ArrayList<>(players == null ? 0 : players.size());
        if (players != null) {
            for (PlayerLocatorLogic.PlayerEntry player : players) {
                if (player == null || player.uuid() == null) {
                    continue;
                }
                entries.add(new PlayerEntry(player.uuid(), player.name() == null ? "" : player.name()));
            }
        }
        return new OpenPlayerLocatorScreen(tardisId, List.copyOf(entries));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public record PlayerEntry(UUID uuid, String name) {
    }
}
