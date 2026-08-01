package com.adamkali.dwm.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/** S2C pose/velocity update for a live ghost exterior entity (footprint-relative). */
public record SyncSotoExteriorEntityUpdateS2CPayload(
        UUID tardisId,
        UUID entityUuid,
        float relX,
        float relY,
        float relZ,
        float yaw,
        float pitch,
        float headYaw,
        float bodyYaw,
        double velX,
        double velY,
        double velZ
) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorEntityUpdateS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_ENTITY_UPDATE_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorEntityUpdateS2CPayload> CODEC =
            PacketCodec.of(SyncSotoExteriorEntityUpdateS2CPayload::encode, SyncSotoExteriorEntityUpdateS2CPayload::decode);

    private static void encode(SyncSotoExteriorEntityUpdateS2CPayload payload, RegistryByteBuf buf) {
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.entityUuid);
        buf.writeFloat(payload.relX);
        buf.writeFloat(payload.relY);
        buf.writeFloat(payload.relZ);
        buf.writeFloat(payload.yaw);
        buf.writeFloat(payload.pitch);
        buf.writeFloat(payload.headYaw);
        buf.writeFloat(payload.bodyYaw);
        buf.writeDouble(payload.velX);
        buf.writeDouble(payload.velY);
        buf.writeDouble(payload.velZ);
    }

    private static SyncSotoExteriorEntityUpdateS2CPayload decode(RegistryByteBuf buf) {
        return new SyncSotoExteriorEntityUpdateS2CPayload(
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static SyncSotoExteriorEntityUpdateS2CPayload fromEntity(
            UUID tardisId,
            Entity entity,
            BlockPos footprintOrigin
    ) {
        float headYaw = entity.getYaw();
        float bodyYaw = entity.getYaw();
        if (entity instanceof LivingEntity living) {
            headYaw = living.getHeadYaw();
            bodyYaw = living.bodyYaw;
        }
        return new SyncSotoExteriorEntityUpdateS2CPayload(
                tardisId,
                entity.getUuid(),
                (float) (entity.getX() - footprintOrigin.getX()),
                (float) (entity.getY() - footprintOrigin.getY()),
                (float) (entity.getZ() - footprintOrigin.getZ()),
                entity.getYaw(),
                entity.getPitch(),
                headYaw,
                bodyYaw,
                entity.getVelocity().x,
                entity.getVelocity().y,
                entity.getVelocity().z
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
