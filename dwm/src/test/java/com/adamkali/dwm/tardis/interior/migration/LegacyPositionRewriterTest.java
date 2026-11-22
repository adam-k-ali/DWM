package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Absolute positions in copied NBT move with the interior, and only those pointing into the copied window. */
class LegacyPositionRewriterTest {
    private static final BlockPos LEGACY = TardisPlotAllocator.legacyPlotOrigin(new UUID(10, 20));
    private static final InteriorBlockOps.Box BOX = InteriorBlockOps.legacyWindowBox(LEGACY);
    private static final Vec3i DELTA = new Vec3i(-30_640, 0, 18_720);
    private static final LegacyPositionRewriter REWRITER = new LegacyPositionRewriter(BOX::contains, DELTA);

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void blockPositionsInsideTheWindowMoveAndOthersStay() {
        BlockPos inside = LEGACY.offset(-27, 3, 36);
        BlockPos outside = LEGACY.offset(-28, 3, 0);
        CompoundTag tag = new CompoundTag();
        tag.put("ConsolePos", ints(inside));
        tag.put("ChairPos", ints(outside));
        tag.put("block_pos", ints(LEGACY.offset(5, 1, 5)));
        CompoundTag moved = REWRITER.rewrite(tag);
        assertArrayEquals(array(inside.offset(DELTA)), intArray(moved, "ConsolePos"));
        assertArrayEquals(array(outside), intArray(moved, "ChairPos"));
        assertArrayEquals(array(LEGACY.offset(5, 1, 5).offset(DELTA)), intArray(moved, "block_pos"));
        // The input is never modified.
        assertArrayEquals(array(inside), intArray(tag, "ConsolePos"));
    }

    @Test
    void nestedListsAndCompoundsAreRewritten() {
        BlockPos post = LEGACY.offset(1, 0, 1);
        ListTag workSpots = new ListTag();
        workSpots.add(ints(LEGACY.offset(2, 0, 2)));
        workSpots.add(ints(new BlockPos(9_999_999, 64, 0)));
        CompoundTag brain = new CompoundTag();
        CompoundTag memories = new CompoundTag();
        CompoundTag home = new CompoundTag();
        home.put("value", globalPos("dwm:tardis", post));
        memories.put("minecraft:home", home);
        brain.put("memories", memories);
        CompoundTag tag = new CompoundTag();
        tag.put("WorkSpots", workSpots);
        tag.put("Brain", brain);

        CompoundTag moved = REWRITER.rewrite(tag);
        ListTag movedSpots = moved.getListOrEmpty("WorkSpots");
        assertArrayEquals(array(LEGACY.offset(2, 0, 2).offset(DELTA)), ((IntArrayTag) movedSpots.get(0)).getAsIntArray());
        assertArrayEquals(array(new BlockPos(9_999_999, 64, 0)), ((IntArrayTag) movedSpots.get(1)).getAsIntArray());
        CompoundTag movedHome = moved.getCompoundOrEmpty("Brain").getCompoundOrEmpty("memories")
                .getCompoundOrEmpty("minecraft:home").getCompoundOrEmpty("value");
        assertArrayEquals(array(post.offset(DELTA)), intArray(movedHome, "pos"));
    }

    @Test
    void globalPositionsInOtherDimensionsStay() {
        BlockPos pos = LEGACY.offset(3, 0, 3);
        CompoundTag tag = new CompoundTag();
        tag.put("lodestone", globalPos("minecraft:overworld", pos));
        CompoundTag moved = REWRITER.rewrite(tag);
        assertArrayEquals(array(pos), intArray(moved.getCompoundOrEmpty("lodestone"), "pos"));
        assertEquals("minecraft:overworld", moved.getCompoundOrEmpty("lodestone").getStringOr("dimension", ""));
    }

    @Test
    void entityPositionsMoveWhenInsideTheWindow() {
        CompoundTag entity = new CompoundTag();
        entity.put("Pos", vec(LEGACY.getX() + 3.25, 65.5, LEGACY.getZ() - 26.75));
        CompoundTag passenger = new CompoundTag();
        passenger.put("Pos", vec(LEGACY.getX() + 3.25, 66.0, LEGACY.getZ() - 26.75));
        ListTag passengers = new ListTag();
        passengers.add(passenger);
        entity.put("Passengers", passengers);
        entity.put("Motion", vec(0.1, -0.08, 0.0));

        CompoundTag moved = REWRITER.rewrite(entity);
        ListTag pos = moved.getListOrEmpty("Pos");
        assertEquals(LEGACY.getX() + 3.25 + DELTA.getX(), ((DoubleTag) pos.get(0)).value(), 1e-9);
        assertEquals(65.5, ((DoubleTag) pos.get(1)).value(), 1e-9);
        assertEquals(LEGACY.getZ() - 26.75 + DELTA.getZ(), ((DoubleTag) pos.get(2)).value(), 1e-9);
        ListTag passengerPos = moved.getListOrEmpty("Passengers").getCompoundOrEmpty(0).getListOrEmpty("Pos");
        assertEquals(LEGACY.getX() + 3.25 + DELTA.getX(), ((DoubleTag) passengerPos.get(0)).value(), 1e-9);
        // Motion is not a position.
        assertEquals(0.1, ((DoubleTag) moved.getListOrEmpty("Motion").get(0)).value(), 1e-9);

        CompoundTag far = new CompoundTag();
        far.put("Pos", vec(LEGACY.getX() + 100.0, 65.0, LEGACY.getZ()));
        assertEquals(LEGACY.getX() + 100.0, ((DoubleTag) REWRITER.rewrite(far).getListOrEmpty("Pos").get(0)).value(), 1e-9);
    }

    @Test
    void otherIntArraysAreCopiedUnchanged() {
        CompoundTag tag = new CompoundTag();
        tag.put("UUID", new IntArrayTag(new int[]{1, 2, 3, 4}));
        tag.put("colors", new IntArrayTag(new int[]{0xFF0000, 0x00FF00}));
        tag.put("name", StringTag.valueOf("Library"));
        CompoundTag moved = REWRITER.rewrite(tag);
        assertArrayEquals(new int[]{1, 2, 3, 4}, intArray(moved, "UUID"));
        assertArrayEquals(new int[]{0xFF0000, 0x00FF00}, intArray(moved, "colors"));
        assertEquals("Library", moved.getStringOr("name", ""));
        assertEquals(LEGACY.offset(DELTA), REWRITER.rewrite(LEGACY));
        assertEquals(new BlockPos(0, 64, -500), REWRITER.rewrite(new BlockPos(0, 64, -500)));
    }

    private static CompoundTag globalPos(String dimension, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.put("pos", ints(pos));
        return tag;
    }

    private static IntArrayTag ints(BlockPos pos) {
        return new IntArrayTag(array(pos));
    }

    private static int[] array(BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    private static int[] intArray(CompoundTag tag, String key) {
        return ((IntArrayTag) tag.get(key)).getAsIntArray();
    }

    private static ListTag vec(double x, double y, double z) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(x));
        list.add(DoubleTag.valueOf(y));
        list.add(DoubleTag.valueOf(z));
        return list;
    }
}
