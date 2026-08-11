package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.block.Blocks;

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
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));

        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 1, 2);
        assertTrue(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));
        assertEquals(1, SotoGhostMeshCache.meshChunkCount(PortalStreamKind.SOTO, id));

        SotoGhostMeshCache.invalidate(PortalStreamKind.SOTO, id);
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));
        assertEquals(0, SotoGhostMeshCache.meshChunkCount(PortalStreamKind.SOTO, id));
    }

    @Test
    void unloadChunk_clearsMeshMarker() {
        UUID id = UUID.randomUUID();
        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 3, 4);
        assertTrue(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));

        SotoGhostMeshCache.onChunkUnloaded(PortalStreamKind.SOTO, id, 3, 4);
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));
    }

    @Test
    void applyChunkUnload_clearsMeshViaGhostExterior() {
        UUID id = UUID.randomUUID();
        SyncPortalChunkS2CPayload payload = new SyncPortalChunkS2CPayload(
                PortalStreamKind.SOTO,
                id,
                1,
                2,
                100,
                64,
                200,
                List.of(new SyncPortalChunkS2CPayload.BlockEntry(
                        2, 1, 3, BotiRelativePosCodec.stateId(Blocks.DIRT.defaultBlockState())
                )),
                List.of()
        );
        SotoGhostExterior.applyChunk(PortalStreamKind.SOTO, payload);
        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 1, 2);
        assertTrue(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));

        SotoGhostExterior.unloadChunk(PortalStreamKind.SOTO, id, 1, 2);
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));
    }

    @Test
    void invalidateAll_clearsAllMeshes() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, a, 0, 0);
        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.BOTI, b, 1, 1);
        SotoGhostMeshCache.invalidateAll();
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, a));
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.BOTI, b));
    }
}
