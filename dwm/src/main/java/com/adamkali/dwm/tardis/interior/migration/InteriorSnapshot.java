package com.adamkali.dwm.tardis.interior.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Everything in one legacy interior window, captured before migration: every non-air block (with its block entity's
 * full NBT) and every non-player root entity (with its passengers, UUIDs kept). Positions are the original legacy
 * world positions; {@link LegacyPositionRewriter} moves them when the snapshot is pasted.
 *
 * <p>Written to disk ({@link #toTag}) before anything is cleared, so a crash mid-migration can resume.
 */
public record InteriorSnapshot(BlockPos legacyAnchor, List<BlockEntry> blocks, List<CompoundTag> entities) {
    private static final int FORMAT = 1;

    /** One captured block: its legacy position, state and block-entity NBT ({@code saveWithId}), if any. */
    public record BlockEntry(BlockPos pos, BlockState state, @Nullable CompoundTag blockEntity) {
    }

    public InteriorSnapshot {
        blocks = List.copyOf(blocks);
        entities = List.copyOf(entities);
    }

    public boolean isEmpty() {
        return blocks.isEmpty() && entities.isEmpty();
    }

    public int blockEntityCount() {
        int count = 0;
        for (BlockEntry entry : blocks) {
            if (entry.blockEntity() != null) {
                count++;
            }
        }
        return count;
    }

    /** NBT form: a block-state palette plus {@code [x, y, z, paletteIndex]} quads, block entities and entities. */
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("format", FORMAT);
        tag.put("legacy_anchor", intArray(legacyAnchor));
        Map<BlockState, Integer> indices = new HashMap<>();
        ListTag palette = new ListTag();
        int[] packed = new int[blocks.size() * 4];
        ListTag blockEntities = new ListTag();
        for (int i = 0; i < blocks.size(); i++) {
            BlockEntry entry = blocks.get(i);
            Integer index = indices.get(entry.state());
            if (index == null) {
                index = palette.size();
                indices.put(entry.state(), index);
                palette.add(NbtUtils.writeBlockState(entry.state()));
            }
            packed[i * 4] = entry.pos().getX();
            packed[i * 4 + 1] = entry.pos().getY();
            packed[i * 4 + 2] = entry.pos().getZ();
            packed[i * 4 + 3] = index;
            if (entry.blockEntity() != null) {
                CompoundTag blockEntity = new CompoundTag();
                blockEntity.putInt("index", i);
                blockEntity.put("nbt", entry.blockEntity().copy());
                blockEntities.add(blockEntity);
            }
        }
        tag.put("palette", palette);
        tag.putIntArray("blocks", packed);
        tag.put("block_entities", blockEntities);
        ListTag entityList = new ListTag();
        for (CompoundTag entity : entities) {
            entityList.add(entity.copy());
        }
        tag.put("entities", entityList);
        return tag;
    }

    /** Reads {@link #toTag()} output. */
    public static InteriorSnapshot fromTag(CompoundTag tag, HolderGetter<Block> blockLookup) {
        int format = tag.getIntOr("format", 0);
        if (format != FORMAT) {
            throw new IllegalArgumentException("Unknown interior snapshot format " + format);
        }
        BlockPos anchor = readPos(tag.get("legacy_anchor"));
        List<BlockState> palette = new ArrayList<>();
        for (Tag entry : tag.getListOrEmpty("palette")) {
            palette.add(NbtUtils.readBlockState(blockLookup, (CompoundTag) entry));
        }
        Map<Integer, CompoundTag> blockEntities = new HashMap<>();
        for (Tag entry : tag.getListOrEmpty("block_entities")) {
            CompoundTag compound = (CompoundTag) entry;
            blockEntities.put(compound.getIntOr("index", -1), compound.getCompoundOrEmpty("nbt").copy());
        }
        int[] packed = tag.getIntArray("blocks").orElse(new int[0]);
        List<BlockEntry> blocks = new ArrayList<>(packed.length / 4);
        for (int i = 0; i * 4 + 3 < packed.length; i++) {
            BlockPos pos = new BlockPos(packed[i * 4], packed[i * 4 + 1], packed[i * 4 + 2]);
            blocks.add(new BlockEntry(pos, palette.get(packed[i * 4 + 3]), blockEntities.get(i)));
        }
        List<CompoundTag> entities = new ArrayList<>();
        for (Tag entry : tag.getListOrEmpty("entities")) {
            entities.add(((CompoundTag) entry).copy());
        }
        return new InteriorSnapshot(anchor, blocks, entities);
    }

    private static IntArrayTag intArray(BlockPos pos) {
        return new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    private static BlockPos readPos(@Nullable Tag tag) {
        if (!(tag instanceof IntArrayTag array) || array.getAsIntArray().length != 3) {
            throw new IllegalArgumentException("Interior snapshot has no legacy anchor");
        }
        int[] values = array.getAsIntArray();
        return new BlockPos(values[0], values[1], values[2]);
    }
}
