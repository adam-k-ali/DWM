package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.boti.BotiRelativePosCodec;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;

import static org.junit.jupiter.api.Assertions.*;

class SyncPortalChunkS2CPayloadTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void fromSample_mapsWorldBlocksToFootprintRelativeForBothKinds() {
        UUID id = UUID.randomUUID();
        BlockPos footprintOrigin = new BlockPos(10, 60, 20);
        BlockPos worldPos = footprintOrigin.offset(3, 2, 4);
        PortalStreamSample sample = new PortalStreamSample(
                worldPos.getX() >> 4,
                worldPos.getZ() >> 4,
                Map.of(worldPos, Blocks.STONE.defaultBlockState()),
                Map.of(),
                new PortalLightData(worldPos, 1, 1, 1, new byte[]{PortalLightData.pack(3, 11)})
        );

        for (PortalStreamKind kind : PortalStreamKind.values()) {
            SyncPortalChunkS2CPayload payload =
                    SyncPortalChunkS2CPayload.fromSample(kind, id, footprintOrigin, sample);

            assertEquals(kind, payload.kind());
            assertEquals(id, payload.tardisId());
            assertEquals(1, payload.blocks().size());
            assertEquals(3, payload.blocks().getFirst().relX());
            assertEquals(2, payload.blocks().getFirst().relY());
            assertEquals(4, payload.blocks().getFirst().relZ());
            assertEquals(BotiRelativePosCodec.stateId(Blocks.STONE.defaultBlockState()), payload.blocks().getFirst().stateId());
            assertEquals(Blocks.STONE, payload.toBlockMap().get(new BlockPos(3, 2, 4)).getBlock());
            assertEquals(new BlockPos(3, 2, 4), payload.lightData().min());
            assertEquals(3, payload.lightData().brightness(LightLayer.BLOCK, new BlockPos(3, 2, 4), -1));
            assertEquals(11, payload.lightData().brightness(LightLayer.SKY, new BlockPos(3, 2, 4), -1));
            assertEquals(SyncPortalChunkS2CPayload.ID, payload.type());
        }
    }

    @Test
    void unloadPayload_retainsKindAndChunkCoords() {
        UUID id = UUID.randomUUID();
        UnloadPortalChunkS2CPayload payload =
                new UnloadPortalChunkS2CPayload(PortalStreamKind.BOTI, id, 4, -2);
        assertEquals(PortalStreamKind.BOTI, payload.kind());
        assertEquals(id, payload.tardisId());
        assertEquals(4, payload.chunkX());
        assertEquals(-2, payload.chunkZ());
        assertEquals(UnloadPortalChunkS2CPayload.ID, payload.type());
    }

    @Test
    void entitySpawnUpdateRemove_roundTripFields() {
        UUID tardisId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", "minecraft:cow");

        SyncPortalEntitySpawnS2CPayload spawn = new SyncPortalEntitySpawnS2CPayload(
                PortalStreamKind.SOTO, tardisId, entityId,
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "cow"),
                1.5f, 2.0f, 3.5f, 90f, 10f, 90f, 90f, 0.1, 0.0, 0.2, nbt
        );
        assertEquals(PortalStreamKind.SOTO, spawn.kind());
        assertEquals(tardisId, spawn.tardisId());
        assertEquals(entityId, spawn.entityUuid());
        assertEquals(1.5f, spawn.relX(), 1e-4f);
        assertEquals(SyncPortalEntitySpawnS2CPayload.ID, spawn.type());

        SyncPortalEntityUpdateS2CPayload update = new SyncPortalEntityUpdateS2CPayload(
                PortalStreamKind.BOTI, tardisId, entityId, 2f, 2f, 4f, 45f, 0f, 45f, 45f, 0.0, 0.0, 0.0
        );
        assertEquals(PortalStreamKind.BOTI, update.kind());
        assertEquals(2f, update.relX(), 1e-4f);
        assertEquals(SyncPortalEntityUpdateS2CPayload.ID, update.type());

        SyncPortalEntityRemoveS2CPayload remove =
                new SyncPortalEntityRemoveS2CPayload(PortalStreamKind.SOTO, tardisId, entityId);
        assertEquals(entityId, remove.entityUuid());
        assertEquals(SyncPortalEntityRemoveS2CPayload.ID, remove.type());
    }

    @Test
    void requestPortalStream_retainsKindAndTardisId() {
        UUID id = UUID.randomUUID();
        RequestPortalStreamC2SPayload payload =
                new RequestPortalStreamC2SPayload(PortalStreamKind.BOTI, id);
        assertEquals(PortalStreamKind.BOTI, payload.kind());
        assertEquals(id, payload.tardisId());
        assertEquals(RequestPortalStreamC2SPayload.ID, payload.type());
    }
}
