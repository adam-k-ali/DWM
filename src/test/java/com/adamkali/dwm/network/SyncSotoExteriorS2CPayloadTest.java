package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoExteriorSnapshot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import static org.junit.jupiter.api.Assertions.*;

class SyncSotoExteriorS2CPayloadTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void fromSnapshot_roundTripsShellAndAtmosphere() {
        UUID id = UUID.randomUUID();
        SotoAtmosphere atmosphere = new SotoAtmosphere(
                BuiltinDimensionTypes.NETHER.identifier(),
                13000L,
                0.4f,
                0.2f,
                0x112233,
                0x445566
        );

        SotoExteriorSnapshot snapshot = SotoExteriorSnapshot.of(
                id,
                7,
                TardisChameleonVariant.FOURTH_DOCTOR_BOX,
                0.8f,
                true,
                12,
                atmosphere
        );

        SyncSotoExteriorS2CPayload payload = SyncSotoExteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(id, payload.tardisId());
        assertEquals(7, payload.revision());
        assertEquals(SotoExteriorSnapshot.FORMAT_VERSION_SHELL_AND_ATMOSPHERE, payload.formatVersion());
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
        assertEquals(SyncSotoExteriorS2CPayload.ID, payload.type());
    }
}
