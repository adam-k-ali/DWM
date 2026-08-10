package com.adamkali.dwm.network;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSotoExteriorEntityUpdateS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_ENTITY_UPDATE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSotoExteriorEntityUpdateS2CPayload> CODEC =
            StreamCodec.ofMember(SyncSotoExteriorEntityUpdateS2CPayload::encode, SyncSotoExteriorEntityUpdateS2CPayload::decode);

    private static void encode(SyncSotoExteriorEntityUpdateS2CPayload payload, RegistryFriendlyByteBuf buf) {
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

    private static SyncSotoExteriorEntityUpdateS2CPayload decode(RegistryFriendlyByteBuf buf) {
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
        float headYaw = entity.getYRot();
        float bodyYaw = entity.getYRot();
        if (entity instanceof LivingEntity living) {
            headYaw = living.getYHeadRot();
            bodyYaw = living.yBodyRot;
        }
        return new SyncSotoExteriorEntityUpdateS2CPayload(
                tardisId,
                entity.getUUID(),
                (float) (entity.getX() - footprintOrigin.getX()),
                (float) (entity.getY() - footprintOrigin.getY()),
                (float) (entity.getZ() - footprintOrigin.getZ()),
                entity.getYRot(),
                entity.getXRot(),
                headYaw,
                bodyYaw,
                entity.getDeltaMovement().x,
                entity.getDeltaMovement().y,
                entity.getDeltaMovement().z
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
