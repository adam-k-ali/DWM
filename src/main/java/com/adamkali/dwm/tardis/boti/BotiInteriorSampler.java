package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Samples the First Doctor console-room footprint for BOTI sync.
 * Relative coords match {@link FirstDoctorConsoleRoomLayout} / exterior alignment.
 */
public final class BotiInteriorSampler {
    public static final int SIZE_X = FirstDoctorConsoleRoomLayout.SIZE_X;
    public static final int SIZE_Y = FirstDoctorConsoleRoomLayout.SIZE_Y;
    public static final int SIZE_Z = FirstDoctorConsoleRoomLayout.SIZE_Z;

    private BotiInteriorSampler() {
    }

    /** Whether a block should appear in the exterior BOTI preview. */
    public static boolean isBotiVisible(BlockState state) {
        return state != null
                && !state.isAir()
                && !state.isOf(Blocks.LIGHT)
                && !state.isOf(DWMBlocks.TARDIS_INTERIOR_DOOR);
    }

    /**
     * Filters an arbitrary placement map the same way live sampling does.
     */
    public static Map<BlockPos, BlockState> filterVisible(Map<BlockPos, BlockState> placements) {
        Map<BlockPos, BlockState> visible = new HashMap<>();
        for (Map.Entry<BlockPos, BlockState> entry : placements.entrySet()) {
            if (isBotiVisible(entry.getValue())) {
                visible.put(entry.getKey(), entry.getValue());
            }
        }
        return visible;
    }

    /**
     * Samples the live plot for {@code tardisId} into relative structure coordinates.
     */
    public static Map<BlockPos, BlockState> sample(ServerWorld interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        Map<BlockPos, BlockState> visible = new HashMap<>();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = interiorWorld.getBlockState(mutable);
                    if (isBotiVisible(state)) {
                        visible.put(new BlockPos(x, y, z), state);
                    }
                }
            }
        }
        return visible;
    }

    /**
     * Samples chunk-sync NBT for block entities in the footprint (interior doors excluded).
     * Each compound includes the BE type {@code id} for client reconstruction.
     */
    public static Map<BlockPos, NbtCompound> sampleBlockEntities(ServerWorld interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        RegistryWrapper.WrapperLookup registries = interiorWorld.getRegistryManager();
        Map<BlockPos, NbtCompound> entities = new HashMap<>();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = interiorWorld.getBlockState(mutable);
                    if (!isBotiVisible(state)) {
                        continue;
                    }
                    BlockEntity blockEntity = interiorWorld.getBlockEntity(mutable);
                    if (blockEntity == null) {
                        continue;
                    }
                    entities.put(new BlockPos(x, y, z), captureSyncNbt(blockEntity, registries));
                }
            }
        }
        return entities;
    }

    /**
     * Chunk-sync NBT plus type {@code id} for client {@link BlockEntity#createFromNbt} reconstruction.
     */
    public static NbtCompound captureSyncNbt(BlockEntity blockEntity, RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = blockEntity.toInitialChunkDataNbt(registries);
        BlockEntity.writeIdToNbt(nbt, blockEntity.getType());
        return nbt;
    }

    public static boolean isInsideFootprint(BlockPos worldPos, BlockPos plotOrigin) {
        int localX = worldPos.getX() - plotOrigin.getX();
        int localY = worldPos.getY() - plotOrigin.getY();
        int localZ = worldPos.getZ() - plotOrigin.getZ();
        return localX >= 0 && localX < SIZE_X
                && localY >= 0 && localY < SIZE_Y
                && localZ >= 0 && localZ < SIZE_Z;
    }
}
