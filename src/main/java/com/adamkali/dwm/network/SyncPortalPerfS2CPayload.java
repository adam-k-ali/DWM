package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.portal.PortalStreamPerfStats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * ~1 Hz S2C snapshot of server MSPT + portal stream sync counters for the portal perf debugger.
 */
public record SyncPortalPerfS2CPayload(
        float msptMs,
        float flushMetaMs,
        float flushSotoMs,
        float flushBotiMs,
        float syncEntitiesMs,
        float syncFlushMs,
        int entitySpawns,
        int entityUpdates,
        int entityRemoves,
        int chunkPackets,
        int metaPackets,
        int chunkSamples,
        int entitiesScanned,
        int fullResyncs,
        int viewers,
        int activeStreams,
        int serverTick
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPortalPerfS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_PERF_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalPerfS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalPerfS2CPayload::encode, SyncPortalPerfS2CPayload::decode);

    public static SyncPortalPerfS2CPayload fromSnapshot(PortalStreamPerfStats.Snapshot snap) {
        if (snap == null || !snap.isPresent()) {
            return new SyncPortalPerfS2CPayload(
                    Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            );
        }
        return new SyncPortalPerfS2CPayload(
                (float) snap.msptMs(),
                (float) snap.flushMetaMs(),
                (float) snap.flushSotoMs(),
                (float) snap.flushBotiMs(),
                (float) snap.syncEntitiesMs(),
                (float) snap.syncFlushMs(),
                snap.entitySpawns(),
                snap.entityUpdates(),
                snap.entityRemoves(),
                snap.chunkPackets(),
                snap.metaPackets(),
                snap.chunkSamples(),
                snap.entitiesScanned(),
                snap.fullResyncs(),
                snap.viewers(),
                snap.activeStreams(),
                snap.serverTick()
        );
    }

    private static void encode(SyncPortalPerfS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeFloat(payload.msptMs);
        buf.writeFloat(payload.flushMetaMs);
        buf.writeFloat(payload.flushSotoMs);
        buf.writeFloat(payload.flushBotiMs);
        buf.writeFloat(payload.syncEntitiesMs);
        buf.writeFloat(payload.syncFlushMs);
        ByteBufCodecs.VAR_INT.encode(buf, payload.entitySpawns);
        ByteBufCodecs.VAR_INT.encode(buf, payload.entityUpdates);
        ByteBufCodecs.VAR_INT.encode(buf, payload.entityRemoves);
        ByteBufCodecs.VAR_INT.encode(buf, payload.chunkPackets);
        ByteBufCodecs.VAR_INT.encode(buf, payload.metaPackets);
        ByteBufCodecs.VAR_INT.encode(buf, payload.chunkSamples);
        ByteBufCodecs.VAR_INT.encode(buf, payload.entitiesScanned);
        ByteBufCodecs.VAR_INT.encode(buf, payload.fullResyncs);
        ByteBufCodecs.VAR_INT.encode(buf, payload.viewers);
        ByteBufCodecs.VAR_INT.encode(buf, payload.activeStreams);
        ByteBufCodecs.VAR_INT.encode(buf, payload.serverTick);
    }

    private static SyncPortalPerfS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new SyncPortalPerfS2CPayload(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
