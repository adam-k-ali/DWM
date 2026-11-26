package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.boti.BotiInteriorSampler;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

/**
 * Block and entity work for moving interiors inside {@code dwm:tardis}: capture a box into an
 * {@link InteriorSnapshot}, clear a box, and paste a snapshot somewhere else. Boxes are horizontal
 * ({@code [minX, maxX] × [minZ, maxZ]}, inclusive, whole world height) and are clipped per block, so the chunk
 * columns they share with neighbouring plots are never touched outside the box.
 *
 * <p>Blocks are set without neighbour or shape updates, drops, block-entity side effects or {@code onPlace}
 * ({@link #QUIET}), so moving a room never pops items, breaks attached blocks or fires redstone.
 */
public final class InteriorBlockOps {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Clients + known shape + no drops + no block-entity side effects + no onPlace; no neighbour updates. */
    static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_ALL_SIDEEFFECTS;

    private InteriorBlockOps() {
    }

    /** A horizontal box at full height, inclusive. */
    public record Box(int minX, int maxX, int minZ, int maxZ) {
        public boolean contains(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }

        public Box moved(Vec3i delta) {
            return new Box(minX + delta.getX(), maxX + delta.getX(), minZ + delta.getZ(), maxZ + delta.getZ());
        }

        int minChunkX() {
            return minX >> 4;
        }

        int maxChunkX() {
            return maxX >> 4;
        }

        int minChunkZ() {
            return minZ >> 4;
        }

        int maxChunkZ() {
            return maxZ >> 4;
        }

        public AABB aabb(ServerLevel level) {
            return new AABB(minX, level.getMinY() - 64, minZ, maxX + 1, level.getMaxY() + 65, maxZ + 1);
        }
    }

    /** The legacy 64-block window of the legacy plot anchored at {@code legacyAnchor}. */
    public static Box legacyWindowBox(BlockPos legacyAnchor) {
        BlockPos window = TardisPlotAllocator.legacyWindowOrigin(legacyAnchor);
        int last = TardisPlotAllocator.LEGACY_PLOT_SPACING - 1;
        return new Box(window.getX(), window.getX() + last, window.getZ(), window.getZ() + last);
    }

    /** Loads every chunk column of {@code box} to FULL and waits until their entities are loaded too. */
    public static void loadChunks(ServerLevel level, Box box) {
        for (int cx = box.minChunkX(); cx <= box.maxChunkX(); cx++) {
            for (int cz = box.minChunkZ(); cz <= box.maxChunkZ(); cz++) {
                loadChunk(level, new ChunkPos(cx, cz));
            }
        }
    }

    /** Loads one chunk to FULL and waits for its entity section. */
    public static LevelChunk loadChunk(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = level.getChunk(pos.x(), pos.z());
        level.waitForEntities(pos, 0);
        return chunk;
    }

    /** Captures every non-air block (with block entity) and every non-player root entity in {@code box}. */
    public static InteriorSnapshot capture(ServerLevel level, BlockPos legacyAnchor, Box box) {
        loadChunks(level, box);
        List<InteriorSnapshot.BlockEntry> blocks = new ArrayList<>();
        List<CompoundTag> entities = new ArrayList<>();
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
            forEachBlock(level, box, (pos, state) -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                CompoundTag nbt = null;
                if (blockEntity != null) {
                    TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
                    blockEntity.saveWithId(output);
                    nbt = output.buildResult();
                }
                blocks.add(new InteriorSnapshot.BlockEntry(pos.immutable(), state, nbt));
            });
            for (Entity entity : rootEntitiesIn(level, box)) {
                TagValueOutput output = TagValueOutput.createWithContext(
                        reporter.forChild(entity.problemPath()), level.registryAccess());
                if (entity.save(output)) {
                    entities.add(output.buildResult().copy());
                }
            }
        }
        return new InteriorSnapshot(legacyAnchor, blocks, entities);
    }

    /** What a clear removed. */
    public record Cleared(int blocks, int entities) {
        public Cleared plus(Cleared other) {
            return new Cleared(blocks + other.blocks, entities + other.entities);
        }

        public boolean isEmpty() {
            return blocks == 0 && entities == 0;
        }
    }

    /** Clears every non-player entity and every block in {@code box}, without drops or updates. */
    public static Cleared clear(ServerLevel level, Box box) {
        loadChunks(level, box);
        return clearLoaded(level, box);
    }

    /** {@link #clear} for one chunk column only (the part of it inside {@code box}). */
    public static Cleared clearChunk(ServerLevel level, Box box, ChunkPos chunk) {
        loadChunk(level, chunk);
        Box clipped = new Box(
                Math.max(box.minX(), chunk.getMinBlockX()), Math.min(box.maxX(), chunk.getMaxBlockX()),
                Math.max(box.minZ(), chunk.getMinBlockZ()), Math.min(box.maxZ(), chunk.getMaxBlockZ()));
        return clearLoaded(level, clipped);
    }

    private static Cleared clearLoaded(ServerLevel level, Box box) {
        int entityCount = 0;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, box.aabb(level),
                entity -> !(entity instanceof Player) && box.contains(entity.blockPosition()))) {
            if (!entity.isRemoved()) {
                entity.discard();
                entityCount++;
            }
        }
        List<BlockPos> toClear = new ArrayList<>();
        forEachBlock(level, box, (pos, state) -> toClear.add(pos.immutable()));
        BlockState air = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : toClear) {
            // Empty containers first, so nothing they hold can drop.
            if (level.getBlockEntity(pos) instanceof Clearable clearable) {
                clearable.clearContent();
            }
            level.setBlock(pos, air, QUIET);
        }
        // Anything the cleared blocks dropped despite the flags (none expected) goes too.
        for (Entity entity : level.getEntitiesOfClass(Entity.class, box.aabb(level),
                entity -> !(entity instanceof Player) && box.contains(entity.blockPosition()))) {
            entity.discard();
            entityCount++;
        }
        return new Cleared(toClear.size(), entityCount);
    }

    /** What a paste placed. */
    public record Pasted(int blocks, int blockEntities, int entities, int entitiesRefused) {
    }

    /**
     * Pastes {@code snapshot} moved by {@code delta}. Entities already in the target box are discarded first, so a
     * repeated paste never duplicates them. Positions inside {@code sourceBox} stored in block-entity and entity NBT
     * move with the interior ({@link LegacyPositionRewriter}); entity UUIDs are kept.
     */
    public static Pasted paste(ServerLevel level, InteriorSnapshot snapshot, Box sourceBox, Vec3i delta) {
        Box target = sourceBox.moved(delta);
        loadChunks(level, target);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, target.aabb(level),
                entity -> !(entity instanceof Player) && target.contains(entity.blockPosition()))) {
            entity.discard();
        }
        Predicate<BlockPos> inside = sourceBox::contains;
        LegacyPositionRewriter rewriter = new LegacyPositionRewriter(inside, delta);
        int blockEntities = 0;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
            BlockState barrier = Blocks.BARRIER.defaultBlockState();
            for (InteriorSnapshot.BlockEntry entry : snapshot.blocks()) {
                BlockPos pos = entry.pos().offset(delta);
                if (entry.blockEntity() != null) {
                    // Reset any block entity already there (as StructureTemplate does) so the copy loads fresh.
                    level.setBlock(pos, barrier, QUIET);
                }
                level.setBlock(pos, entry.state(), QUIET);
            }
            for (InteriorSnapshot.BlockEntry entry : snapshot.blocks()) {
                if (entry.blockEntity() == null) {
                    continue;
                }
                BlockPos pos = entry.pos().offset(delta);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null) {
                    LOGGER.warn("TARDIS interior migration: no block entity for {} at {}", entry.state(), pos);
                    continue;
                }
                CompoundTag nbt = rewriter.rewrite(entry.blockEntity());
                blockEntity.loadWithComponents(TagValueInput.create(
                        reporter.forChild(blockEntity.problemPath()), level.registryAccess(), nbt));
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, entry.state(), entry.state(), Block.UPDATE_CLIENTS);
                blockEntities++;
            }
        }
        int entities = 0;
        int refused = 0;
        for (CompoundTag tag : snapshot.entities()) {
            CompoundTag moved = rewriter.rewrite(tag);
            Entity entity = EntityType.loadEntityRecursive(
                    moved, level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true), loaded -> loaded);
            if (entity == null) {
                LOGGER.warn("TARDIS interior migration: could not load entity {}", tag.getStringOr("id", "?"));
                refused++;
                continue;
            }
            if (level.tryAddFreshEntityWithPassengers(entity)) {
                entities++;
            } else {
                LOGGER.warn("TARDIS interior migration: entity {} ({}) already exists; not pasted",
                        entity.getUUID(), tag.getStringOr("id", "?"));
                refused++;
            }
        }
        BotiInteriorSampler.enableChunkLighting(level,
                new int[]{target.minChunkX(), target.maxChunkX(), target.minChunkZ(), target.maxChunkZ()});
        return new Pasted(snapshot.blocks().size(), blockEntities, entities, refused);
    }

    /** Root (non-passenger) non-player entities whose block position is in {@code box}. */
    public static List<Entity> rootEntitiesIn(ServerLevel level, Box box) {
        return level.getEntitiesOfClass(Entity.class, box.aabb(level),
                entity -> !(entity instanceof Player) && !entity.isPassenger() && box.contains(entity.blockPosition()));
    }

    public interface BlockVisitor {
        void visit(BlockPos.MutableBlockPos pos, BlockState state);
    }

    /** Visits every non-air block in {@code box} (chunks must be loaded), skipping all-air sections. */
    public static void forEachBlock(ServerLevel level, Box box, BlockVisitor visitor) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = box.minChunkX(); cx <= box.maxChunkX(); cx++) {
            for (int cz = box.minChunkZ(); cz <= box.maxChunkZ(); cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                LevelChunkSection[] sections = chunk.getSections();
                int minX = Math.max(box.minX(), cx << 4);
                int maxX = Math.min(box.maxX(), (cx << 4) + 15);
                int minZ = Math.max(box.minZ(), cz << 4);
                int maxZ = Math.min(box.maxZ(), (cz << 4) + 15);
                for (int index = 0; index < sections.length; index++) {
                    LevelChunkSection section = sections[index];
                    if (section == null || section.hasOnlyAir()) {
                        continue;
                    }
                    int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                    for (int y = 0; y < 16; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            for (int x = minX; x <= maxX; x++) {
                                BlockState state = section.getBlockState(x & 15, y, z & 15);
                                if (!state.isAir()) {
                                    pos.set(x, baseY + y, z);
                                    visitor.visit(pos, state);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
