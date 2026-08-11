package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.network.SyncPortalMetaS2CPayload;
import com.adamkali.dwm.render.portal.PortalSceneStore;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import static org.junit.jupiter.api.Assertions.*;

class SotoExteriorMeshCacheTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void tearDown() {
        PortalSceneStore.invalidateAll();
    }

    @Test
    void emptyWithoutSnapshot() {
        UUID id = UUID.randomUUID();
        assertFalse(SotoExteriorMeshCache.hasSnapshot(id));
        assertNull(SotoExteriorMeshCache.getShellState(id));
        assertNull(SotoExteriorMeshCache.getAtmosphere(id));
    }

    @Test
    void applyMeta_storesShellMetadata() {
        UUID id = UUID.randomUUID();
        SyncPortalMetaS2CPayload payload = SyncPortalMetaS2CPayload.of(
                PortalStreamKind.SOTO,
                id,
                1,
                new PortalShellState(TardisChameleonVariant.FIRST_DOCTOR_BOX, 0.75f, true, 4),
                PortalAtmosphere.DEFAULT
        );
        PortalSceneStore.applyMeta(payload);

        assertTrue(SotoExteriorMeshCache.hasSnapshot(id));
        PortalShellState shell = SotoExteriorMeshCache.getShellState(id);
        assertNotNull(shell);
        assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, shell.variant());
        assertEquals(0.75f, shell.doorSwing(), 1e-4f);
        assertTrue(shell.isOpen());
        assertEquals(4, shell.exteriorRotation());
    }

    @Test
    void applyMeta_storesAtmosphere() {
        UUID id = UUID.randomUUID();
        PortalAtmosphere atmosphere = new PortalAtmosphere(
                BuiltinDimensionTypes.END.identifier(),
                18000L,
                0.0f,
                0.0f,
                0x000000,
                0xA080FF
        );
        SyncPortalMetaS2CPayload payload = SyncPortalMetaS2CPayload.of(
                PortalStreamKind.SOTO,
                id,
                1,
                new PortalShellState(TardisChameleonVariant.TT_CAPSULE, 1.0f, true, 0),
                atmosphere
        );
        PortalSceneStore.applyMeta(payload);

        SotoAtmosphere cached = SotoExteriorMeshCache.getAtmosphere(id);
        assertNotNull(cached);
        assertEquals(BuiltinDimensionTypes.END.identifier(), cached.dimensionEffectsId());
        assertEquals(18000L, cached.timeOfDay());
        assertEquals(0xA080FF, cached.biomeFogColor());
    }

    @Test
    void applyMeta_ignoresOlderRevision() {
        UUID id = UUID.randomUUID();
        SyncPortalMetaS2CPayload first = SyncPortalMetaS2CPayload.of(
                PortalStreamKind.SOTO, id, 5,
                new PortalShellState(TardisChameleonVariant.TT_CAPSULE, 1.0f, true, 0),
                PortalAtmosphere.DEFAULT
        );
        SyncPortalMetaS2CPayload older = SyncPortalMetaS2CPayload.of(
                PortalStreamKind.SOTO, id, 4,
                new PortalShellState(TardisChameleonVariant.FIRST_DOCTOR_BOX, 0.0f, false, 8),
                PortalAtmosphere.DEFAULT
        );
        PortalSceneStore.applyMeta(first);
        PortalSceneStore.applyMeta(older);

        assertEquals(TardisChameleonVariant.TT_CAPSULE, SotoExteriorMeshCache.getShellState(id).variant());
        assertTrue(SotoExteriorMeshCache.getShellState(id).isOpen());
    }
}
