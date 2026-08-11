package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** S2C pose/velocity update for a live portal ghost entity (footprint-relative). */
public record SyncPortalEntityUpdateS2CPayload(
        PortalStreamKind kind,
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
    public static final CustomPacketPayload.Type<SyncPortalEntityUpdateS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_ENTITY_UPDATE_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalEntityUpdateS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalEntityUpdateS2CPayload::encode, SyncPortalEntityUpdateS2CPayload::decode);

    private static void encode(SyncPortalEntityUpdateS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.kind.toWire());
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

    private static SyncPortalEntityUpdateS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new SyncPortalEntityUpdateS2CPayload(
                PortalStreamKind.fromWire(buf.readByte()),
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

    public static SyncPortalEntityUpdateS2CPayload fromEntity(
            PortalStreamKind kind,
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
        return new SyncPortalEntityUpdateS2CPayload(
                kind,
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
