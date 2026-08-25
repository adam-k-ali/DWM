package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;

class SotoGhostExteriorTest {

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
    void applyChunk_exposesBlockStateAndUnloadClears() {
        UUID id = UUID.randomUUID();
        BlockPos rel = new BlockPos(2, 1, 3);
        SyncPortalChunkS2CPayload payload = new SyncPortalChunkS2CPayload(
                PortalStreamKind.SOTO,
                id,
                1,
                2,
                100,
                64,
                200,
                List.of(new SyncPortalChunkS2CPayload.BlockEntry(
                        rel.getX(),
                        rel.getY(),
                        rel.getZ(),
                        BotiRelativePosCodec.stateId(Blocks.DIRT.defaultBlockState())
                )),
                List.of()
        );

        SotoGhostExterior.applyChunk(PortalStreamKind.SOTO, payload);

        SotoGhostExterior ghost = SotoGhostExterior.get(PortalStreamKind.SOTO, id);
        assertNotNull(ghost);
        assertEquals(1, ghost.chunkCount());
        assertEquals(Blocks.DIRT, ghost.getBlockState(rel).getBlock());
        assertEquals(new BlockPos(100, 64, 200), ghost.footprintOrigin());

        SotoGhostExterior.unloadChunk(PortalStreamKind.SOTO, id, 1, 2);
        assertEquals(0, ghost.chunkCount());
        assertTrue(ghost.getBlockState(rel).isAir());
    }

    @Test
    void chunkContentUnchanged_comparesBlocksAndBes() {
        BlockPos pos = new BlockPos(1, 2, 3);
        Map<BlockPos, BlockState> blocksA = Map.of(pos, Blocks.STONE.defaultBlockState());
        Map<BlockPos, BlockState> blocksB = Map.of(pos, Blocks.STONE.defaultBlockState());
        Map<BlockPos, BlockState> blocksC = Map.of(pos, Blocks.DIRT.defaultBlockState());
        Map<BlockPos, CompoundTag> bes = Map.of(pos, new CompoundTag());

        assertTrue(SotoGhostExterior.chunkContentUnchanged(blocksA, blocksB, null, null));
        assertTrue(SotoGhostExterior.chunkContentUnchanged(null, Map.of(), null, Map.of()));
        assertFalse(SotoGhostExterior.chunkContentUnchanged(blocksA, blocksC, null, null));
        assertFalse(SotoGhostExterior.chunkContentUnchanged(blocksA, blocksB, null, bes));
    }

    @Test
    void applyChunk_identicalWithDrawableMesh_skipsRebake() {
        UUID id = UUID.randomUUID();
        SyncPortalChunkS2CPayload payload = dirtChunk(id, 1, 2, Blocks.DIRT);

        SotoGhostExterior.applyChunk(PortalStreamKind.SOTO, payload);
        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 1, 2);
        assertTrue(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 1, 2));

        // Second identical apply must keep drawable mesh (skip onChunkApplied / bake).
        SotoGhostExterior.applyChunk(PortalStreamKind.SOTO, payload);
        assertTrue(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 1, 2));
        assertEquals(1, SotoGhostMeshCache.meshChunkCount(PortalStreamKind.SOTO, id));
    }

    @Test
    void applyChunk_contentChange_replacesDrawableMesh() {
        UUID id = UUID.randomUUID();
        SyncPortalChunkS2CPayload dirt = dirtChunk(id, 1, 2, Blocks.DIRT);
        SyncPortalChunkS2CPayload stone = dirtChunk(id, 1, 2, Blocks.STONE);

        SotoGhostExterior.applyChunk(PortalStreamKind.SOTO, dirt);
        SotoGhostMeshCache.markChunkMeshForTest(PortalStreamKind.SOTO, id, 1, 2);
        assertTrue(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 1, 2));

        SotoGhostExterior.applyChunk(PortalStreamKind.SOTO, stone);
        // Real bake in unit tests yields MARKER/empty — drawable test mesh must be replaced.
        assertFalse(SotoGhostMeshCache.hasDrawableChunk(PortalStreamKind.SOTO, id, 1, 2));
        assertTrue(SotoGhostMeshCache.arePassBatchesDirtyForTest(PortalStreamKind.SOTO, id));

        SotoGhostExterior ghost = SotoGhostExterior.get(PortalStreamKind.SOTO, id);
        assertNotNull(ghost);
        assertEquals(Blocks.STONE, ghost.getBlockState(new BlockPos(2, 1, 3)).getBlock());
    }

    @Test
    void removeEntity_andInvalidateClearState() {
        UUID id = UUID.randomUUID();
        SotoGhostExterior.getOrCreate(PortalStreamKind.SOTO, id);
        assertNotNull(SotoGhostExterior.get(PortalStreamKind.SOTO, id));
        SotoGhostExterior.removeEntity(PortalStreamKind.SOTO, id, UUID.randomUUID());
        assertFalse(SotoGhostExterior.hasEntities(PortalStreamKind.SOTO, id));
        SotoGhostExterior.invalidate(PortalStreamKind.SOTO, id);
        assertNull(SotoGhostExterior.get(PortalStreamKind.SOTO, id));
    }

    @Test
    void entityInterp_advancesLerpedPoseBetweenPackets() {
        UUID tardisId = UUID.randomUUID();
        UUID entityUuid = UUID.randomUUID();
        long t0 = 1_000L;

        SotoGhostExterior.putInterpForTest(
                PortalStreamKind.SOTO,
                tardisId,
                entityUuid,
                new com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState(
                        0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        t0
                )
        );
        SotoGhostExterior.advanceInterpForTest(
                PortalStreamKind.SOTO, tardisId, entityUuid,
                10.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                t0 + SotoGhostExterior.ENTITY_UPDATE_INTERVAL_MS
        );

        long receive = t0 + SotoGhostExterior.ENTITY_UPDATE_INTERVAL_MS;
        var atReceive = SotoGhostExterior.sampleLerpedPosesForTest(PortalStreamKind.SOTO, tardisId, receive);
        var atMid = SotoGhostExterior.sampleLerpedPosesForTest(
                PortalStreamKind.SOTO, tardisId, receive + SotoGhostExterior.ENTITY_UPDATE_INTERVAL_MS / 2
        );

        assertEquals(1, atReceive.size());
        assertEquals(1, atMid.size());
        assertEquals(0.0, atReceive.getFirst().x(), 1.0e-4);
        assertEquals(5.0, atMid.getFirst().x(), 1.0e-4);
    }

    @Test
    void usesIdentityInterp_onlyForItemEntities() {
        assertFalse(SotoGhostExterior.usesIdentityInterp(null));
    }

    @Test
    void animAgeInTicks_growsWithWallClock() {
        long start = 1_000L;
        assertEquals(0.0f, SotoGhostExterior.animAgeInTicks(start, start), 1.0e-4f);
        assertEquals(2.0f, SotoGhostExterior.animAgeInTicks(start, start + 100L), 1.0e-4f);
        assertEquals(20.0f, SotoGhostExterior.animAgeInTicks(start, start + 1_000L), 1.0e-4f);
        assertTrue(SotoGhostExterior.animAgeInTicks(start, start + 50_000L) > 2.0f);
    }

    @Test
    void nextInterpForUpdate_nullEntity_advances() {
        var previous = com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState.identity(
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1_000L
        );
        var next = SotoGhostExterior.nextInterpForUpdate(
                null, previous, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1_100L
        );
        assertEquals(0.0f, next.fromX(), 1.0e-4f);
        assertEquals(10.0f, next.toX(), 1.0e-4f);
        assertEquals(1_100L, next.receiveTimeMs());
    }

    @Test
    void nextInterpForUpdate_nullPrevious_usesIdentity() {
        var next = SotoGhostExterior.nextInterpForUpdate(
                null, null, 3.0f, 1.0f, 2.0f, 45.0f, 10.0f, 2_000L
        );
        assertEquals(3.0f, next.fromX(), 1.0e-4f);
        assertEquals(3.0f, next.toX(), 1.0e-4f);
        assertEquals(1.0f, next.fromY(), 1.0e-4f);
        assertEquals(1.0f, next.toY(), 1.0e-4f);
        assertEquals(2_000L, next.receiveTimeMs());
    }

    private static SyncPortalChunkS2CPayload dirtChunk(
            UUID id,
            int cx,
            int cz,
            net.minecraft.world.level.block.Block block
    ) {
        return new SyncPortalChunkS2CPayload(
                PortalStreamKind.SOTO,
                id,
                cx,
                cz,
                100,
                64,
                200,
                List.of(new SyncPortalChunkS2CPayload.BlockEntry(
                        2, 1, 3, BotiRelativePosCodec.stateId(block.defaultBlockState())
                )),
                List.of()
        );
    }
}
