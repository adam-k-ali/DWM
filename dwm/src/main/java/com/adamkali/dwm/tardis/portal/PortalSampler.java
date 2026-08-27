package com.adamkali.dwm.tardis.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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

    /** Ticket-only keep-alive for {@code bounds} ({@code [minCX, maxCX, minCZ, maxCZ]}). */
    protected void addTickets(ServerLevel world, int[] bounds) {
        if (world == null || bounds == null) {
            return;
        }
        var chunkManager = world.getChunkSource();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                chunkManager.addTicketWithRadius(ticket, new ChunkPos(cx, cz), 2);
            }
        }
    }

    protected void ensureLoaded(ServerLevel world, BlockPos anchor) {
    }

    protected AABB entityBox(BlockPos anchor) {
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
        return !world.getEntities((Entity) null, entityBox(anchor), entity -> !entity.isRemoved()).isEmpty();
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
        for (Entity entity : world.getEntities((Entity) null, entityBox(anchor), e -> !e.isRemoved())) {
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
        return List.copyOf(world.getEntities((Entity) null, entityBox(anchor), entity -> !entity.isRemoved()));
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
