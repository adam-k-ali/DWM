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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

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
    void removeEntity_andInvalidateClearState() {
        UUID id = UUID.randomUUID();
        SotoGhostExterior.getOrCreate(PortalStreamKind.SOTO, id);
        assertNotNull(SotoGhostExterior.get(PortalStreamKind.SOTO, id));
        SotoGhostExterior.removeEntity(PortalStreamKind.SOTO, id, UUID.randomUUID());
        assertFalse(SotoGhostExterior.hasEntities(PortalStreamKind.SOTO, id));
        SotoGhostExterior.invalidate(PortalStreamKind.SOTO, id);
        assertNull(SotoGhostExterior.get(PortalStreamKind.SOTO, id));
    }
}
