package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
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
        CompoundTag nbt
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSotoExteriorEntitySpawnS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_ENTITY_SPAWN_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSotoExteriorEntitySpawnS2CPayload> CODEC =
            StreamCodec.ofMember(SyncSotoExteriorEntitySpawnS2CPayload::encode, SyncSotoExteriorEntitySpawnS2CPayload::decode);

    private static void encode(SyncSotoExteriorEntitySpawnS2CPayload payload, RegistryFriendlyByteBuf buf) {
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

    private static SyncSotoExteriorEntitySpawnS2CPayload decode(RegistryFriendlyByteBuf buf) {
        return new SyncSotoExteriorEntitySpawnS2CPayload(
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

    public static SyncSotoExteriorEntitySpawnS2CPayload fromEntity(
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
        return new SyncSotoExteriorEntitySpawnS2CPayload(
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
