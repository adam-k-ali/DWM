package com.adamkali.dwm.tardis.portal;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Shared sampling helpers for BOTI (look-in) and SOTO (look-out) portal streams.
 * Subclasses supply visibility, load strategy, and light-volume bounds.
 */
public abstract class PortalSampler {
    /** Fallback when server/player view distance is unavailable (matches former Phase-1 cap). */
    public static final int DEFAULT_STREAM_RADIUS_CHUNKS = 2;
    /** Fog starts at this fraction of the streamed block radius. */
    public static final float FOG_START_FRACTION = 0.6f;

    protected final int sizeX;
    protected final int sizeY;
    protected final int sizeZ;
    protected final TicketType ticket;

    protected PortalSampler(int sizeX, int sizeY, int sizeZ, TicketType ticket) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.ticket = ticket;
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

    public boolean inFootprint(BlockPos worldPos, BlockPos origin) {
        int localX = worldPos.getX() - origin.getX();
        int localY = worldPos.getY() - origin.getY();
        int localZ = worldPos.getZ() - origin.getZ();
        return localX >= 0 && localX < sizeX
                && localY >= 0 && localY < sizeY
                && localZ >= 0 && localZ < sizeZ;
    }

    /** Axis-aligned footprint box in world space for entity queries. */
    public AABB footprintAabb(BlockPos origin) {
        return new AABB(
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                origin.getX() + sizeX,
                origin.getY() + sizeY,
                origin.getZ() + sizeZ
        );
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

    /** Ticket-only keep-alive for a small footprint ({@code [minCX, maxCX, minCZ, maxCZ]}). */
    protected void addTickets(ServerLevel world, int[] bounds) {
        if (world == null || bounds == null) {
            return;
        }
        var chunkManager = world.getChunkSource();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                chunkManager.addTicketWithRadius(ticket, new ChunkPos(cx, cz), 0);
            }
        }
    }

    /** One ticket at {@code anchor}'s chunk covering the Chebyshev stream radius. */
    protected void addStreamTickets(ServerLevel world, BlockPos anchor, int radiusChunks) {
        if (world == null || anchor == null) {
            return;
        }
        world.getChunkSource().addTicketWithRadius(
                ticket,
                streamTicketChunk(anchor),
                Math.max(0, radiusChunks)
        );
    }

    protected void ensureLoaded(ServerLevel world, BlockPos anchor) {
    }

    protected AABB entityBox(ServerLevel world, BlockPos anchor) {
        return footprintAabb(anchor);
    }

    protected boolean includePos(BlockPos worldPos, BlockPos anchor) {
        return true;
    }

    protected YRange sampleYRange(ServerLevel world, BlockPos anchor) {
        return yRange(anchor.getY(), anchor.getY() + sizeY - 1);
    }

    protected static YRange yRange(int min, int max) {
        return new YRange(min, max);
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
     */
    protected PortalStreamSample sampleChunkColumn(
            ServerLevel world,
            BlockPos anchor,
            int chunkX,
            int chunkZ
    ) {
        if (world == null || anchor == null) {
            return new PortalStreamSample(chunkX, chunkZ, Map.of(), Map.of(), PortalLightData.EMPTY);
        }
        world.getChunk(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
        int lowestVisibleY = Integer.MAX_VALUE;
        int highestVisibleY = Integer.MIN_VALUE;
        HolderLookup.Provider registries = world.registryAccess();
        YRange yRange = sampleYRange(world, anchor);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = yRange.min(); y <= yRange.max(); y++) {
                    mutable.set(baseX + lx, y, baseZ + lz);
                    if (!includePos(mutable, anchor)) {
                        continue;
                    }
                    BlockState state = world.getBlockState(mutable);
                    if (!isVisible(state)) {
                        continue;
                    }
                    BlockPos immutable = mutable.immutable();
                    blocks.put(immutable, state);
                    lowestVisibleY = Math.min(lowestVisibleY, y);
                    highestVisibleY = Math.max(highestVisibleY, y);
                    BlockEntity blockEntity = world.getBlockEntity(mutable);
                    if (blockEntity != null) {
                        blockEntities.put(immutable, captureSyncNbt(blockEntity, registries));
                    }
                }
            }
        }
        PortalLightData lightData = sampleLight(
                world, anchor, chunkX, chunkZ, yRange, blocks, lowestVisibleY, highestVisibleY);
        return new PortalStreamSample(chunkX, chunkZ, blocks, blockEntities, lightData);
    }

    protected record YRange(int min, int max) {
    }
}
