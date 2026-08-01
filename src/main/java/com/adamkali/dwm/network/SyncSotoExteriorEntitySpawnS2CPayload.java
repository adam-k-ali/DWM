package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * S2C spawn a live ghost exterior entity. Pose is footprint-relative.
 */
public record SyncSotoExteriorEntitySpawnS2CPayload(
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
        NbtCompound nbt
) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorEntitySpawnS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_ENTITY_SPAWN_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorEntitySpawnS2CPayload> CODEC =
            PacketCodec.of(SyncSotoExteriorEntitySpawnS2CPayload::encode, SyncSotoExteriorEntitySpawnS2CPayload::decode);

    private static void encode(SyncSotoExteriorEntitySpawnS2CPayload payload, RegistryByteBuf buf) {
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.entityUuid);
        Identifier.PACKET_CODEC.encode(buf, payload.typeId);
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
        PacketCodecs.NBT_COMPOUND.encode(buf, payload.nbt == null ? new NbtCompound() : payload.nbt);
    }

    private static SyncSotoExteriorEntitySpawnS2CPayload decode(RegistryByteBuf buf) {
        return new SyncSotoExteriorEntitySpawnS2CPayload(
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf),
                Identifier.PACKET_CODEC.decode(buf),
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
                PacketCodecs.NBT_COMPOUND.decode(buf)
        );
    }

    public static SyncSotoExteriorEntitySpawnS2CPayload fromEntity(
            UUID tardisId,
            Entity entity,
            BlockPos footprintOrigin
    ) {
        NbtCompound nbt = BotiInteriorSampler.captureEntityNbt(entity);
        if (nbt == null) {
            nbt = new NbtCompound();
        }
        Identifier typeId = Identifier.of(nbt.getString("id"));
        if (entity instanceof PlayerEntity) {
            typeId = Identifier.of("minecraft", "player");
        }
        float headYaw = entity.getYaw();
        float bodyYaw = entity.getYaw();
        if (entity instanceof LivingEntity living) {
            headYaw = living.getHeadYaw();
            bodyYaw = living.bodyYaw;
        }
        return new SyncSotoExteriorEntitySpawnS2CPayload(
                tardisId,
                entity.getUuid(),
                typeId,
                (float) (entity.getX() - footprintOrigin.getX()),
                (float) (entity.getY() - footprintOrigin.getY()),
                (float) (entity.getZ() - footprintOrigin.getZ()),
                entity.getYaw(),
                entity.getPitch(),
                headYaw,
                bodyYaw,
                entity.getVelocity().x,
                entity.getVelocity().y,
                entity.getVelocity().z,
                nbt
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
