package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import static org.junit.jupiter.api.Assertions.*;

class SyncPortalMetaS2CPayloadTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void of_roundTripsShellAndAtmosphereForBothKinds() {
        UUID id = UUID.randomUUID();
        PortalAtmosphere atmosphere = new PortalAtmosphere(
                BuiltinDimensionTypes.NETHER.identifier(),
                13000L,
                0.4f,
                0.2f,
                0x112233,
                0x445566
        );
        PortalShellState shell = new PortalShellState(
                TardisChameleonVariant.FOURTH_DOCTOR_BOX,
                0.8f,
                true,
                12
        );

        for (PortalStreamKind kind : PortalStreamKind.values()) {
            SyncPortalMetaS2CPayload payload = SyncPortalMetaS2CPayload.of(kind, id, 7, shell, atmosphere);

            assertEquals(kind, payload.kind());
            assertEquals(id, payload.tardisId());
            assertEquals(7, payload.revision());
            assertEquals(SyncPortalMetaS2CPayload.FORMAT_VERSION, payload.formatVersion());
            assertEquals(TardisChameleonVariant.FOURTH_DOCTOR_BOX.getId(), payload.variantId());
            assertEquals(0.8f, payload.doorSwing(), 1e-4f);
            assertTrue(payload.isOpen());
            assertEquals(12, payload.exteriorRotation());
            assertEquals(TardisChameleonVariant.FOURTH_DOCTOR_BOX, payload.variant());
            assertEquals(BuiltinDimensionTypes.NETHER.identifier(), payload.atmosphere().dimensionEffectsId());
            assertEquals(13000L, payload.atmosphere().timeOfDay());
            assertEquals(0.4f, payload.atmosphere().rainGradient(), 1e-4f);
            assertEquals(0.2f, payload.atmosphere().thunderGradient(), 1e-4f);
            assertEquals(0x112233, payload.atmosphere().biomeSkyColor());
            assertEquals(0x445566, payload.atmosphere().biomeFogColor());
            assertEquals(SyncPortalMetaS2CPayload.ID, payload.type());
            assertEquals(kind.toWire(), PortalStreamKind.fromWire(kind.toWire()).toWire());
        }
    }

    @Test
    void codec_roundTripsLightingFields() {
        UUID id = UUID.randomUUID();
        PortalAtmosphere atmosphere = new PortalAtmosphere(
                Identifier.fromNamespaceAndPath("dwm", "tardis"),
                18000L,
                0.1f,
                0.0f,
                0x010203,
                0x040506,
                0.35f,
                0x7A7AFF,
                0x302821,
                0xFFD88C,
                CardinalLighting.Type.NETHER
        );
        PortalShellState shell = new PortalShellState(TardisChameleonVariant.FOURTH_DOCTOR_BOX, 0.5f, true, 4);
        SyncPortalMetaS2CPayload sent = SyncPortalMetaS2CPayload.of(PortalStreamKind.BOTI, id, 3, shell, atmosphere);

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        SyncPortalMetaS2CPayload.CODEC.encode(buf, sent);
        SyncPortalMetaS2CPayload received = SyncPortalMetaS2CPayload.CODEC.decode(buf);

        assertEquals(0, buf.readableBytes());
        assertEquals(SyncPortalMetaS2CPayload.FORMAT_VERSION, received.formatVersion());
        assertEquals(atmosphere, received.atmosphere());
        assertEquals(CardinalLighting.Type.NETHER, received.atmosphere().cardinalLighting());
        assertEquals(0.35f, received.atmosphere().skyLightFactor(), 1e-6f);
        assertEquals(0x302821, received.atmosphere().ambientLightColor());
    }

    @Test
    void legacyAtmosphere_derivesNetherCardinalLightingOnlyForTheNether() {
        assertEquals(CardinalLighting.Type.NETHER, new PortalAtmosphere(
                BuiltinDimensionTypes.NETHER.identifier(), 0L, 0.0f, 0.0f, 0, 0).cardinalLighting());
        assertEquals(CardinalLighting.Type.DEFAULT, PortalAtmosphere.DEFAULT.cardinalLighting());
        assertEquals(PortalAtmosphere.DEFAULT_BLOCK_LIGHT_TINT, PortalAtmosphere.DEFAULT.blockLightTint());
    }
}
