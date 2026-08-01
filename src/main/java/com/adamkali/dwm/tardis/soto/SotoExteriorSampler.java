package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Samples an 11×7×11 exterior footprint centered on the TARDIS block for SOTO sync.
 * Relative coords: min corner = {@code exteriorPos + (-5, -1, -5)}; TARDIS at (5, 1, 5).
 */
public final class SotoExteriorSampler {
    public static final int SIZE_X = 11;
    public static final int SIZE_Y = 7;
    public static final int SIZE_Z = 11;

    /** Relative position of the exterior TARDIS block within the footprint. */
    public static final BlockPos RELATIVE_TARDIS_POS = new BlockPos(5, 1, 5);

    /** Offset from exterior block pos to footprint min corner. */
    public static final BlockPos FOOTPRINT_MIN_OFFSET = new BlockPos(-5, -1, -5);

    private static final ChunkTicketType<ChunkPos> SOTO_TICKET =
            ChunkTicketType.create("dwm_soto", Comparator.comparingLong(ChunkPos::toLong), 80);

    private SotoExteriorSampler() {
    }

    public static BlockPos footprintOrigin(BlockPos exteriorPos) {
        return exteriorPos.add(FOOTPRINT_MIN_OFFSET);
    }

    /**
     * Whether a block should appear in the interior SOTO preview.
     * The exterior TARDIS block is excluded (drawn as a synthetic chameleon shell).
     */
    public static boolean isSotoVisible(BlockState state) {
        return state != null
                && !state.isAir()
                && !state.isOf(Blocks.LIGHT)
                && !state.isOf(DWMBlocks.TARDIS_BLOCK);
    }

    public static Map<BlockPos, BlockState> filterVisible(Map<BlockPos, BlockState> placements) {
        Map<BlockPos, BlockState> visible = new HashMap<>();
        for (Map.Entry<BlockPos, BlockState> entry : placements.entrySet()) {
            if (isSotoVisible(entry.getValue())) {
                visible.put(entry.getKey(), entry.getValue());
            }
        }
        return visible;
    }

    public static Map<BlockPos, BlockState> sample(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        BlockPos origin = footprintOrigin(exteriorPos);
        Map<BlockPos, BlockState> visible = new HashMap<>();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = exteriorWorld.getBlockState(mutable);
                    if (isSotoVisible(state)) {
                        visible.put(new BlockPos(x, y, z), state);
                    }
                }
            }
        }
        return visible;
    }

    public static Map<BlockPos, NbtCompound> sampleBlockEntities(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        BlockPos origin = footprintOrigin(exteriorPos);
        RegistryWrapper.WrapperLookup registries = exteriorWorld.getRegistryManager();
        Map<BlockPos, NbtCompound> entities = new HashMap<>();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = exteriorWorld.getBlockState(mutable);
                    if (!isSotoVisible(state)) {
                        continue;
                    }
                    BlockEntity blockEntity = exteriorWorld.getBlockEntity(mutable);
                    if (blockEntity == null) {
                        continue;
                    }
                    entities.put(new BlockPos(x, y, z), BotiInteriorSampler.captureSyncNbt(blockEntity, registries));
                }
            }
        }
        return entities;
    }

    public static Box footprintBox(BlockPos footprintOrigin) {
        return new Box(
                footprintOrigin.getX(),
                footprintOrigin.getY(),
                footprintOrigin.getZ(),
                footprintOrigin.getX() + SIZE_X,
                footprintOrigin.getY() + SIZE_Y,
                footprintOrigin.getZ() + SIZE_Z
        );
    }

    public static int[] footprintChunkBounds(BlockPos footprintOrigin) {
        return new int[]{
                ChunkSectionPos.getSectionCoord(footprintOrigin.getX()),
                ChunkSectionPos.getSectionCoord(footprintOrigin.getX() + SIZE_X - 1),
                ChunkSectionPos.getSectionCoord(footprintOrigin.getZ()),
                ChunkSectionPos.getSectionCoord(footprintOrigin.getZ() + SIZE_Z - 1)
        };
    }

    public static void ensureFootprintChunksLoaded(ServerWorld world, BlockPos footprintOrigin) {
        if (world == null || footprintOrigin == null) {
            return;
        }
        int[] bounds = footprintChunkBounds(footprintOrigin);
        var chunkManager = world.getChunkManager();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                chunkManager.addTicket(SOTO_TICKET, chunkPos, 2, chunkPos);
                world.getChunk(cx, cz);
            }
        }
    }

    public static boolean hasEntities(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        BlockPos origin = footprintOrigin(exteriorPos);
        ensureFootprintChunksLoaded(exteriorWorld, origin);
        return !exteriorWorld.getOtherEntities(null, footprintBox(origin), entity -> !entity.isRemoved()).isEmpty();
    }

    public static void keepMobAiActive(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        if (exteriorWorld == null || exteriorPos == null) {
            return;
        }
        if (!exteriorWorld.getPlayers().isEmpty()) {
            return;
        }
        BlockPos origin = footprintOrigin(exteriorPos);
        ensureFootprintChunksLoaded(exteriorWorld, origin);
        for (Entity entity : exteriorWorld.getOtherEntities(null, footprintBox(origin), e -> !e.isRemoved())) {
            if (entity instanceof MobEntity mob && mob.getDespawnCounter() != 0) {
                mob.setDespawnCounter(0);
            }
        }
    }

    public static List<BotiEntitySample> sampleEntities(ServerWorld exteriorWorld, BlockPos exteriorPos) {
        BlockPos origin = footprintOrigin(exteriorPos);
        ensureFootprintChunksLoaded(exteriorWorld, origin);
        List<Entity> found = exteriorWorld.getOtherEntities(null, footprintBox(origin), entity -> !entity.isRemoved());
        if (found.isEmpty()) {
            return List.of();
        }
        List<BotiEntitySample> samples = new ArrayList<>(found.size());
        for (Entity entity : found) {
            BotiEntitySample sample = captureEntity(entity, origin);
            if (sample != null) {
                samples.add(sample);
            }
        }
        return List.copyOf(samples);
    }

    public static BotiEntitySample captureEntity(Entity entity, BlockPos footprintOrigin) {
        return BotiInteriorSampler.captureEntity(entity, footprintOrigin);
    }

    public static boolean isInsideFootprint(BlockPos worldPos, BlockPos footprintOrigin) {
        int localX = worldPos.getX() - footprintOrigin.getX();
        int localY = worldPos.getY() - footprintOrigin.getY();
        int localZ = worldPos.getZ() - footprintOrigin.getZ();
        return localX >= 0 && localX < SIZE_X
                && localY >= 0 && localY < SIZE_Y
                && localZ >= 0 && localZ < SIZE_Z;
    }
}
