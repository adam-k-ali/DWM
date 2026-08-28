package com.adamkali.dwm.tardis.portal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Shared sampling helpers for BOTI (look-in) and SOTO (look-out) portal streams.
 * Subclasses supply visibility, load strategy, and light-volume bounds.
 *
 * <p>Streaming matches vanilla {@code PlayerChunkSender}: tickets request async load, sampling
 * reads only {@code FULL} chunks via {@code getChunkNow}, and never force-generates on the
 * server thread.
 */
public abstract class PortalSampler {
    /** Fallback when server/player view distance is unavailable (matches former Phase-1 cap). */
    public static final int DEFAULT_STREAM_RADIUS_CHUNKS = 2;
    /** Fog starts at this fraction of the streamed block radius. */
    public static final float FOG_START_FRACTION = 0.6f;

    static final long STREAM_TICKET_TIMEOUT = 80L;
    static final TicketType STREAM_LOADING_TICKET =
            new TicketType(STREAM_TICKET_TIMEOUT, TicketType.FLAG_LOADING);
    static final TicketType STREAM_SIMULATION_TICKET = new TicketType(
            STREAM_TICKET_TIMEOUT,
            TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE
    );
    private static final int COLUMN_STRIDE_Y = 256;
    private static final ThreadLocal<BlockState[]> COLUMN_GRID =
            ThreadLocal.withInitial(() -> new BlockState[384 * COLUMN_STRIDE_Y]);

    protected PortalSampler() {
    }

    /**
     * Server view distance in chunks, or {@link #DEFAULT_STREAM_RADIUS_CHUNKS} when unavailable.
     */
    public static int streamRadiusChunks(ServerLevel world) {
        if (world == null) {
            return DEFAULT_STREAM_RADIUS_CHUNKS;
        }
        MinecraftServer server = world.getServer();
        if (server == null) {
            return DEFAULT_STREAM_RADIUS_CHUNKS;
        }
        int view = server.getPlayerList().getViewDistance();
        return view > 0 ? view : DEFAULT_STREAM_RADIUS_CHUNKS;
    }

    /**
     * Per-viewer stream radius: {@code min(server view distance, player requested view distance)}.
     */
    public static int streamRadiusChunks(ServerPlayer player) {
        if (player == null) {
            return DEFAULT_STREAM_RADIUS_CHUNKS;
        }
        ServerLevel world = player.level() instanceof ServerLevel serverLevel ? serverLevel : null;
        int serverView = streamRadiusChunks(world);
        int requested = player.requestedViewDistance();
        if (requested <= 0) {
            return serverView;
        }
        return Math.min(serverView, requested);
    }

    /**
     * Simulation radius in chunks: {@code min(stream radius, server simulation distance)}.
     * Matches vanilla loading-at-view / ticking-at-simulation split.
     */
    public static int simulationRadiusChunks(ServerLevel world) {
        int stream = streamRadiusChunks(world);
        if (world == null) {
            return stream;
        }
        MinecraftServer server = world.getServer();
        if (server == null) {
            return stream;
        }
        int simulation = server.getPlayerList().getSimulationDistance();
        if (simulation <= 0) {
            return stream;
        }
        return Math.min(stream, simulation);
    }

    /** Vertical half-extent in blocks matching a Chebyshev chunk radius. */
    public static int streamYRadiusBlocks(int radiusChunks) {
        return Math.max(0, radiusChunks) * 16;
    }

    /**
     * Inclusive chunk-grid bounds covering a Chebyshev radius around {@code anchor}:
     * {@code [minChunkX, maxChunkX, minChunkZ, maxChunkZ]}.
     */
    public static int[] streamChunkBounds(BlockPos anchor, int radiusChunks) {
        if (anchor == null) {
            return new int[]{0, 0, 0, 0};
        }
        int cx = SectionPos.blockToSectionCoord(anchor.getX());
        int cz = SectionPos.blockToSectionCoord(anchor.getZ());
        int radius = Math.max(0, radiusChunks);
        return new int[]{cx - radius, cx + radius, cz - radius, cz + radius};
    }

    /** Inclusive intersection of two chunk-grid rectangles. */
    public static int[] clipChunkBounds(int[] bounds, int minCX, int maxCX, int minCZ, int maxCZ) {
        if (bounds == null || bounds.length < 4) {
            return new int[]{minCX, maxCX, minCZ, maxCZ};
        }
        return new int[]{
                Math.max(bounds[0], minCX),
                Math.min(bounds[1], maxCX),
                Math.max(bounds[2], minCZ),
                Math.min(bounds[3], maxCZ)
        };
    }

    /** Axis-aligned box covering the Chebyshev chunk radius plus a vertical half-extent. */
    public static AABB streamBox(BlockPos anchor, int radiusChunks, int yRadius) {
        int half = Math.max(0, radiusChunks) * 16;
        int y = Math.max(0, yRadius);
        return new AABB(
                anchor.getX() - half,
                anchor.getY() - y,
                anchor.getZ() - half,
                anchor.getX() + half + 1,
                anchor.getY() + y + 1,
                anchor.getZ() + half + 1
        );
    }

    public static boolean isInsideStreamRadius(
            BlockPos worldPos,
            BlockPos anchor,
            int radiusChunks,
            int yRadius
    ) {
        if (worldPos == null || anchor == null) {
            return false;
        }
        int cx = SectionPos.blockToSectionCoord(worldPos.getX());
        int cz = SectionPos.blockToSectionCoord(worldPos.getZ());
        int anchorCx = SectionPos.blockToSectionCoord(anchor.getX());
        int anchorCz = SectionPos.blockToSectionCoord(anchor.getZ());
        int radius = Math.max(0, radiusChunks);
        if (Math.abs(cx - anchorCx) > radius || Math.abs(cz - anchorCz) > radius) {
            return false;
        }
        return Math.abs(worldPos.getY() - anchor.getY()) <= Math.max(0, yRadius);
    }

    /** Fog start distance in blocks for a streamed Chebyshev radius. */
    public static float fogStartBlocks(int radiusChunks) {
        return streamYRadiusBlocks(radiusChunks) * FOG_START_FRACTION;
    }

    /** Fog end distance in blocks (the streamed Chebyshev radius). Always greater than start. */
    public static float fogEndBlocks(int radiusChunks) {
        float start = fogStartBlocks(radiusChunks);
        float end = streamYRadiusBlocks(radiusChunks);
        return end > start ? end : start + 1.0f;
    }

    /** Ticket pos + radius for a stream centered on {@code anchor} (one ticket, not per-column). */
    public static ChunkPos streamTicketChunk(BlockPos anchor) {
        return new ChunkPos(anchor.getX() >> 4, anchor.getZ() >> 4);
    }

    /** Whether a block should appear in this sampler's portal preview. */
    public abstract boolean isVisible(BlockState state);

    public Map<BlockPos, BlockState> filterVisibleBlocks(Map<BlockPos, BlockState> placements) {
        Map<BlockPos, BlockState> visible = new HashMap<>();
        for (Map.Entry<BlockPos, BlockState> entry : placements.entrySet()) {
            if (isVisible(entry.getValue())) {
                visible.put(entry.getKey(), entry.getValue());
            }
        }
        return visible;
    }

    /**
     * Samples sky/fog atmosphere at {@code samplePos}. Returns {@link PortalAtmosphere#DEFAULT}
     * when the world or position is missing.
     */
    public static PortalAtmosphere sampleAtmosphere(ServerLevel world, BlockPos samplePos) {
        if (world == null || samplePos == null) {
            return PortalAtmosphere.DEFAULT;
        }
        Identifier effectsId = world.dimensionTypeRegistration()
                .unwrapKey()
                .map(ResourceKey::identifier)
                .orElseGet(BuiltinDimensionTypes.OVERWORLD::identifier);
        long timeOfDay = world.getOverworldClockTime();
        float rain = world.getRainLevel(0.0f);
        float thunder = world.getThunderLevel(0.0f);
        var attrs = world.environmentAttributes();
        return new PortalAtmosphere(
                effectsId,
                timeOfDay,
                rain,
                thunder,
                attrs.getValue(EnvironmentAttributes.SKY_COLOR, samplePos),
                attrs.getValue(EnvironmentAttributes.FOG_COLOR, samplePos)
        );
    }

    /**
     * Chunk-sync NBT plus type {@code id} for client {@link BlockEntity#loadStatic} reconstruction.
     */
    public static CompoundTag captureSyncNbt(BlockEntity blockEntity, HolderLookup.Provider registries) {
        CompoundTag nbt = blockEntity.getUpdateTag(registries);
        TagValueOutput typeOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        BlockEntity.addEntityType(typeOut, blockEntity.getType());
        CompoundTag typeTag = typeOut.buildResult();
        for (String key : typeTag.keySet()) {
            nbt.put(key, typeTag.get(key));
        }
        return nbt;
    }

    /**
     * LOADING ticket at {@code radiusChunks} plus a SIMULATION ticket at simulation distance.
     * Does not call {@code getChunk}.
     */
    protected void addStreamTickets(ServerLevel world, BlockPos anchor, int radiusChunks) {
        if (world == null || anchor == null) {
            return;
        }
        ChunkPos ticketChunk = streamTicketChunk(anchor);
        int loadRadius = Math.max(0, radiusChunks);
        world.getChunkSource().addTicketWithRadius(STREAM_LOADING_TICKET, ticketChunk, loadRadius);
        int simRadius = Math.min(loadRadius, simulationRadiusChunks(world));
        world.getChunkSource().addTicketWithRadius(STREAM_SIMULATION_TICKET, ticketChunk, simRadius);
    }

    protected void ensureLoaded(ServerLevel world, BlockPos anchor) {
    }

    protected AABB entityBox(ServerLevel world, BlockPos anchor) {
        int radius = simulationRadiusChunks(world);
        return streamBox(anchor, radius, streamYRadiusBlocks(radius));
    }

    protected boolean includePos(BlockPos worldPos, BlockPos anchor) {
        return true;
    }

    /**
     * When true, only sample each column from its world-surface height down to the lowest
     * neighboring surface (plus a 1-block pad). Soto uses this so buried cave layers are not
     * walked; BOTI keeps the full plot Y range.
     */
    protected boolean useSurfaceSkin() {
        return false;
    }

    protected YRange sampleYRange(ServerLevel world, BlockPos anchor) {
        int yRadius = streamYRadiusBlocks(streamRadiusChunks(world));
        int minY = Math.max(world.getMinY(), anchor.getY() - yRadius);
        int maxY = Math.min(world.getMinY() + world.getHeight() - 1, anchor.getY() + yRadius);
        return yRange(minY, maxY);
    }

    protected static YRange yRange(int min, int max) {
        return new YRange(min, max);
    }

    /** Inclusive Y span covering visible blocks plus a 1-block pad, clipped to {@code yRange}. */
    protected static YRange lightYRange(YRange yRange, int lowestVisibleY, int highestVisibleY) {
        if (yRange == null || lowestVisibleY > highestVisibleY) {
            return yRange(1, 0);
        }
        return yRange(
                Math.max(yRange.min(), lowestVisibleY - 1),
                Math.min(yRange.max(), highestVisibleY + 1)
        );
    }

    /**
     * True when {@code state} is a full solid cube whose six neighbors are also full solid cubes,
     * so the client mesh would emit no faces.
     */
    static boolean isHiddenInterior(
            BlockState state,
            BlockState east,
            BlockState west,
            BlockState up,
            BlockState down,
            BlockState south,
            BlockState north
    ) {
        if (state == null || !state.isSolidRender()) {
            return false;
        }
        return isSolidOccluder(east)
                && isSolidOccluder(west)
                && isSolidOccluder(up)
                && isSolidOccluder(down)
                && isSolidOccluder(south)
                && isSolidOccluder(north);
    }

    private static boolean isSolidOccluder(BlockState neighbor) {
        return neighbor != null && neighbor.isSolidRender();
    }

    /** True when every palette entry is a full solid cube (buried stone, packed dirt, …). */
    static boolean isSolidFilledSection(LevelChunkSection section) {
        return section != null
                && !section.hasOnlyAir()
                && !section.maybeHas(state -> state != null && !state.isSolidRender());
    }

    /**
     * Per-column inclusive Y bounds from a 16×16 WORLD_SURFACE heightmap.
     * Interior columns keep a 1-block pad below the lowest 4-neighbor surface; chunk-edge
     * columns extend 16 blocks down so cliffs on a chunk border still get a face.
     */
    static void computeSurfaceSkinBounds(int[] heightY, int rangeMin, int rangeMax, int[] colMin, int[] colMax) {
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int index = (lz << 4) | lx;
                int own = heightY[index];
                int floor = own;
                if (lx > 0) {
                    floor = Math.min(floor, heightY[index - 1]);
                }
                if (lx < 15) {
                    floor = Math.min(floor, heightY[index + 1]);
                }
                if (lz > 0) {
                    floor = Math.min(floor, heightY[index - 16]);
                }
                if (lz < 15) {
                    floor = Math.min(floor, heightY[index + 16]);
                }
                int minY = floor - 1;
                if (lx == 0 || lx == 15 || lz == 0 || lz == 15) {
                    minY = Math.min(minY, own - 16);
                }
                colMax[index] = Math.min(rangeMax, own);
                colMin[index] = Math.max(rangeMin, minY);
            }
        }
    }

    private static boolean isSolidAtWorldY(
            boolean[] solidSection,
            int minSectionY,
            int maxSectionY,
            int minY,
            int maxY,
            int y
    ) {
        if (y < minY || y > maxY) {
            return false;
        }
        int sectionY = SectionPos.blockToSectionCoord(y);
        return sectionY >= minSectionY
                && sectionY <= maxSectionY
                && solidSection[sectionY - minSectionY];
    }

    private static BlockState occludeOrSolidSection(
            BlockState neighbor,
            boolean[] solidSection,
            int minSectionY,
            int maxSectionY,
            int neighborY,
            int minY,
            int ySize
    ) {
        if (neighbor != null) {
            return neighbor;
        }
        if (isSolidAtWorldY(solidSection, minSectionY, maxSectionY, minY, minY + ySize - 1, neighborY)) {
            return Blocks.STONE.defaultBlockState();
        }
        return null;
    }

    private int emitSolidSectionLayer(
            LevelChunkSection section,
            boolean[] includeColumn,
            Map<BlockPos, BlockState> blocks,
            int baseX,
            int baseZ,
            int sectionMinY,
            int ly,
            int neighborY,
            BlockState[] grid,
            int minY,
            int ySize
    ) {
        int emitted = 0;
        int y = sectionMinY + ly;
        int neighborGy = neighborY - minY;
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                if (!includeColumn[(lz << 4) | lx]) {
                    continue;
                }
                BlockState neighbor = null;
                if (neighborGy >= 0 && neighborGy < ySize) {
                    neighbor = grid[neighborGy * COLUMN_STRIDE_Y + (lz << 4) + lx];
                }
                if (isSolidOccluder(neighbor)) {
                    continue;
                }
                BlockState state = section.getBlockState(lx, ly, lz);
                if (!isVisible(state)) {
                    continue;
                }
                blocks.put(new BlockPos(baseX + lx, y, baseZ + lz), state);
                emitted++;
            }
        }
        return emitted;
    }

    protected PortalLightData sampleLight(
            ServerLevel world,
            BlockPos anchor,
            int chunkX,
            int chunkZ,
            YRange yRange,
            Map<BlockPos, BlockState> blocks,
            int lowestVisibleY,
            int highestVisibleY
    ) {
        return PortalLightData.EMPTY;
    }

    protected boolean hasLoadedEntities(ServerLevel world, BlockPos anchor) {
        if (world == null || anchor == null) {
            return false;
        }
        return !world.getEntities((Entity) null, entityBox(world, anchor), entity -> !entity.isRemoved()).isEmpty();
    }

    protected void resetMobAi(ServerLevel world, BlockPos anchor) {
        if (world == null || anchor == null) {
            return;
        }
        // Players in-dimension already reset the counter via MobEntity#checkDespawn.
        if (!world.players().isEmpty()) {
            return;
        }
        ensureLoaded(world, anchor);
        for (Entity entity : world.getEntities((Entity) null, entityBox(world, anchor), e -> !e.isRemoved())) {
            if (entity instanceof Mob mob && mob.getNoActionTime() != 0) {
                mob.setNoActionTime(0);
            }
        }
    }

    protected List<Entity> collectEntities(ServerLevel world, BlockPos anchor) {
        if (world == null || anchor == null) {
            return List.of();
        }
        ensureLoaded(world, anchor);
        return List.copyOf(world.getEntities((Entity) null, entityBox(world, anchor), entity -> !entity.isRemoved()));
    }

    /**
     * Collects visible block states (+ BE NBT) for one chunk column.
     * Positions in the returned maps are world-absolute.
     *
     * @return the sample, or {@code null} when the column is not yet {@code FULL} (retry later)
     */
    protected @Nullable PortalStreamSample sampleChunkColumn(
            ServerLevel world,
            BlockPos anchor,
            int chunkX,
            int chunkZ
    ) {
        if (world == null || anchor == null) {
            return new PortalStreamSample(chunkX, chunkZ, Map.of(), Map.of(), PortalLightData.EMPTY);
        }
        LevelChunk chunk = world.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            return null;
        }
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
        int lowestVisibleY = Integer.MAX_VALUE;
        int highestVisibleY = Integer.MIN_VALUE;
        HolderLookup.Provider registries = world.registryAccess();
        YRange yRange = sampleYRange(world, anchor);
        if (yRange.min() > yRange.max()) {
            return new PortalStreamSample(chunkX, chunkZ, Map.of(), Map.of(), PortalLightData.EMPTY);
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minSectionY = Math.max(chunk.getMinSectionY(), SectionPos.blockToSectionCoord(yRange.min()));
        int maxSectionY = Math.min(chunk.getMaxSectionY(), SectionPos.blockToSectionCoord(yRange.max()));
        int ySize = yRange.max() - yRange.min() + 1;
        int gridLen = ySize * COLUMN_STRIDE_Y;
        BlockState[] grid = COLUMN_GRID.get();
        if (grid.length < gridLen) {
            grid = new BlockState[gridLen];
            COLUMN_GRID.set(grid);
        }
        boolean[] includeColumn = new boolean[256];
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                mutable.set(baseX + lx, yRange.min(), baseZ + lz);
                includeColumn[(lz << 4) | lx] = includePos(mutable, anchor);
            }
        }
        int[] colMin = new int[256];
        int[] colMax = new int[256];
        if (useSurfaceSkin()) {
            int[] heights = new int[256];
            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    heights[(lz << 4) | lx] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
                }
            }
            computeSurfaceSkinBounds(heights, yRange.min(), yRange.max(), colMin, colMax);
        } else {
            Arrays.fill(colMin, yRange.min());
            Arrays.fill(colMax, yRange.max());
        }
        int minSkin = colMin[0];
        int maxSkin = colMax[0];
        for (int i = 1; i < 256; i++) {
            minSkin = Math.min(minSkin, colMin[i]);
            maxSkin = Math.max(maxSkin, colMax[i]);
        }
        int gridVisible = 0;
        int minGy = ySize;
        int maxGy = -1;
        int sectionCount = maxSectionY - minSectionY + 1;
        boolean[] solidSection = new boolean[sectionCount];
        for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
            int sectionMinY = SectionPos.sectionToBlockCoord(sectionY);
            if (sectionMinY + 15 < minSkin || sectionMinY > maxSkin) {
                continue;
            }
            if (section.hasOnlyAir()) {
                continue;
            }
            if (!section.maybeHas(this::isVisible)) {
                continue;
            }
            if (isSolidFilledSection(section)) {
                solidSection[sectionY - minSectionY] = true;
                continue;
            }
            int localMinY = Math.max(0, yRange.min() - sectionMinY);
            int localMaxY = Math.min(15, yRange.max() - sectionMinY);
            for (int ly = localMinY; ly <= localMaxY; ly++) {
                int y = sectionMinY + ly;
                if (y < minSkin || y > maxSkin) {
                    continue;
                }
                int gy = y - yRange.min();
                int yBase = gy * COLUMN_STRIDE_Y;
                for (int lz = 0; lz < 16; lz++) {
                    int zBase = yBase + (lz << 4);
                    for (int lx = 0; lx < 16; lx++) {
                        int col = (lz << 4) | lx;
                        if (!includeColumn[col] || y < colMin[col] || y > colMax[col]) {
                            continue;
                        }
                        BlockState state = section.getBlockState(lx, ly, lz);
                        if (!isVisible(state)) {
                            continue;
                        }
                        grid[zBase + lx] = state;
                        gridVisible++;
                        minGy = Math.min(minGy, gy);
                        maxGy = Math.max(maxGy, gy);
                    }
                }
            }
        }
        Map<BlockPos, BlockState> blocks = new HashMap<>(Math.max(16, gridVisible / 4 + 32));
        int minY = yRange.min();
        for (int gy = minGy; gy <= maxGy; gy++) {
            int yBase = gy * COLUMN_STRIDE_Y;
            int y = minY + gy;
            for (int lz = 0; lz < 16; lz++) {
                for (int lx = 0; lx < 16; lx++) {
                    int index = yBase + (lz << 4) + lx;
                    BlockState state = grid[index];
                    if (state == null) {
                        continue;
                    }
                    BlockState down = gy > 0 ? grid[index - COLUMN_STRIDE_Y] : null;
                    BlockState up = gy + 1 < ySize ? grid[index + COLUMN_STRIDE_Y] : null;
                    BlockState north = lz > 0 ? grid[index - 16] : null;
                    BlockState south = lz < 15 ? grid[index + 16] : null;
                    BlockState west = lx > 0 ? grid[index - 1] : null;
                    BlockState east = lx < 15 ? grid[index + 1] : null;
                    int col = (lz << 4) | lx;
                    if (down == null && y - 1 < colMin[col]) {
                        down = Blocks.STONE.defaultBlockState();
                    } else {
                        down = occludeOrSolidSection(
                                down, solidSection, minSectionY, maxSectionY, y - 1, minY, ySize);
                    }
                    up = occludeOrSolidSection(
                            up, solidSection, minSectionY, maxSectionY, y + 1, minY, ySize);
                    if (isHiddenInterior(state, east, west, up, down, south, north)) {
                        continue;
                    }
                    blocks.put(new BlockPos(baseX + lx, y, baseZ + lz), state);
                    lowestVisibleY = Math.min(lowestVisibleY, y);
                    highestVisibleY = Math.max(highestVisibleY, y);
                }
            }
        }
        for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            if (!solidSection[sectionY - minSectionY]) {
                continue;
            }
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
            int sectionMinY = SectionPos.sectionToBlockCoord(sectionY);
            int localMinY = Math.max(0, yRange.min() - sectionMinY);
            int localMaxY = Math.min(15, yRange.max() - sectionMinY);
            int topY = sectionMinY + localMaxY;
            int bottomY = sectionMinY + localMinY;
            if (!isSolidAtWorldY(solidSection, minSectionY, maxSectionY, minY, yRange.max(), topY + 1)) {
                int emitted = emitSolidSectionLayer(
                        section, includeColumn, blocks, baseX, baseZ, sectionMinY, localMaxY, topY + 1, grid, minY, ySize);
                if (emitted > 0) {
                    lowestVisibleY = Math.min(lowestVisibleY, topY);
                    highestVisibleY = Math.max(highestVisibleY, topY);
                }
            }
            if (!isSolidAtWorldY(solidSection, minSectionY, maxSectionY, minY, yRange.max(), bottomY - 1)) {
                int emitted = emitSolidSectionLayer(
                        section, includeColumn, blocks, baseX, baseZ, sectionMinY, localMinY, bottomY - 1, grid, minY, ySize);
                if (emitted > 0) {
                    lowestVisibleY = Math.min(lowestVisibleY, bottomY);
                    highestVisibleY = Math.max(highestVisibleY, bottomY);
                }
            }
        }
        Arrays.fill(grid, 0, gridLen, null);
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            BlockPos pos = blockEntity.getBlockPos();
            if (pos.getY() < yRange.min() || pos.getY() > yRange.max()) {
                continue;
            }
            if (!includePos(pos, anchor)) {
                continue;
            }
            BlockState state = blocks.get(pos);
            if (state == null) {
                state = blockEntity.getBlockState();
                if (!isVisible(state)) {
                    continue;
                }
                blocks.put(pos.immutable(), state);
                lowestVisibleY = Math.min(lowestVisibleY, pos.getY());
                highestVisibleY = Math.max(highestVisibleY, pos.getY());
            }
            blockEntities.put(pos.immutable(), captureSyncNbt(blockEntity, registries));
        }
        PortalLightData lightData = sampleLight(
                world, anchor, chunkX, chunkZ, yRange, blocks, lowestVisibleY, highestVisibleY);
        return new PortalStreamSample(chunkX, chunkZ, blocks, blockEntities, lightData);
    }

    protected record YRange(int min, int max) {
    }
}
