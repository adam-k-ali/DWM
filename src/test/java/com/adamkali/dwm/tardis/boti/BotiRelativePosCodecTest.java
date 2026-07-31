package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.network.SyncBotiInteriorS2CPayload;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotiRelativePosCodecTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void packUnpack_RoundTripsFootprintCoords() {
        for (int x = 0; x < BotiInteriorSampler.SIZE_X; x++) {
            for (int y = 0; y < BotiInteriorSampler.SIZE_Y; y++) {
                for (int z = 0; z < BotiInteriorSampler.SIZE_Z; z++) {
                    BlockPos original = new BlockPos(x, y, z);
                    assertEquals(original, BotiRelativePosCodec.unpack(BotiRelativePosCodec.pack(original)));
                }
            }
        }
    }

    @Test
    void snapshotPayload_RoundTripsBlockMap() {
        UUID tardisId = UUID.randomUUID();
        Map<BlockPos, BlockState> blocks = Map.of(
                new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.getDefaultState(),
                new BlockPos(5, 1, 5), DWMBlocks.TEAL_BIG_ROUNDEL_A.getDefaultState()
        );
        BotiInteriorSnapshot snapshot = BotiInteriorSnapshot.of(tardisId, 7, blocks);
        SyncBotiInteriorS2CPayload payload = SyncBotiInteriorS2CPayload.fromSnapshot(snapshot);

        assertEquals(BotiInteriorSnapshot.FORMAT_VERSION_BLOCKS, payload.formatVersion());
        assertEquals(tardisId, payload.tardisId());
        assertEquals(7, payload.revision());
        assertEquals(2, payload.blocks().size());

        Map<BlockPos, BlockState> decoded = payload.toBlockMap();
        assertEquals(blocks.get(new BlockPos(0, 0, 0)), decoded.get(new BlockPos(0, 0, 0)));
        assertEquals(blocks.get(new BlockPos(5, 1, 5)), decoded.get(new BlockPos(5, 1, 5)));
        assertFalse(decoded.containsValue(Blocks.AIR.getDefaultState()));
    }
}
