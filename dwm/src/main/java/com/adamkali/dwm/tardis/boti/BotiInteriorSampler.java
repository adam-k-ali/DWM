package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomPlacer;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalSampler;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Samples the TARDIS interior plot for BOTI portal streaming.
 */
public final class BotiInteriorSampler extends PortalSampler {
    static final BotiInteriorSampler INSTANCE = new BotiInteriorSampler();

    private BotiInteriorSampler() {
    }

    /**
     * Whether a block should appear in the exterior BOTI preview.
     * Interior doors stay excluded (no dedicated interior-door BER in BOTI yet).
     * The First Doctor console is included so its BER can draw via synced BE NBT.
     */
    public static boolean isBotiVisible(BlockState state) {
        return INSTANCE.isVisible(state);
    }

    @Override
    public boolean isVisible(BlockState state) {
        return state != null
                && !state.isAir()
                && !state.is(Blocks.LIGHT)
                && !state.is(DWMBlocks.TARDIS_INTERIOR_DOOR);
    }

    /**
     * Filters an arbitrary placement map the same way live sampling does.
     */
    public static Map<BlockPos, BlockState> filterVisible(Map<BlockPos, BlockState> placements) {
        return INSTANCE.filterVisibleBlocks(placements);
    }

    /**
     * Chunk-sync NBT plus type {@code id} for client {@link BlockEntity#loadStatic} reconstruction.
     */
    public static CompoundTag captureSyncNbt(BlockEntity blockEntity, HolderLookup.Provider registries) {
        return PortalSampler.captureSyncNbt(blockEntity, registries);
    }

    /**
     * Inclusive chunk-grid bounds of the window owned by the plot anchored at {@code plotOrigin}
     * ({@link TardisPlotAllocator#windowOrigin}). The window is not chunk-aligned, so the edge columns are
     * shared with the neighbouring windows; sampling clips them per block ({@link #includePos}).
     */
    public static int[] windowChunkBounds(BlockPos plotOrigin) {
        BlockPos window = TardisPlotAllocator.windowOrigin(plotOrigin);
        int minCX = SectionPos.blockToSectionCoord(window.getX());
        int maxCX = SectionPos.blockToSectionCoord(window.getX() + TardisPlotAllocator.PLOT_SPACING - 1);
        int minCZ = SectionPos.blockToSectionCoord(window.getZ());
        int maxCZ = SectionPos.blockToSectionCoord(window.getZ() + TardisPlotAllocator.PLOT_SPACING - 1);
        return new int[]{minCX, maxCX, minCZ, maxCZ};
    }

    /**
     * View-distance Chebyshev box around the anchor {@code plotOrigin}, clipped to the owning window.
     */
    public static int[] streamChunkBounds(BlockPos plotOrigin, int radiusChunks) {
        int[] plot = windowChunkBounds(plotOrigin);
        return PortalSampler.clipChunkBounds(
                PortalSampler.streamChunkBounds(plotOrigin, radiusChunks),
                plot[0],
                plot[1],
                plot[2],
                plot[3]
        );
    }

    public static int[] streamChunkBounds(ServerLevel world, BlockPos plotOrigin) {
        return streamChunkBounds(plotOrigin, PortalSampler.streamRadiusChunks(world));
    }

    /**
     * Caps a view-distance radius so fog/stream stay inside {@link TardisPlotAllocator#PLOT_SPACING}. The cap (3)
     * still covers the whole window, which spans anchor chunk {@code -2..+2}.
     */
    public static int clipStreamRadiusChunks(int radiusChunks) {
        int plotRadius = Math.max(0, (TardisPlotAllocator.PLOT_SPACING - 1) / 16);
        return Math.min(Math.max(0, radiusChunks), plotRadius);
    }

    /** Whether {@code worldPos} is in the window of the plot anchored at {@code plotOrigin} and in stream range. */
    public static boolean isInsideWindowStream(BlockPos worldPos, BlockPos plotOrigin, int radiusChunks) {
        if (!TardisPlotAllocator.isInsideWindow(worldPos, plotOrigin)) {
            return false;
        }
        return PortalSampler.isInsideStreamRadius(
                worldPos, plotOrigin, radiusChunks, PortalSampler.streamYRadiusBlocks(radiusChunks));
    }

    /**
     * Ticket-only keep-alive for the portal stream radius. Does not call {@code getChunk}.
     */
    public static void addStreamTickets(ServerLevel world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return;
        }
        INSTANCE.addStreamTickets(world, plotOrigin, clipStreamRadiusChunks(PortalSampler.streamRadiusChunks(world)));
    }

    /** True once the stamped room's known light source has propagated into vanilla light data. */
    public static boolean isFootprintLightReady(ServerLevel world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return false;
        }
        BlockPos sourcePos = plotOrigin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));
        BlockState source = world.getBlockState(sourcePos);
        int emission = source.getLightEmission();
        return source.is(Blocks.LIGHT)
                && emission > 0
                && world.getBrightness(LightLayer.BLOCK, sourcePos) >= emission;
    }

    /**
     * Enables vanilla light storage and queues source propagation for a newly stamped room.
     * Empty flat-world chunks can reach FULL without lighting ever being enabled, so ordinary
     * block-change checks alone have nowhere to retain their result.
     */
    public static void enableFootprintLighting(ServerLevel world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return;
        }
        enableChunkLighting(world, FirstDoctorConsoleRoomPlacer.roomChunkBounds(plotOrigin));
        BlockPos sourcePos = plotOrigin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));
        world.getLightEngine().checkBlock(sourcePos);
    }

    /**
     * Enables vanilla light storage and queues source propagation for every chunk in the inclusive bounds
     * {@code [minChunkX, maxChunkX, minChunkZ, maxChunkZ]}, for rooms stamped into previously empty chunks
     * (see {@link #enableFootprintLighting}).
     */
    public static void enableChunkLighting(ServerLevel world, int[] chunkBounds) {
        if (world == null || chunkBounds == null) {
            return;
        }
        var lightEngine = world.getLightEngine();
        for (int cx = chunkBounds[0]; cx <= chunkBounds[1]; cx++) {
            for (int cz = chunkBounds[2]; cz <= chunkBounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                lightEngine.setLightEnabled(chunkPos, true);
                lightEngine.propagateLightSources(chunkPos);
            }
        }
    }

    /**
     * Resets vanilla mob {@code despawnCounter} so wander AI keeps running while no player is in
     * {@code dwm:tardis}. Without a nearby player the counter climbs past 100 and
     * {@code WanderAroundGoal} refuses to start — BOTI then shows frozen livestock.
     */
    public static void keepMobAiActive(ServerLevel interiorWorld, UUID tardisId) {
        if (interiorWorld == null || tardisId == null) {
            return;
        }
        INSTANCE.resetMobAi(interiorWorld, TardisPlotAllocator.plotOrigin(tardisId));
    }

    /**
     * Entity NBT with type {@code id}. Players get profile tags for client {@code OtherClientPlayerEntity}.
     */
    public static CompoundTag captureEntityNbt(Entity entity) {
        if (entity == null || entity.isRemoved() || entity instanceof ConsoleControlInteractionEntity) {
            return null;
        }
        if (entity instanceof Player player) {
            TagValueOutput output = TagValueOutput.createWithContext(
                    ProblemReporter.DISCARDING, entity.registryAccess());
            output.putString("id", EntityType.getKey(EntityTypes.PLAYER).toString());
            GameProfile profile = player.getGameProfile();
            output.store(BotiEntitySample.BOTI_PROFILE_ID, UUIDUtil.CODEC, profile.id());
            output.putString(BotiEntitySample.BOTI_PROFILE_NAME, profile.name() == null ? "" : profile.name());
            player.saveWithoutId(output);
            return output.buildResult();
        }
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, entity.registryAccess());
        if (!entity.saveAsPassenger(output)) {
            return null;
        }
        return output.buildResult();
    }

    static void writeRelativePos(CompoundTag nbt, float relX, float relY, float relZ) {
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(relX));
        pos.add(DoubleTag.valueOf(relY));
        pos.add(DoubleTag.valueOf(relZ));
        nbt.put("Pos", pos);
    }

    /**
     * Rewrites decoration {@code TileX/Y/Z} into plot-relative coords so client reconstruction
     * does not trip {@code BlockAttachedEntity}'s absolute-vs-Pos distance check.
     */
    static void writeRelativeAttachment(CompoundTag nbt, BlockPos plotOrigin) {
        if (nbt == null || plotOrigin == null || !nbt.contains("TileX")) {
            return;
        }
        nbt.putInt("TileX", nbt.getIntOr("TileX", 0) - plotOrigin.getX());
        nbt.putInt("TileY", nbt.getIntOr("TileY", 0) - plotOrigin.getY());
        nbt.putInt("TileZ", nbt.getIntOr("TileZ", 0) - plotOrigin.getZ());
    }

    /** Samples interior sky/fog atmosphere at the plot origin. */
    public static PortalAtmosphere sampleAtmosphere(ServerLevel interiorWorld, BlockPos plotOrigin) {
        return PortalSampler.sampleAtmosphere(interiorWorld, plotOrigin);
    }

    /**
     * Collects visible block states (+ BE NBT) for one chunk column clipped to the plot's window.
     * Positions in the returned maps are world-absolute.
     *
     * @return the sample, or {@code null} when the column is not yet {@code FULL}
     */
    public static @Nullable PortalStreamSample sampleStreamChunk(
            ServerLevel interiorWorld,
            UUID tardisId,
            int chunkX,
            int chunkZ
    ) {
        if (interiorWorld == null || tardisId == null) {
            return new PortalStreamSample(chunkX, chunkZ, Map.of(), Map.of(), PortalLightData.EMPTY);
        }
        return INSTANCE.sampleChunkColumn(interiorWorld, TardisPlotAllocator.plotOrigin(tardisId), chunkX, chunkZ);
    }

    public static List<Entity> collectStreamEntities(ServerLevel interiorWorld, UUID tardisId) {
        if (interiorWorld == null || tardisId == null) {
            return List.of();
        }
        return INSTANCE.collectEntities(interiorWorld, TardisPlotAllocator.plotOrigin(tardisId));
    }

    @Override
    protected void ensureLoaded(ServerLevel world, BlockPos anchor) {
        addStreamTickets(world, anchor);
    }

    @Override
    protected AABB entityBox(ServerLevel world, BlockPos anchor) {
        int radius = PortalSampler.simulationRadiusChunks(world);
        int yRadius = PortalSampler.streamYRadiusBlocks(radius);
        AABB stream = PortalSampler.streamBox(anchor, radius, yRadius);
        BlockPos window = TardisPlotAllocator.windowOrigin(anchor);
        AABB plot = new AABB(
                window.getX(),
                stream.minY,
                window.getZ(),
                window.getX() + TardisPlotAllocator.PLOT_SPACING,
                stream.maxY,
                window.getZ() + TardisPlotAllocator.PLOT_SPACING
        );
        return stream.intersect(plot);
    }

    @Override
    protected boolean includePos(BlockPos worldPos, BlockPos anchor) {
        return TardisPlotAllocator.isInsideWindow(worldPos, anchor);
    }

    @Override
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
        if (blocks.isEmpty()) {
            return PortalLightData.EMPTY;
        }
        YRange lightY = lightYRange(yRange, lowestVisibleY, highestVisibleY);
        if (lightY.min() > lightY.max()) {
            return PortalLightData.EMPTY;
        }
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        BlockPos window = TardisPlotAllocator.windowOrigin(anchor);
        int minX = Math.max(baseX, window.getX());
        int maxX = Math.min(baseX + 15, window.getX() + TardisPlotAllocator.PLOT_SPACING - 1);
        int minZ = Math.max(baseZ, window.getZ());
        int maxZ = Math.min(baseZ + 15, window.getZ() + TardisPlotAllocator.PLOT_SPACING - 1);
        return PortalLightData.sample(
                world.getLightEngine(),
                new BlockPos(minX, lightY.min(), minZ),
                new BlockPos(maxX, lightY.max(), maxZ)
        );
    }
}
