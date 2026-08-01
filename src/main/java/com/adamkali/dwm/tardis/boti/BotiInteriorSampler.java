package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
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
import java.util.UUID;

/**
 * Samples the First Doctor console-room footprint for BOTI sync.
 * Relative coords match {@link FirstDoctorConsoleRoomLayout} / exterior alignment.
 */
public final class BotiInteriorSampler {
    public static final int SIZE_X = FirstDoctorConsoleRoomLayout.SIZE_X;
    public static final int SIZE_Y = FirstDoctorConsoleRoomLayout.SIZE_Y;
    public static final int SIZE_Z = FirstDoctorConsoleRoomLayout.SIZE_Z;

    /**
     * Keeps footprint chunks entity-accessible briefly after the player leaves the TARDIS
     * dimension so BOTI can still sample live entities for the exterior preview.
     */
    private static final ChunkTicketType<ChunkPos> BOTI_TICKET =
            ChunkTicketType.create("dwm_boti", Comparator.comparingLong(ChunkPos::toLong), 80);

    private BotiInteriorSampler() {
    }

    /**
     * Whether a block should appear in the exterior BOTI preview.
     * Interior doors stay excluded (no dedicated interior-door BER in BOTI yet).
     * The First Doctor console is included so its BER can draw via synced BE NBT.
     */
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
     * Samples chunk-sync NBT for block entities in the footprint
     * ({@link #isBotiVisible} — interior doors excluded).
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

    /** Axis-aligned footprint box in world space for entity queries. */
    public static Box footprintBox(BlockPos plotOrigin) {
        return new Box(
                plotOrigin.getX(),
                plotOrigin.getY(),
                plotOrigin.getZ(),
                plotOrigin.getX() + SIZE_X,
                plotOrigin.getY() + SIZE_Y,
                plotOrigin.getZ() + SIZE_Z
        );
    }

    /**
     * Inclusive chunk-grid bounds covering the footprint:
     * {@code [minChunkX, maxChunkX, minChunkZ, maxChunkZ]}.
     */
    public static int[] footprintChunkBounds(BlockPos plotOrigin) {
        return new int[]{
                ChunkSectionPos.getSectionCoord(plotOrigin.getX()),
                ChunkSectionPos.getSectionCoord(plotOrigin.getX() + SIZE_X - 1),
                ChunkSectionPos.getSectionCoord(plotOrigin.getZ()),
                ChunkSectionPos.getSectionCoord(plotOrigin.getZ() + SIZE_Z - 1)
        };
    }

    /**
     * Ensures footprint chunks are loaded (and briefly ticketed) so entity queries work even when
     * no player is in {@code dwm:tardis}.
     */
    public static void ensureFootprintChunksLoaded(ServerWorld world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return;
        }
        int[] bounds = footprintChunkBounds(plotOrigin);
        var chunkManager = world.getChunkManager();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                chunkManager.addTicket(BOTI_TICKET, chunkPos, 2, chunkPos);
                world.getChunk(cx, cz);
            }
        }
    }

    /** True if any non-removed entity intersects the plot footprint. */
    public static boolean hasEntities(ServerWorld interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        return !interiorWorld.getOtherEntities(null, footprintBox(origin), entity -> !entity.isRemoved()).isEmpty();
    }

    /**
     * Resets vanilla mob {@code despawnCounter} so wander AI keeps running while no player is in
     * {@code dwm:tardis}. Without a nearby player the counter climbs past 100 and
     * {@code WanderAroundGoal} refuses to start — BOTI then shows frozen livestock.
     */
    public static void keepMobAiActive(ServerWorld interiorWorld, UUID tardisId) {
        if (interiorWorld == null || tardisId == null) {
            return;
        }
        // Players in-dimension already reset the counter via MobEntity#checkDespawn.
        if (!interiorWorld.getPlayers().isEmpty()) {
            return;
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        for (Entity entity : interiorWorld.getOtherEntities(null, footprintBox(origin), e -> !e.isRemoved())) {
            if (entity instanceof MobEntity mob && mob.getDespawnCounter() != 0) {
                mob.setDespawnCounter(0);
            }
        }
    }

    /**
     * Samples entities intersecting the footprint into relative structure coordinates.
     * Uses {@link Entity#saveSelfNbt}; players are special-cased ({@code EntityType.PLAYER} is not saveable).
     */
    public static List<BotiEntitySample> sampleEntities(ServerWorld interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        List<Entity> found = interiorWorld.getOtherEntities(null, footprintBox(origin), entity -> !entity.isRemoved());
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

    /**
     * Captures one entity for BOTI sync. Returns null when the entity cannot be serialized.
     */
    public static BotiEntitySample captureEntity(Entity entity, BlockPos plotOrigin) {
        if (entity == null || entity.isRemoved() || plotOrigin == null) {
            return null;
        }
        NbtCompound nbt = captureEntityNbt(entity);
        if (nbt == null) {
            return null;
        }
        float relX = (float) (entity.getX() - plotOrigin.getX());
        float relY = (float) (entity.getY() - plotOrigin.getY());
        float relZ = (float) (entity.getZ() - plotOrigin.getZ());
        writeRelativePos(nbt, relX, relY, relZ);
        writeRelativeAttachment(nbt, plotOrigin);
        return new BotiEntitySample(relX, relY, relZ, entity.getYaw(), entity.getPitch(), nbt);
    }

    /**
     * Entity NBT with type {@code id}. Players get profile tags for client {@code OtherClientPlayerEntity}.
     */
    public static NbtCompound captureEntityNbt(Entity entity) {
        NbtCompound nbt = new NbtCompound();
        if (entity instanceof PlayerEntity player) {
            nbt.putString("id", EntityType.getId(EntityType.PLAYER).toString());
            GameProfile profile = player.getGameProfile();
            nbt.putUuid(BotiEntitySample.BOTI_PROFILE_ID, profile.getId());
            nbt.putString(BotiEntitySample.BOTI_PROFILE_NAME, profile.getName() == null ? "" : profile.getName());
            player.writeNbt(nbt);
            return nbt;
        }
        if (!entity.saveSelfNbt(nbt)) {
            return null;
        }
        return nbt;
    }

    static void writeRelativePos(NbtCompound nbt, float relX, float relY, float relZ) {
        NbtList pos = new NbtList();
        pos.add(NbtDouble.of(relX));
        pos.add(NbtDouble.of(relY));
        pos.add(NbtDouble.of(relZ));
        nbt.put("Pos", pos);
    }

    /**
     * Rewrites decoration {@code TileX/Y/Z} into plot-relative coords so client reconstruction
     * does not trip {@code BlockAttachedEntity}'s absolute-vs-Pos distance check.
     */
    static void writeRelativeAttachment(NbtCompound nbt, BlockPos plotOrigin) {
        if (nbt == null || plotOrigin == null || !nbt.contains("TileX")) {
            return;
        }
        nbt.putInt("TileX", nbt.getInt("TileX") - plotOrigin.getX());
        nbt.putInt("TileY", nbt.getInt("TileY") - plotOrigin.getY());
        nbt.putInt("TileZ", nbt.getInt("TileZ") - plotOrigin.getZ());
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
