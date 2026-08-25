package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.logic.PlayerLocatorLogic;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

public record OpenPlayerLocatorScreen(
        UUID tardisId,
        List<PlayerEntry> players,
        @Nullable UUID selectedPlayerUuid
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenPlayerLocatorScreen> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.OPEN_PLAYER_LOCATOR_SCREEN_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerEntry> ENTRY_CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, PlayerEntry::uuid,
            ByteBufCodecs.STRING_UTF8, PlayerEntry::name,
            ByteBufCodecs.STRING_UTF8, PlayerEntry::dimension,
            ByteBufCodecs.VAR_INT, PlayerEntry::x,
            ByteBufCodecs.VAR_INT, PlayerEntry::y,
            ByteBufCodecs.VAR_INT, PlayerEntry::z,
            PlayerEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPlayerLocatorScreen> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, OpenPlayerLocatorScreen::tardisId,
            ENTRY_CODEC.apply(ByteBufCodecs.list()), OpenPlayerLocatorScreen::players,
            DWMPacketCodecs.NULLABLE_UUID_PACKET_CODEC, OpenPlayerLocatorScreen::selectedPlayerUuid,
            OpenPlayerLocatorScreen::new
    );

    public static OpenPlayerLocatorScreen of(
            UUID tardisId,
            List<PlayerLocatorLogic.PlayerEntry> players,
            @Nullable UUID selectedPlayerUuid
    ) {
        List<PlayerEntry> entries = new ArrayList<>(players == null ? 0 : players.size());
        if (players != null) {
            for (PlayerLocatorLogic.PlayerEntry player : players) {
                if (player == null || player.uuid() == null) {
                    continue;
                }
                entries.add(new PlayerEntry(
                        player.uuid(),
                        player.name() == null ? "" : player.name(),
                        player.dimension() == null ? "" : player.dimension(),
                        player.x(),
                        player.y(),
                        player.z()
                ));
            }
        }
        return new OpenPlayerLocatorScreen(tardisId, List.copyOf(entries), selectedPlayerUuid);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public record PlayerEntry(
            UUID uuid,
            String name,
            String dimension,
            int x,
            int y,
            int z
    ) {
    }
}
