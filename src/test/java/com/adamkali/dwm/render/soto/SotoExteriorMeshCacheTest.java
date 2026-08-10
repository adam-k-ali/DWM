package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
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
        SotoExteriorMeshCache.invalidateAll();
    }

    @Test
    void emptyWithoutSnapshot() {
        UUID id = UUID.randomUUID();
        assertFalse(SotoExteriorMeshCache.hasSnapshot(id));
        assertNull(SotoExteriorMeshCache.getShellState(id));
        assertNull(SotoExteriorMeshCache.getAtmosphere(id));
    }

    @Test
    void applySnapshot_storesShellMetadata() {
        UUID id = UUID.randomUUID();
        SotoExteriorMeshCache.applySnapshot(
                id,
                1,
                TardisChameleonVariant.FIRST_DOCTOR_BOX,
                0.75f,
                true,
                4,
                SotoAtmosphere.DEFAULT
        );

        assertTrue(SotoExteriorMeshCache.hasSnapshot(id));
        SotoExteriorMeshCache.ShellState shell = SotoExteriorMeshCache.getShellState(id);
        assertNotNull(shell);
        assertEquals(TardisChameleonVariant.FIRST_DOCTOR_BOX, shell.variant());
        assertEquals(0.75f, shell.doorSwing(), 1e-4f);
        assertTrue(shell.isOpen());
        assertEquals(4, shell.exteriorRotation());
    }

    @Test
    void applySnapshot_storesAtmosphere() {
        UUID id = UUID.randomUUID();
        SotoAtmosphere atmosphere = new SotoAtmosphere(
                BuiltinDimensionTypes.END.identifier(),
                18000L,
                0.0f,
                0.0f,
                0x000000,
                0xA080FF
        );
        SotoExteriorMeshCache.applySnapshot(
                id,
                1,
                TardisChameleonVariant.TT_CAPSULE,
                1.0f,
                true,
                0,
                atmosphere
        );

        SotoAtmosphere cached = SotoExteriorMeshCache.getAtmosphere(id);
        assertNotNull(cached);
        assertEquals(BuiltinDimensionTypes.END.identifier(), cached.dimensionEffectsId());
        assertEquals(18000L, cached.timeOfDay());
        assertEquals(0xA080FF, cached.biomeFogColor());
    }

    @Test
    void applySnapshot_ignoresOlderRevision() {
        UUID id = UUID.randomUUID();
        SotoExteriorMeshCache.applySnapshot(
                id, 5,
                TardisChameleonVariant.TT_CAPSULE, 1.0f, true, 0, SotoAtmosphere.DEFAULT
        );
        SotoExteriorMeshCache.applySnapshot(
                id, 4,
                TardisChameleonVariant.FIRST_DOCTOR_BOX, 0.0f, false, 8, SotoAtmosphere.DEFAULT
        );

        assertEquals(TardisChameleonVariant.TT_CAPSULE, SotoExteriorMeshCache.getShellState(id).variant());
        assertTrue(SotoExteriorMeshCache.getShellState(id).isOpen());
    }
}
