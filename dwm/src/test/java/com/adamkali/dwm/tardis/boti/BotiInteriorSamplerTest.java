package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;

class BotiInteriorSamplerTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @AfterEach
    void tearDown() {
        BotiPlotIndex.clear();
    }

    @Test
    void filterVisible_excludesAirLightAndInteriorDoorIncludesConsole() {
        Map<BlockPos, BlockState> input = new HashMap<>();
        input.put(new BlockPos(0, 0, 0), DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState());
        input.put(new BlockPos(1, 0, 0), Blocks.AIR.defaultBlockState());
        input.put(new BlockPos(2, 0, 0), Blocks.LIGHT.defaultBlockState());
        input.put(new BlockPos(3, 0, 0), DWMBlocks.TARDIS_INTERIOR_DOOR.defaultBlockState());
        input.put(new BlockPos(4, 0, 0), DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState());

        Map<BlockPos, BlockState> visible = BotiInteriorSampler.filterVisible(input);

        assertEquals(2, visible.size());
        assertTrue(visible.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(visible.containsKey(new BlockPos(4, 0, 0)));
    }

    @Test
    void filterVisible_matchesBlueprintHelper() {
        Map<BlockPos, BlockState> fromLayout = FirstDoctorConsoleRoomLayout.botiVisiblePlacements();
        Map<BlockPos, BlockState> fromSampler = BotiInteriorSampler.filterVisible(FirstDoctorConsoleRoomLayout.placements());
        assertEquals(fromLayout, fromSampler);
        assertFalse(fromLayout.isEmpty());
    }

    @Test
    void isBotiVisible_excludesInteriorDoorIncludesConsoleAndChest() {
        assertFalse(BotiInteriorSampler.isBotiVisible(DWMBlocks.TARDIS_INTERIOR_DOOR.defaultBlockState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(Blocks.CHEST.defaultBlockState()));
        assertFalse(BotiInteriorSampler.isBotiVisible(Blocks.LIGHT.defaultBlockState()));
    }

    @Test
    void windowChunkBounds_spansTheWholeWindow() {
        BlockPos origin = new BlockPos(TardisPlotAllocator.PLOT_SPACING * 10, 64, TardisPlotAllocator.PLOT_SPACING * -3);
        BlockPos window = TardisPlotAllocator.windowOrigin(origin);
        int last = TardisPlotAllocator.PLOT_SPACING - 1;
        // Window x/z = anchor - 4995 .. anchor + 5004: not chunk-aligned, so edge columns are shared.
        assertArrayEquals(new int[]{
                window.getX() >> 4, (window.getX() + last) >> 4,
                window.getZ() >> 4, (window.getZ() + last) >> 4
        }, BotiInteriorSampler.windowChunkBounds(origin));
    }

    @Test
    void streamChunkBounds_capsTheStreamAroundTheAnchor() {
        BlockPos origin = new BlockPos(0, 64, 0);
        int max = BotiInteriorSampler.MAX_STREAM_RADIUS_CHUNKS;
        int[] capped = {-max, max, -max, max};
        int radius = BotiInteriorSampler.clipStreamRadiusChunks(32);
        assertEquals(max, radius);
        assertArrayEquals(capped, BotiInteriorSampler.streamChunkBounds(origin, radius));
        // A smaller view distance streams a smaller box; the window never clips it.
        assertArrayEquals(new int[]{-2, 2, -2, 2}, BotiInteriorSampler.streamChunkBounds(origin, 2));
        // The capped stream still covers the console room and both lobbies (anchor chunk -2..+2).
        assertTrue(max >= 2);
    }

    @Test
    void clipStreamRadiusChunks_capsToTheBotiStream() {
        assertEquals(3, BotiInteriorSampler.MAX_STREAM_RADIUS_CHUNKS);
        assertEquals(0, BotiInteriorSampler.clipStreamRadiusChunks(0));
        assertEquals(0, BotiInteriorSampler.clipStreamRadiusChunks(-4));
        assertEquals(2, BotiInteriorSampler.clipStreamRadiusChunks(2));
        assertEquals(3, BotiInteriorSampler.clipStreamRadiusChunks(12));
        assertEquals(
                BotiInteriorSampler.clipStreamRadiusChunks(3),
                BotiInteriorSampler.clipStreamRadiusChunks(32)
        );
    }

    @Test
    void isInsideWindowStream_rejectsNeighborWindowsAndFarPositions() {
        BlockPos origin = new BlockPos(0, 64, 0);
        int offset = TardisPlotAllocator.WINDOW_OFFSET;
        int span = TardisPlotAllocator.PLOT_SPACING - offset;
        assertTrue(BotiInteriorSampler.isInsideWindowStream(origin, origin, 2));
        assertTrue(BotiInteriorSampler.isInsideWindowStream(origin.offset(-20, 0, 30), origin, 2));
        // Inside the window but far outside the stream.
        assertFalse(BotiInteriorSampler.isInsideWindowStream(origin.offset(-offset, 0, -offset), origin, 2));
        assertFalse(BotiInteriorSampler.isInsideWindowStream(origin.offset(span - 1, 0, span - 1), origin, 2));
        // Outside the window.
        assertFalse(BotiInteriorSampler.isInsideWindowStream(origin.offset(span, 0, 0), origin, 12));
        assertFalse(BotiInteriorSampler.isInsideWindowStream(origin.offset(-offset - 1, 0, 0), origin, 12));
        assertFalse(BotiInteriorSampler.isInsideWindowStream(origin.offset(0, 0, -offset - 1), origin, 12));
    }

    @Test
    void includePos_clipsSamplingToTheWindowPerBlock() {
        BlockPos anchor = new BlockPos(TardisPlotAllocator.PLOT_SPACING * 3, 64, TardisPlotAllocator.PLOT_SPACING * 5);
        int offset = TardisPlotAllocator.WINDOW_OFFSET;
        int span = TardisPlotAllocator.PLOT_SPACING - offset;
        assertTrue(BotiInteriorSampler.INSTANCE.includePos(anchor, anchor));
        assertTrue(BotiInteriorSampler.INSTANCE.includePos(anchor.offset(-32, 0, 0), anchor));
        assertFalse(BotiInteriorSampler.INSTANCE.includePos(anchor.offset(-offset - 1, 0, 0), anchor));
        assertTrue(BotiInteriorSampler.INSTANCE.includePos(anchor.offset(-offset, 0, 0), anchor));
        assertTrue(BotiInteriorSampler.INSTANCE.includePos(anchor.offset(span - 1, 0, span - 1), anchor));
        assertFalse(BotiInteriorSampler.INSTANCE.includePos(anchor.offset(span, 0, 0), anchor));
        assertFalse(BotiInteriorSampler.INSTANCE.includePos(anchor.offset(0, 0, span), anchor));
    }

    @Test
    void isFootprintLightReady_waitsForStampedSourceBrightness() {
        ServerLevel world = Mockito.mock(ServerLevel.class);
        BlockPos origin = new BlockPos(15, 64, 15);
        BlockPos sourcePos = origin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));
        Mockito.when(world.getBlockState(sourcePos)).thenReturn(Blocks.AIR.defaultBlockState());

        assertFalse(BotiInteriorSampler.isFootprintLightReady(world, origin));

        Mockito.when(world.getBlockState(sourcePos)).thenReturn(Blocks.LIGHT.defaultBlockState());
        Mockito.when(world.getBrightness(LightLayer.BLOCK, sourcePos)).thenReturn(0);
        assertFalse(BotiInteriorSampler.isFootprintLightReady(world, origin));

        Mockito.when(world.getBrightness(LightLayer.BLOCK, sourcePos)).thenReturn(15);
        assertTrue(BotiInteriorSampler.isFootprintLightReady(world, origin));
        assertFalse(BotiInteriorSampler.isFootprintLightReady(null, origin));
        assertFalse(BotiInteriorSampler.isFootprintLightReady(world, null));
    }

    @Test
    void captureEntityNbt_NullEntityReturnsNull() {
        assertNull(BotiInteriorSampler.captureEntityNbt(null));
    }

    @Test
    void captureEntityNbt_skipsConsoleControlInteraction() {
        ConsoleControlInteractionEntity entity = Mockito.mock(ConsoleControlInteractionEntity.class);
        Mockito.when(entity.isRemoved()).thenReturn(false);
        assertNull(BotiInteriorSampler.captureEntityNbt(entity));
    }

    @Test
    void writeRelativePos_OverwritesPosList() {
        CompoundTag nbt = new CompoundTag();
        BotiInteriorSampler.writeRelativePos(nbt, 1.5f, 2.25f, 3.75f);
        assertTrue(nbt.contains("Pos"));
        var pos = nbt.getListOrEmpty("Pos");
        assertEquals(1.5, pos.getDoubleOr(0, 0), 0.0001);
        assertEquals(2.25, pos.getDoubleOr(1, 0), 0.0001);
        assertEquals(3.75, pos.getDoubleOr(2, 0), 0.0001);
    }

    @Test
    void writeRelativeAttachment_RewritesTileToPlotLocal() {
        CompoundTag nbt = new CompoundTag();
        BlockPos origin = new BlockPos(1114432, 64, 151168);
        nbt.putInt("TileX", 1114438);
        nbt.putInt("TileY", 66);
        nbt.putInt("TileZ", 151171);
        BotiInteriorSampler.writeRelativeAttachment(nbt, origin);
        assertEquals(6, nbt.getIntOr("TileX", 0));
        assertEquals(2, nbt.getIntOr("TileY", 0));
        assertEquals(3, nbt.getIntOr("TileZ", 0));
    }

    @Test
    void writeRelativeAttachment_NoTileIsNoOp() {
        CompoundTag nbt = new CompoundTag();
        BotiInteriorSampler.writeRelativePos(nbt, 1f, 1f, 1f);
        BotiInteriorSampler.writeRelativeAttachment(nbt, new BlockPos(10, 20, 30));
        assertFalse(nbt.contains("TileX"));
    }

    @Test
    void playerEntityType_IsNotSaveable_RequiresSpecialCase() {
        // Documents why captureEntityNbt special-cases players.
        assertFalse(EntityTypes.PLAYER.canSerialize());
        assertEquals("minecraft:player", EntityType.getKey(EntityTypes.PLAYER).toString());
    }
}
