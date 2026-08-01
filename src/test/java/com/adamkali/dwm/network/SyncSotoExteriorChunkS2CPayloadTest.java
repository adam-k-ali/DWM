package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.soto.SotoExteriorSampler;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SyncSotoExteriorChunkS2CPayloadTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void fromSample_mapsWorldBlocksToFootprintRelative() {
        UUID id = UUID.randomUUID();
        BlockPos footprintOrigin = new BlockPos(10, 60, 20);
        BlockPos worldPos = footprintOrigin.add(3, 2, 4);
        SotoExteriorSampler.StreamChunkSample sample = new SotoExteriorSampler.StreamChunkSample(
                worldPos.getX() >> 4,
                worldPos.getZ() >> 4,
                Map.of(worldPos, Blocks.STONE.getDefaultState()),
                Map.of()
        );

        SyncSotoExteriorChunkS2CPayload payload =
                SyncSotoExteriorChunkS2CPayload.fromSample(id, footprintOrigin, sample);

        assertEquals(id, payload.tardisId());
        assertEquals(1, payload.blocks().size());
        assertEquals(3, payload.blocks().getFirst().relX());
        assertEquals(2, payload.blocks().getFirst().relY());
        assertEquals(4, payload.blocks().getFirst().relZ());
        assertEquals(BotiRelativePosCodec.stateId(Blocks.STONE.getDefaultState()), payload.blocks().getFirst().stateId());
        assertEquals(Blocks.STONE, payload.toBlockMap().get(new BlockPos(3, 2, 4)).getBlock());
        assertEquals(SyncSotoExteriorChunkS2CPayload.ID, payload.getId());
    }

    @Test
    void unloadPayload_retainsChunkCoords() {
        UUID id = UUID.randomUUID();
        UnloadSotoExteriorChunkS2CPayload payload = new UnloadSotoExteriorChunkS2CPayload(id, 4, -2);
        assertEquals(id, payload.tardisId());
        assertEquals(4, payload.chunkX());
        assertEquals(-2, payload.chunkZ());
        assertEquals(UnloadSotoExteriorChunkS2CPayload.ID, payload.getId());
    }

    @Test
    void entitySpawnUpdateRemove_roundTripFields() {
        UUID tardisId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", "minecraft:cow");

        SyncSotoExteriorEntitySpawnS2CPayload spawn = new SyncSotoExteriorEntitySpawnS2CPayload(
                tardisId, entityId, net.minecraft.util.Identifier.of("minecraft", "cow"),
                1.5f, 2.0f, 3.5f, 90f, 10f, 90f, 90f, 0.1, 0.0, 0.2, nbt
        );
        assertEquals(tardisId, spawn.tardisId());
        assertEquals(entityId, spawn.entityUuid());
        assertEquals(1.5f, spawn.relX(), 1e-4f);
        assertEquals(SyncSotoExteriorEntitySpawnS2CPayload.ID, spawn.getId());

        SyncSotoExteriorEntityUpdateS2CPayload update = new SyncSotoExteriorEntityUpdateS2CPayload(
                tardisId, entityId, 2f, 2f, 4f, 45f, 0f, 45f, 45f, 0.0, 0.0, 0.0
        );
        assertEquals(2f, update.relX(), 1e-4f);
        assertEquals(SyncSotoExteriorEntityUpdateS2CPayload.ID, update.getId());

        SyncSotoExteriorEntityRemoveS2CPayload remove =
                new SyncSotoExteriorEntityRemoveS2CPayload(tardisId, entityId);
        assertEquals(entityId, remove.entityUuid());
        assertEquals(SyncSotoExteriorEntityRemoveS2CPayload.ID, remove.getId());
    }

    @Test
    void requestGhost_retainsTardisId() {
        UUID id = UUID.randomUUID();
        RequestSotoGhostC2SPayload payload = new RequestSotoGhostC2SPayload(id);
        assertEquals(id, payload.tardisId());
        assertEquals(RequestSotoGhostC2SPayload.ID, payload.getId());
    }
}
