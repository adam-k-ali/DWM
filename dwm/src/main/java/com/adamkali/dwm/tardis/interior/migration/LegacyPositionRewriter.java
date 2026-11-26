package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Moves the absolute positions stored in copied block-entity and entity NBT along with the interior they point into.
 *
 * <p>Minecraft and DWM both save block positions as {@code BlockPos.CODEC}, an int array of length 3 (painting and
 * item frame {@code block_pos}, leash knots, bee hive flower positions, villager memories, DWM's console-control
 * {@code ConsolePos}, chair {@code ChairPos}, Dalek posts). Every such triple that lies <em>inside the copied legacy
 * window</em> is shifted by the migration delta; positions elsewhere are left alone. A {@code GlobalPos}
 * ({@code {dimension, pos}}) is only shifted when its dimension is {@code dwm:tardis}. An entity's {@code Pos}
 * (three doubles) is shifted when it lies in the window. Everything else is copied unchanged.
 */
public final class LegacyPositionRewriter {
    private static final String GLOBAL_DIMENSION = "dimension";
    private static final String GLOBAL_POS = "pos";
    private static final String ENTITY_POS = "Pos";

    private final Predicate<BlockPos> inside;
    private final Vec3i delta;

    public LegacyPositionRewriter(Predicate<BlockPos> inside, Vec3i delta) {
        this.inside = inside;
        this.delta = delta;
    }

    /** A rewritten copy of {@code tag}; the input is never modified. */
    public CompoundTag rewrite(CompoundTag tag) {
        return (CompoundTag) rewriteTag(tag);
    }

    /** {@code pos} shifted by the delta when it is inside the window, else unchanged. */
    public BlockPos rewrite(BlockPos pos) {
        return inside.test(pos) ? pos.offset(delta) : pos;
    }

    private Tag rewriteTag(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            return rewriteCompound(compound);
        }
        if (tag instanceof ListTag list) {
            ListTag copy = new ListTag();
            for (Tag element : list) {
                copy.add(rewriteTag(element));
            }
            return copy;
        }
        if (tag instanceof IntArrayTag array) {
            return rewriteIntArray(array);
        }
        return tag.copy();
    }

    private CompoundTag rewriteCompound(CompoundTag compound) {
        boolean otherDimension = isGlobalPosInOtherDimension(compound);
        CompoundTag copy = new CompoundTag();
        for (String key : compound.keySet()) {
            Tag value = compound.get(key);
            if (value == null) {
                continue;
            }
            if (otherDimension && GLOBAL_POS.equals(key)) {
                copy.put(key, value.copy());
            } else if (ENTITY_POS.equals(key) && value instanceof ListTag list && isVec3(list)) {
                copy.put(key, rewriteVec3(list));
            } else {
                copy.put(key, rewriteTag(value));
            }
        }
        return copy;
    }

    private static boolean isGlobalPosInOtherDimension(CompoundTag compound) {
        if (!(compound.get(GLOBAL_POS) instanceof IntArrayTag)
                || !(compound.get(GLOBAL_DIMENSION) instanceof StringTag dimension)) {
            return false;
        }
        return !TardisDimensions.DIMENSION_ID.toString().equals(dimension.value());
    }

    private Tag rewriteIntArray(IntArrayTag array) {
        int[] values = array.getAsIntArray();
        if (values.length != 3) {
            return array.copy();
        }
        BlockPos pos = new BlockPos(values[0], values[1], values[2]);
        BlockPos moved = rewrite(pos);
        return new IntArrayTag(new int[]{moved.getX(), moved.getY(), moved.getZ()});
    }

    private static boolean isVec3(ListTag list) {
        return list.size() == 3 && list.get(0) instanceof NumericTag && list.get(1) instanceof NumericTag
                && list.get(2) instanceof NumericTag;
    }

    private Tag rewriteVec3(ListTag list) {
        double x = ((NumericTag) list.get(0)).doubleValue();
        double y = ((NumericTag) list.get(1)).doubleValue();
        double z = ((NumericTag) list.get(2)).doubleValue();
        if (!inside.test(BlockPos.containing(x, y, z))) {
            return list.copy();
        }
        ListTag moved = new ListTag();
        moved.add(DoubleTag.valueOf(x + delta.getX()));
        moved.add(DoubleTag.valueOf(y + delta.getY()));
        moved.add(DoubleTag.valueOf(z + delta.getZ()));
        return moved;
    }
}
