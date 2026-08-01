package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.network.SyncSotoExteriorChunkS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SotoGhostMeshCacheTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void tearDown() {
        SotoGhostExterior.invalidateAll();
        SotoGhostMeshCache.invalidateAll();
    }

    @Test
    void markChunkMesh_hasMeshesUntilInvalidate() {
        UUID id = UUID.randomUUID();
        assertFalse(SotoGhostMeshCache.hasMeshes(id));

        SotoGhostMeshCache.markChunkMeshForTest(id, 1, 2);
        assertTrue(SotoGhostMeshCache.hasMeshes(id));
        assertEquals(1, SotoGhostMeshCache.meshChunkCount(id));

        SotoGhostMeshCache.invalidate(id);
        assertFalse(SotoGhostMeshCache.hasMeshes(id));
        assertEquals(0, SotoGhostMeshCache.meshChunkCount(id));
    }

    @Test
    void unloadChunk_clearsMeshMarker() {
        UUID id = UUID.randomUUID();
        SotoGhostMeshCache.markChunkMeshForTest(id, 3, 4);
        assertTrue(SotoGhostMeshCache.hasMeshes(id));

        SotoGhostMeshCache.onChunkUnloaded(id, 3, 4);
        assertFalse(SotoGhostMeshCache.hasMeshes(id));
    }

    @Test
    void applyChunkUnload_clearsMeshViaGhostExterior() {
        UUID id = UUID.randomUUID();
        SyncSotoExteriorChunkS2CPayload payload = new SyncSotoExteriorChunkS2CPayload(
                id,
                1,
                2,
                100,
                64,
                200,
                List.of(new SyncSotoExteriorChunkS2CPayload.BlockEntry(
                        2, 1, 3, BotiRelativePosCodec.stateId(Blocks.DIRT.getDefaultState())
                )),
                List.of()
        );
        SotoGhostExterior.applyChunk(payload);
        // Headless tests cannot bake VertexBuffers; seed a marker so unload wiring is observable.
        SotoGhostMeshCache.markChunkMeshForTest(id, 1, 2);
        assertTrue(SotoGhostMeshCache.hasMeshes(id));

        SotoGhostExterior.unloadChunk(id, 1, 2);
        assertFalse(SotoGhostMeshCache.hasMeshes(id));
    }

    @Test
    void invalidateAll_clearsAllMeshes() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        SotoGhostMeshCache.markChunkMeshForTest(a, 0, 0);
        SotoGhostMeshCache.markChunkMeshForTest(b, 1, 1);
        SotoGhostMeshCache.invalidateAll();
        assertFalse(SotoGhostMeshCache.hasMeshes(a));
        assertFalse(SotoGhostMeshCache.hasMeshes(b));
    }
}
