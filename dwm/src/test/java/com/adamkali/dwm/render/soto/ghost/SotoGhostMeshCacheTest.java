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
    void markerOnly_doesNotCountAsDrawableMesh() {
        UUID id = UUID.randomUUID();
        SotoGhostMeshCache.markChunkMarkerForTest(PortalStreamKind.SOTO, id, 0, 0);
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));
        assertEquals(0, SotoGhostMeshCache.meshChunkCount(PortalStreamKind.SOTO, id));

        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 1, 1);
        assertTrue(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));
        assertEquals(1, SotoGhostMeshCache.meshChunkCount(PortalStreamKind.SOTO, id));
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
    void hasDrawableChunk_trueOnlyForDrawableMesh() {
        UUID id = UUID.randomUUID();
        assertFalse(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 0, 0));

        SotoGhostMeshCache.markChunkMarkerForTest(PortalStreamKind.SOTO, id, 0, 0);
        assertFalse(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 0, 0));

        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 0, 0);
        assertTrue(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 0, 0));
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

    private static final SotoGhostMeshCache.TerrainPass OPAQUE = SotoGhostMeshCache.TerrainPass.OPAQUE;
    private static final SotoGhostMeshCache.TerrainPass CUTOUT = SotoGhostMeshCache.TerrainPass.CUTOUT;

    @Test
    void editingChunkA_doesNotRecreateChunkBBuffers() {
        UUID id = UUID.randomUUID();
        SotoGhostMeshCache.resetBufferStats();
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.SOTO, id, 0, 0, OPAQUE, CUTOUT);
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.SOTO, id, 1, 0, OPAQUE, CUTOUT);
        List<SotoGhostMeshCache.LayerBuffer> aBefore = SotoGhostMeshCache.chunkBuffersForTest(PortalStreamKind.SOTO, id, 0, 0);
        List<SotoGhostMeshCache.LayerBuffer> bBefore = SotoGhostMeshCache.chunkBuffersForTest(PortalStreamKind.SOTO, id, 1, 0);
        long createdBefore = SotoGhostMeshCache.buffersCreated();

        // Re-bake A only.
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.SOTO, id, 0, 0, OPAQUE);

        List<SotoGhostMeshCache.LayerBuffer> bAfter = SotoGhostMeshCache.chunkBuffersForTest(PortalStreamKind.SOTO, id, 1, 0);
        assertEquals(bBefore.size(), bAfter.size());
        for (int i = 0; i < bBefore.size(); i++) {
            assertSame(bBefore.get(i), bAfter.get(i), "chunk B buffers must be the same objects");
            assertFalse(bAfter.get(i).isClosed());
        }
        for (SotoGhostMeshCache.LayerBuffer old : aBefore) {
            assertTrue(old.isClosed(), "chunk A's replaced buffers must be closed");
        }
        assertEquals(createdBefore + 1, SotoGhostMeshCache.buffersCreated(), "only A's new buffer is created");
        assertEquals(2, SotoGhostMeshCache.buffersClosed());
    }

    @Test
    void unloadAndInvalidate_releaseEveryBuffer() {
        UUID id = UUID.randomUUID();
        SotoGhostMeshCache.resetBufferStats();
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.SOTO, id, 0, 0, OPAQUE, CUTOUT);
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.SOTO, id, 3, 3, OPAQUE);
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.BOTI, id, 5, 5, CUTOUT);
        assertEquals(4, SotoGhostMeshCache.buffersCreated());
        assertEquals(2, SotoGhostMeshCache.meshChunkCount(PortalStreamKind.SOTO, id));

        SotoGhostMeshCache.onChunkUnloaded(PortalStreamKind.SOTO, id, 0, 0);
        assertEquals(2, SotoGhostMeshCache.buffersClosed());

        SotoGhostMeshCache.invalidate(PortalStreamKind.SOTO, id);
        assertEquals(3, SotoGhostMeshCache.buffersClosed());
        assertFalse(SotoGhostMeshCache.hasMeshes(PortalStreamKind.SOTO, id));

        SotoGhostMeshCache.invalidateAll();
        assertEquals(SotoGhostMeshCache.buffersCreated(), SotoGhostMeshCache.buffersClosed());
    }

    @Test
    void replacingRealMeshWithMarker_closesBuffers() {
        UUID id = UUID.randomUUID();
        SotoGhostMeshCache.resetBufferStats();
        SotoGhostMeshCache.markChunkBuffersForTest(PortalStreamKind.SOTO, id, 0, 0, OPAQUE);
        List<SotoGhostMeshCache.LayerBuffer> before = SotoGhostMeshCache.chunkBuffersForTest(PortalStreamKind.SOTO, id, 0, 0);
        SotoGhostMeshCache.markChunkMarkerForTest(PortalStreamKind.SOTO, id, 0, 0);
        assertTrue(before.get(0).isClosed());
        assertFalse(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 0, 0));
        assertEquals(SotoGhostMeshCache.buffersCreated(), SotoGhostMeshCache.buffersClosed());
    }
}
