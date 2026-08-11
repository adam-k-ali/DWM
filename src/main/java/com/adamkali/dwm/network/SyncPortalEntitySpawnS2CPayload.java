package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** S2C spawn a live portal ghost entity. Pose is footprint-relative. */
public record SyncPortalEntitySpawnS2CPayload(
        PortalStreamKind kind,
        UUID tardisId,
        UUID entityUuid,
        Identifier typeId,
        float relX,
        float relY,
        float relZ,
        float yaw,
        float pitch,
        float headYaw,
        float bodyYaw,
        double velX,
        double velY,
        double velZ,
        CompoundTag nbt
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPortalEntitySpawnS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_ENTITY_SPAWN_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalEntitySpawnS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalEntitySpawnS2CPayload::encode, SyncPortalEntitySpawnS2CPayload::decode);

    private static void encode(SyncPortalEntitySpawnS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.kind.toWire());
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.entityUuid);
        Identifier.STREAM_CODEC.encode(buf, payload.typeId);
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
        ByteBufCodecs.COMPOUND_TAG.encode(buf, payload.nbt == null ? new CompoundTag() : payload.nbt);
    }

    private static SyncPortalEntitySpawnS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new SyncPortalEntitySpawnS2CPayload(
                PortalStreamKind.fromWire(buf.readByte()),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                Identifier.STREAM_CODEC.decode(buf),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                ByteBufCodecs.COMPOUND_TAG.decode(buf)
        );
    }

    public static SyncPortalEntitySpawnS2CPayload fromEntity(
            PortalStreamKind kind,
            UUID tardisId,
            Entity entity,
            BlockPos footprintOrigin
    ) {
        CompoundTag nbt = BotiInteriorSampler.captureEntityNbt(entity);
        if (nbt == null) {
            nbt = new CompoundTag();
        }
        Identifier typeId = Identifier.parse(nbt.getStringOr("id", "minecraft:pig"));
        if (entity instanceof Player) {
            typeId = Identifier.fromNamespaceAndPath("minecraft", "player");
        }
        float headYaw = entity.getYRot();
        float bodyYaw = entity.getYRot();
        if (entity instanceof LivingEntity living) {
            headYaw = living.getYHeadRot();
            bodyYaw = living.yBodyRot;
        }
        return new SyncPortalEntitySpawnS2CPayload(
                kind,
                tardisId,
                entity.getUUID(),
                typeId,
                (float) (entity.getX() - footprintOrigin.getX()),
                (float) (entity.getY() - footprintOrigin.getY()),
                (float) (entity.getZ() - footprintOrigin.getZ()),
                entity.getYRot(),
                entity.getXRot(),
                headYaw,
                bodyYaw,
                entity.getDeltaMovement().x,
                entity.getDeltaMovement().y,
                entity.getDeltaMovement().z,
                nbt
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
