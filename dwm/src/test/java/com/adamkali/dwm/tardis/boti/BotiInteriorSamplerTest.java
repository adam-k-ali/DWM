package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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
    void isInsideFootprint_respectsRoomBounds() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        BlockPos origin = TardisPlotAllocator.plotOrigin(id);

        assertTrue(BotiInteriorSampler.isInsideFootprint(origin, origin));
        assertTrue(BotiInteriorSampler.isInsideFootprint(
                origin.offset(BotiInteriorSampler.SIZE_X - 1, BotiInteriorSampler.SIZE_Y - 1, BotiInteriorSampler.SIZE_Z - 1),
                origin));
        assertFalse(BotiInteriorSampler.isInsideFootprint(origin.offset(BotiInteriorSampler.SIZE_X, 0, 0), origin));
        assertFalse(BotiInteriorSampler.isInsideFootprint(origin.offset(0, -1, 0), origin));
    }

    @Test
    void isBotiVisible_excludesInteriorDoorIncludesConsoleAndChest() {
        assertFalse(BotiInteriorSampler.isBotiVisible(DWMBlocks.TARDIS_INTERIOR_DOOR.defaultBlockState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()));
        assertTrue(BotiInteriorSampler.isBotiVisible(Blocks.CHEST.defaultBlockState()));
        assertFalse(BotiInteriorSampler.isBotiVisible(Blocks.LIGHT.defaultBlockState()));
    }

    @Test
    void footprintBox_matchesRoomDimensions() {
        BlockPos origin = new BlockPos(64, 64, 128);
        AABB box = BotiInteriorSampler.footprintBox(origin);
        assertEquals(origin.getX(), box.minX);
        assertEquals(origin.getY(), box.minY);
        assertEquals(origin.getZ(), box.minZ);
        assertEquals(origin.getX() + BotiInteriorSampler.SIZE_X, box.maxX);
        assertEquals(origin.getY() + BotiInteriorSampler.SIZE_Y, box.maxY);
        assertEquals(origin.getZ() + BotiInteriorSampler.SIZE_Z, box.maxZ);
    }

    @Test
    void footprintChunkBounds_coversFullFootprint() {
        BlockPos origin = new BlockPos(15, 64, 15); // straddles chunk boundary for 11-wide room
        int[] bounds = BotiInteriorSampler.footprintChunkBounds(origin);
        assertEquals(0, bounds[0]);
        assertEquals(1, bounds[1]);
        assertEquals(0, bounds[2]);
        assertEquals(1, bounds[3]);
    }

    @Test
    void streamChunkBounds_clipsViewDistanceToPlot() {
        BlockPos origin = new BlockPos(0, 64, 0);
        int[] plot = BotiInteriorSampler.plotChunkBounds(origin);
        int[] clipped = BotiInteriorSampler.streamChunkBounds(origin, 12);
        assertEquals(plot[0], clipped[0]);
        assertEquals(plot[1], clipped[1]);
        assertEquals(plot[2], clipped[2]);
        assertEquals(plot[3], clipped[3]);
        assertTrue(clipped[1] - clipped[0] < 12);
    }

    @Test
    void clipStreamRadiusChunks_capsToPlot() {
        assertEquals(0, BotiInteriorSampler.clipStreamRadiusChunks(0));
        assertEquals(2, BotiInteriorSampler.clipStreamRadiusChunks(2));
        assertTrue(BotiInteriorSampler.clipStreamRadiusChunks(12) <= 3);
        assertEquals(
                BotiInteriorSampler.clipStreamRadiusChunks(3),
                BotiInteriorSampler.clipStreamRadiusChunks(32)
        );
    }

    @Test
    void isInsidePlotStream_rejectsNeighborPlot() {
        BlockPos origin = new BlockPos(0, 64, 0);
        assertTrue(BotiInteriorSampler.isInsidePlotStream(origin, origin, 2));
        assertFalse(BotiInteriorSampler.isInsidePlotStream(
                origin.offset(TardisPlotAllocator.PLOT_SPACING, 0, 0), origin, 12));
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
    void captureEntity_NullEntityReturnsNull() {
        assertNull(BotiInteriorSampler.captureEntity(null, BlockPos.ZERO));
    }

    @Test
    void captureEntity_skipsConsoleControlInteraction() {
        ConsoleControlInteractionEntity entity = Mockito.mock(ConsoleControlInteractionEntity.class);
        Mockito.when(entity.isRemoved()).thenReturn(false);
        assertNull(BotiInteriorSampler.captureEntity(entity, BlockPos.ZERO));
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
