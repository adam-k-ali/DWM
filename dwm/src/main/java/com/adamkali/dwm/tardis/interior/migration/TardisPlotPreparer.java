package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

/**
 * Empties a plot window before its console room is first placed. A fresh 10000-block window can overlap older
 * content in {@code dwm:tardis}: legacy 64-block interiors of TARDISes whose data is gone, or anything built outside a
 * legacy window. Loading all ~390k chunk columns of a window is out of the question, so only columns that actually
 * exist as fully generated chunks (loaded now, or saved in a region file) are loaded and cleared; in a fresh world
 * that is none.
 */
public final class TardisPlotPreparer {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Chunks per region file edge. */
    static final int REGION_CHUNKS = 32;

    private TardisPlotPreparer() {
    }

    /** The window of the plot anchored at {@code anchor} as a box. */
    public static InteriorBlockOps.Box windowBox(BlockPos anchor) {
        BlockPos window = TardisPlotAllocator.windowOrigin(anchor);
        int last = TardisPlotAllocator.PLOT_SPACING - 1;
        return new InteriorBlockOps.Box(window.getX(), window.getX() + last, window.getZ(), window.getZ() + last);
    }

    /** Inclusive region-file coordinates {@code [minX, maxX, minZ, maxZ]} that overlap {@code box}. */
    public static int[] regionBounds(InteriorBlockOps.Box box) {
        return new int[]{
                Math.floorDiv(box.minX() >> 4, REGION_CHUNKS), Math.floorDiv(box.maxX() >> 4, REGION_CHUNKS),
                Math.floorDiv(box.minZ() >> 4, REGION_CHUNKS), Math.floorDiv(box.maxZ() >> 4, REGION_CHUNKS)
        };
    }

    /** Chunk columns of region {@code (regionX, regionZ)} that overlap {@code box}. */
    public static List<ChunkPos> chunksInRegion(InteriorBlockOps.Box box, int regionX, int regionZ) {
        int minCx = Math.max(box.minX() >> 4, regionX * REGION_CHUNKS);
        int maxCx = Math.min(box.maxX() >> 4, regionX * REGION_CHUNKS + REGION_CHUNKS - 1);
        int minCz = Math.max(box.minZ() >> 4, regionZ * REGION_CHUNKS);
        int maxCz = Math.min(box.maxZ() >> 4, regionZ * REGION_CHUNKS + REGION_CHUNKS - 1);
        List<ChunkPos> chunks = new ArrayList<>();
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        return chunks;
    }

    /**
     * Clears every non-player entity and every block in the window of the plot anchored at {@code anchor}, in the
     * chunk columns that exist. Returns what was cleared.
     */
    public static InteriorBlockOps.Cleared prepare(ServerLevel level, BlockPos anchor) {
        InteriorBlockOps.Box box = windowBox(anchor);
        List<ChunkPos> existing = existingChunks(level, box);
        InteriorBlockOps.Cleared cleared = new InteriorBlockOps.Cleared(0, 0);
        for (ChunkPos chunk : existing) {
            cleared = cleared.plus(InteriorBlockOps.clearChunk(level, box, chunk));
        }
        if (!cleared.isEmpty()) {
            LOGGER.info("Cleared {} leftover blocks and {} entities from the TARDIS plot at {} ({} chunks scanned)",
                    cleared.blocks(), cleared.entities(), anchor, existing.size());
        }
        return cleared;
    }

    /** Chunk columns overlapping {@code box} that are loaded or saved; never loads or creates anything. */
    static List<ChunkPos> existingChunks(ServerLevel level, InteriorBlockOps.Box box) {
        List<ChunkPos> existing = new ArrayList<>();
        // Finish pending chunk writes so every saved chunk is in a region file that exists.
        level.getChunkSource().chunkMap.synchronize(true).join();
        Path regionFolder = DimensionType.getStorageFolder(
                level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT)).resolve("region");
        int[] regions = regionBounds(box);
        for (int rx = regions[0]; rx <= regions[1]; rx++) {
            for (int rz = regions[2]; rz <= regions[3]; rz++) {
                boolean regionExists = Files.isRegularFile(regionFolder.resolve("r." + rx + "." + rz + ".mca"));
                for (ChunkPos chunk : chunksInRegion(box, rx, rz)) {
                    if (level.getChunkSource().getChunkNow(chunk.x(), chunk.z()) != null
                            || (regionExists && isSaved(level, chunk))) {
                        existing.add(chunk);
                    }
                }
            }
        }
        return existing;
    }

    /**
     * Whether {@code chunk} is saved as a fully generated chunk. Chunks that are only partly generated (the ring
     * around every fully loaded chunk) were never playable, so they cannot hold anything built; loading them to clear
     * them would only generate more.
     */
    private static boolean isSaved(ServerLevel level, ChunkPos chunk) {
        CollectFields fields = new CollectFields(new FieldSelector(StringTag.TYPE, "Status"));
        level.getChunkSource().chunkScanner().scanChunk(chunk, fields).join();
        return fields.getResult() instanceof CompoundTag tag && isFull(tag.getStringOr("Status", ""));
    }

    /** Whether a saved chunk {@code Status} is the fully generated one. */
    static boolean isFull(String status) {
        Identifier id = Identifier.tryParse(status);
        return id != null && id.equals(BuiltInRegistries.CHUNK_STATUS.getKey(ChunkStatus.FULL));
    }
}
