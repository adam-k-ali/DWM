package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSnapshot;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

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
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncSotoExteriorS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_SOTO_EXTERIOR_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSotoExteriorS2CPayload> CODEC =
            StreamCodec.ofMember(SyncSotoExteriorS2CPayload::encode, SyncSotoExteriorS2CPayload::decode);

    private static void encode(SyncSotoExteriorS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.formatVersion);
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        ByteBufCodecs.VAR_INT.encode(buf, payload.revision);
        Identifier.STREAM_CODEC.encode(buf, payload.variantId);
        buf.writeFloat(payload.doorSwing);
        buf.writeBoolean(payload.isOpen);
        ByteBufCodecs.VAR_INT.encode(buf, payload.exteriorRotation);
        encodeAtmosphere(payload.atmosphere(), buf);
    }

    private static SyncSotoExteriorS2CPayload decode(RegistryFriendlyByteBuf buf) {
        byte formatVersion = buf.readByte();
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int revision = ByteBufCodecs.VAR_INT.decode(buf);
        Identifier variantId = Identifier.STREAM_CODEC.decode(buf);
        float doorSwing = buf.readFloat();
        boolean isOpen = buf.readBoolean();
        int exteriorRotation = ByteBufCodecs.VAR_INT.decode(buf);
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

    private static void encodeAtmosphere(SotoAtmosphere atmosphere, RegistryFriendlyByteBuf buf) {
        SotoAtmosphere value = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        Identifier.STREAM_CODEC.encode(buf, value.dimensionEffectsId());
        buf.writeLong(value.timeOfDay());
        buf.writeFloat(value.rainGradient());
        buf.writeFloat(value.thunderGradient());
        buf.writeInt(value.biomeSkyColor());
        buf.writeInt(value.biomeFogColor());
    }

    private static SotoAtmosphere decodeAtmosphere(RegistryFriendlyByteBuf buf) {
        Identifier effectsId = Identifier.STREAM_CODEC.decode(buf);
        if (effectsId == null) {
            effectsId = BuiltinDimensionTypes.OVERWORLD.identifier();
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
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
