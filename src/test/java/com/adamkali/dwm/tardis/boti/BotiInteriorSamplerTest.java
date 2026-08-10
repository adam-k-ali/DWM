package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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
    void captureEntity_NullEntityReturnsNull() {
        assertNull(BotiInteriorSampler.captureEntity(null, BlockPos.ZERO));
    }

    @Test
    void writeRelativePos_OverwritesPosList() {
        CompoundTag nbt = new CompoundTag();
        BotiInteriorSampler.writeRelativePos(nbt, 1.5f, 2.25f, 3.75f);
        assertTrue(nbt.contains("Pos", Tag.TAG_LIST));
        assertEquals(1.5, nbt.getList("Pos", Tag.TAG_DOUBLE).getDouble(0), 0.0001);
        assertEquals(2.25, nbt.getList("Pos", Tag.TAG_DOUBLE).getDouble(1), 0.0001);
        assertEquals(3.75, nbt.getList("Pos", Tag.TAG_DOUBLE).getDouble(2), 0.0001);
    }

    @Test
    void writeRelativeAttachment_RewritesTileToPlotLocal() {
        CompoundTag nbt = new CompoundTag();
        BlockPos origin = new BlockPos(1114432, 64, 151168);
        nbt.putInt("TileX", 1114438);
        nbt.putInt("TileY", 66);
        nbt.putInt("TileZ", 151171);
        BotiInteriorSampler.writeRelativeAttachment(nbt, origin);
        assertEquals(6, nbt.getInt("TileX"));
        assertEquals(2, nbt.getInt("TileY"));
        assertEquals(3, nbt.getInt("TileZ"));
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
