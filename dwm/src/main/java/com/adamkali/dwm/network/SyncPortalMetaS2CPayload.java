package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

/**
 * S2C portal shell + atmosphere metadata (shared by BOTI and SOTO).
 * formatVersion 1 = shell + atmosphere.
 */
public record SyncPortalMetaS2CPayload(
        byte formatVersion,
        PortalStreamKind kind,
        UUID tardisId,
        int revision,
        Identifier variantId,
        float doorSwing,
        boolean isOpen,
        int exteriorRotation,
        PortalAtmosphere atmosphere
) implements CustomPacketPayload {
    public static final byte FORMAT_VERSION = 1;

    public static final CustomPacketPayload.Type<SyncPortalMetaS2CPayload> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.SYNC_PORTAL_META_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPortalMetaS2CPayload> CODEC =
            StreamCodec.ofMember(SyncPortalMetaS2CPayload::encode, SyncPortalMetaS2CPayload::decode);

    private static void encode(SyncPortalMetaS2CPayload payload, RegistryFriendlyByteBuf buf) {
        buf.writeByte(payload.formatVersion);
        buf.writeByte(payload.kind.toWire());
        DWMPacketCodecs.UUID_PACKET_CODEC.encode(buf, payload.tardisId);
        ByteBufCodecs.VAR_INT.encode(buf, payload.revision);
        Identifier.STREAM_CODEC.encode(buf, payload.variantId);
        buf.writeFloat(payload.doorSwing);
        buf.writeBoolean(payload.isOpen);
        ByteBufCodecs.VAR_INT.encode(buf, payload.exteriorRotation);
        encodeAtmosphere(payload.atmosphere(), buf);
    }

    private static SyncPortalMetaS2CPayload decode(RegistryFriendlyByteBuf buf) {
        byte formatVersion = buf.readByte();
        PortalStreamKind kind = PortalStreamKind.fromWire(buf.readByte());
        UUID tardisId = DWMPacketCodecs.UUID_PACKET_CODEC.decode(buf);
        int revision = ByteBufCodecs.VAR_INT.decode(buf);
        Identifier variantId = Identifier.STREAM_CODEC.decode(buf);
        float doorSwing = buf.readFloat();
        boolean isOpen = buf.readBoolean();
        int exteriorRotation = ByteBufCodecs.VAR_INT.decode(buf);
        PortalAtmosphere atmosphere = decodeAtmosphere(buf);
        return new SyncPortalMetaS2CPayload(
                formatVersion,
                kind,
                tardisId,
                revision,
                variantId,
                doorSwing,
                isOpen,
                exteriorRotation,
                atmosphere
        );
    }

    private static void encodeAtmosphere(PortalAtmosphere atmosphere, RegistryFriendlyByteBuf buf) {
        PortalAtmosphere value = atmosphere == null ? PortalAtmosphere.DEFAULT : atmosphere;
        Identifier.STREAM_CODEC.encode(buf, value.dimensionEffectsId());
        buf.writeLong(value.timeOfDay());
        buf.writeFloat(value.rainGradient());
        buf.writeFloat(value.thunderGradient());
        buf.writeInt(value.biomeSkyColor());
        buf.writeInt(value.biomeFogColor());
    }

    private static PortalAtmosphere decodeAtmosphere(RegistryFriendlyByteBuf buf) {
        Identifier effectsId = Identifier.STREAM_CODEC.decode(buf);
        if (effectsId == null) {
            effectsId = BuiltinDimensionTypes.OVERWORLD.identifier();
        }
        return new PortalAtmosphere(
                effectsId,
                buf.readLong(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static SyncPortalMetaS2CPayload of(
            PortalStreamKind kind,
            UUID tardisId,
            int revision,
            PortalShellState shell,
            PortalAtmosphere atmosphere
    ) {
        return new SyncPortalMetaS2CPayload(
                FORMAT_VERSION,
                kind,
                tardisId,
                revision,
                shell.variant().getId(),
                shell.doorSwing(),
                shell.isOpen(),
                shell.exteriorRotation(),
                atmosphere == null ? PortalAtmosphere.DEFAULT : atmosphere
        );
    }

    public TardisChameleonVariant variant() {
        try {
            return TardisChameleonVariant.fromId(variantId);
        } catch (IllegalArgumentException e) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
    }

    public PortalShellState shellState() {
        return new PortalShellState(variant(), doorSwing, isOpen, exteriorRotation);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
