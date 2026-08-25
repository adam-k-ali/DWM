package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
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
}
