package com.adamkali.dwm.render.soto.ghost;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.network.SyncSotoExteriorChunkS2CPayload;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SotoGhostExteriorTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void tearDown() {
        SotoGhostExterior.invalidateAll();
    }

    @Test
    void applyChunk_exposesBlockStateAndUnloadClears() {
        UUID id = UUID.randomUUID();
        BlockPos rel = new BlockPos(2, 1, 3);
        SyncSotoExteriorChunkS2CPayload payload = new SyncSotoExteriorChunkS2CPayload(
                id,
                1,
                2,
                100,
                64,
                200,
                List.of(new SyncSotoExteriorChunkS2CPayload.BlockEntry(
                        rel.getX(),
                        rel.getY(),
                        rel.getZ(),
                        BotiRelativePosCodec.stateId(Blocks.DIRT.getDefaultState())
                )),
                List.of()
        );

        SotoGhostExterior.applyChunk(payload);

        SotoGhostExterior ghost = SotoGhostExterior.get(id);
        assertNotNull(ghost);
        assertEquals(1, ghost.chunkCount());
        assertEquals(Blocks.DIRT, ghost.getBlockState(rel).getBlock());
        assertEquals(new BlockPos(100, 64, 200), ghost.footprintOrigin());

        SotoGhostExterior.unloadChunk(id, 1, 2);
        assertEquals(0, ghost.chunkCount());
        assertTrue(ghost.getBlockState(rel).isAir());
    }

    @Test
    void removeEntity_andInvalidateClearState() {
        UUID id = UUID.randomUUID();
        SotoGhostExterior.getOrCreate(id);
        assertNotNull(SotoGhostExterior.get(id));
        SotoGhostExterior.removeEntity(id, UUID.randomUUID());
        assertFalse(SotoGhostExterior.hasEntities(id));
        SotoGhostExterior.invalidate(id);
        assertNull(SotoGhostExterior.get(id));
    }
}
