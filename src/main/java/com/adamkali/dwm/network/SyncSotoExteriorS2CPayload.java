package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSnapshot;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.UUID;

/**
 * S2C SOTO exterior shell + atmosphere snapshot.
 * formatVersion 5 = shell metadata + atmosphere (no footprint blocks/entities).
 */
public record SyncSotoExteriorS2CPayload(
        byte formatVersion,
        UUID tardisId,
        int revision,
        Identifier variantId,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation,
        SotoAtmosphere atmosphere
) implements CustomPayload {
    public static final CustomPayload.Id<SyncSotoExteriorS2CPayload> ID =
            new CustomPayload.Id<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncSotoExteriorS2CPayload> CODEC =
            PacketCodec.of(SyncSotoExteriorS2CPayload::encode, SyncSotoExteriorS2CPayload::decode);

    private static void encode(SyncSotoExteriorS2CPayload payload, RegistryByteBuf buf) {
        buf.writeByte(payload.formatVersion);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        PacketCodecs.VAR_INT.encode(buf, payload.revision);
        Identifier.PACKET_CODEC.encode(buf, payload.variantId);
        buf.writeFloat(payload.doorSwing);
        buf.writeBoolean(payload.isOpen);
        PacketCodecs.VAR_INT.encode(buf, payload.exteriorRotation);
        encodeAtmosphere(payload.atmosphere(), buf);
    }

    private static SyncSotoExteriorS2CPayload decode(RegistryByteBuf buf) {
        byte formatVersion = buf.readByte();
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int revision = PacketCodecs.VAR_INT.decode(buf);
        Identifier variantId = Identifier.PACKET_CODEC.decode(buf);
        float doorSwing = buf.readFloat();
        boolean isOpen = buf.readBoolean();
        int exteriorRotation = PacketCodecs.VAR_INT.decode(buf);
        SotoAtmosphere atmosphere = decodeAtmosphere(buf);
        return new SyncSotoExteriorS2CPayload(
                formatVersion,
                tardisId,
                revision,
                variantId,
                doorSwing,
                isOpen,
                exteriorRotation,
                atmosphere
        );
    }

    private static void encodeAtmosphere(SotoAtmosphere atmosphere, RegistryByteBuf buf) {
        SotoAtmosphere value = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        Identifier.PACKET_CODEC.encode(buf, value.dimensionEffectsId());
        buf.writeLong(value.timeOfDay());
        buf.writeFloat(value.rainGradient());
        buf.writeFloat(value.thunderGradient());
        buf.writeInt(value.biomeSkyColor());
        buf.writeInt(value.biomeFogColor());
    }

    private static SotoAtmosphere decodeAtmosphere(RegistryByteBuf buf) {
        Identifier effectsId = Identifier.PACKET_CODEC.decode(buf);
        if (effectsId == null) {
            effectsId = DimensionTypes.OVERWORLD_ID;
        }
        return new SotoAtmosphere(
                effectsId,
                buf.readLong(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static SyncSotoExteriorS2CPayload fromSnapshot(SotoExteriorSnapshot snapshot) {
        return new SyncSotoExteriorS2CPayload(
                (byte) snapshot.formatVersion(),
                snapshot.tardisId(),
                snapshot.revision(),
                snapshot.variant().getId(),
                snapshot.doorSwing(),
                snapshot.isOpen(),
                snapshot.exteriorRotation(),
                snapshot.atmosphere()
        );
    }

    public TardisChameleonVariant variant() {
        try {
            return TardisChameleonVariant.fromId(variantId);
        } catch (IllegalArgumentException e) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
