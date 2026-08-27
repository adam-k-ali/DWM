package com.adamkali.dwm.tardis.boti;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalLightData;
import com.adamkali.dwm.tardis.portal.PortalStreamSample;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

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
    private static final TicketType BOTI_TICKET = new TicketType(80, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION);

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
                && !state.is(Blocks.LIGHT)
                && !state.is(DWMBlocks.TARDIS_INTERIOR_DOOR);
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
    public static Map<BlockPos, BlockState> sample(ServerLevel interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        Map<BlockPos, BlockState> visible = new HashMap<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
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
    public static Map<BlockPos, CompoundTag> sampleBlockEntities(ServerLevel interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        HolderLookup.Provider registries = interiorWorld.registryAccess();
        Map<BlockPos, CompoundTag> entities = new HashMap<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
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

    /** Axis-aligned footprint box in world space for entity queries. */
    public static AABB footprintBox(BlockPos plotOrigin) {
        return new AABB(
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
                SectionPos.blockToSectionCoord(plotOrigin.getX()),
                SectionPos.blockToSectionCoord(plotOrigin.getX() + SIZE_X - 1),
                SectionPos.blockToSectionCoord(plotOrigin.getZ()),
                SectionPos.blockToSectionCoord(plotOrigin.getZ() + SIZE_Z - 1)
        };
    }

    /**
     * Ticket-only keep-alive for the console-room footprint. Does not call {@code getChunk}
     * (avoids synchronous force-loads — used by deferred interior preload).
     */
    public static void addFootprintTickets(ServerLevel world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return;
        }
        int[] bounds = footprintChunkBounds(plotOrigin);
        var chunkManager = world.getChunkSource();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                chunkManager.addTicketWithRadius(BOTI_TICKET, new ChunkPos(cx, cz), 2);
            }
        }
    }

    /**
     * True when every footprint column is already present in the chunk cache (no force-load).
     */
    public static boolean areFootprintChunksLoaded(ServerLevel world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return false;
        }
        int[] bounds = footprintChunkBounds(plotOrigin);
        var chunkManager = world.getChunkSource();
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                if (!chunkManager.hasChunk(cx, cz)) {
                    return false;
                }
            }
        }
        return true;
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
        int[] bounds = footprintChunkBounds(plotOrigin);
        var lightEngine = world.getLightEngine();
        int scheduledColumns = 0;
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                ChunkPos chunkPos = new ChunkPos(cx, cz);
                lightEngine.setLightEnabled(chunkPos, true);
                lightEngine.propagateLightSources(chunkPos);
                scheduledColumns++;
            }
        }
        BlockPos sourcePos = plotOrigin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));
        lightEngine.checkBlock(sourcePos);
        // #region agent log
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"),
                    "{\"hypothesisId\":\"G,H\",\"location\":\"BotiInteriorSampler.enableFootprintLighting\",\"message\":\"queued footprint lighting\",\"data\":{\"columns\":" + scheduledColumns + ",\"sourceEmission\":" + world.getBlockState(sourcePos).getLightEmission() + ",\"lightOnInSourceColumn\":" + lightEngine.lightOnInColumn(ChunkPos.pack(sourcePos.getX() >> 4, sourcePos.getZ() >> 4)) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException ignored) {
        }
        // #endregion
    }

    /**
     * Synchronously force-loads footprint chunks (blocking). Prefer {@link #addFootprintTickets}
     * + {@link #areFootprintChunksLoaded} for approach-time preload.
     */
    public static void forceLoadFootprintChunks(ServerLevel world, BlockPos plotOrigin) {
        if (world == null || plotOrigin == null) {
            return;
        }
        addFootprintTickets(world, plotOrigin);
        int[] bounds = footprintChunkBounds(plotOrigin);
        for (int cx = bounds[0]; cx <= bounds[1]; cx++) {
            for (int cz = bounds[2]; cz <= bounds[3]; cz++) {
                world.getChunk(cx, cz);
            }
        }
    }

    /**
     * Ensures footprint chunks are loaded (and briefly ticketed) so entity queries work even when
     * no player is in {@code dwm:tardis}.
     */
    public static void ensureFootprintChunksLoaded(ServerLevel world, BlockPos plotOrigin) {
        forceLoadFootprintChunks(world, plotOrigin);
    }

    /** True if any non-removed entity intersects the plot footprint. */
    public static boolean hasEntities(ServerLevel interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        return !interiorWorld.getEntities((Entity) null, footprintBox(origin), entity -> !entity.isRemoved()).isEmpty();
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
        // Players in-dimension already reset the counter via MobEntity#checkDespawn.
        if (!interiorWorld.players().isEmpty()) {
            return;
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        for (Entity entity : interiorWorld.getEntities((Entity) null, footprintBox(origin), e -> !e.isRemoved())) {
            if (entity instanceof Mob mob && mob.getNoActionTime() != 0) {
                mob.setNoActionTime(0);
            }
        }
    }

    /**
     * Samples entities intersecting the footprint into relative structure coordinates.
     * Uses {@link Entity#saveAsPassenger}; players are special-cased ({@code EntityTypes.PLAYER} is not saveable).
     */
    public static List<BotiEntitySample> sampleEntities(ServerLevel interiorWorld, UUID tardisId) {
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        List<Entity> found = interiorWorld.getEntities((Entity) null, footprintBox(origin), entity -> !entity.isRemoved());
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
        if (entity instanceof ConsoleControlInteractionEntity) {
            return null;
        }
        CompoundTag nbt = captureEntityNbt(entity);
        if (nbt == null) {
            return null;
        }
        float relX = (float) (entity.getX() - plotOrigin.getX());
        float relY = (float) (entity.getY() - plotOrigin.getY());
        float relZ = (float) (entity.getZ() - plotOrigin.getZ());
        writeRelativePos(nbt, relX, relY, relZ);
        writeRelativeAttachment(nbt, plotOrigin);
        return new BotiEntitySample(relX, relY, relZ, entity.getYRot(), entity.getXRot(), nbt);
    }

    /**
     * Entity NBT with type {@code id}. Players get profile tags for client {@code OtherClientPlayerEntity}.
     */
    public static CompoundTag captureEntityNbt(Entity entity) {
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

    public static boolean isInsideFootprint(BlockPos worldPos, BlockPos plotOrigin) {
        int localX = worldPos.getX() - plotOrigin.getX();
        int localY = worldPos.getY() - plotOrigin.getY();
        int localZ = worldPos.getZ() - plotOrigin.getZ();
        return localX >= 0 && localX < SIZE_X
                && localY >= 0 && localY < SIZE_Y
                && localZ >= 0 && localZ < SIZE_Z;
    }

    /** Samples interior sky/fog atmosphere at the plot origin. */
    public static PortalAtmosphere sampleAtmosphere(ServerLevel interiorWorld, BlockPos plotOrigin) {
        if (interiorWorld == null || plotOrigin == null) {
            return PortalAtmosphere.DEFAULT;
        }
        Identifier effectsId = interiorWorld.dimensionTypeRegistration()
                .unwrapKey()
                .map(ResourceKey::identifier)
                .orElseGet(BuiltinDimensionTypes.OVERWORLD::identifier);
        long timeOfDay = interiorWorld.getOverworldClockTime();
        float rain = interiorWorld.getRainLevel(0.0f);
        float thunder = interiorWorld.getThunderLevel(0.0f);
        var attrs = interiorWorld.environmentAttributes();
        return new PortalAtmosphere(
                effectsId,
                timeOfDay,
                rain,
                thunder,
                attrs.getValue(EnvironmentAttributes.SKY_COLOR, plotOrigin),
                attrs.getValue(EnvironmentAttributes.FOG_COLOR, plotOrigin)
        );
    }

    /**
     * Collects visible block states (+ BE NBT) for one chunk column clipped to the footprint.
     * Positions in the returned maps are world-absolute.
     */
    public static PortalStreamSample sampleStreamChunk(
            ServerLevel interiorWorld,
            UUID tardisId,
            int chunkX,
            int chunkZ
    ) {
        if (interiorWorld == null || tardisId == null) {
            return new PortalStreamSample(chunkX, chunkZ, Map.of(), Map.of(), PortalLightData.EMPTY);
        }
        BlockPos plotOrigin = TardisPlotAllocator.plotOrigin(tardisId);
        interiorWorld.getChunk(chunkX, chunkZ);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
        HolderLookup.Provider registries = interiorWorld.registryAccess();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minY = plotOrigin.getY();
        int maxY = plotOrigin.getY() + SIZE_Y - 1;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                for (int y = minY; y <= maxY; y++) {
                    mutable.set(baseX + lx, y, baseZ + lz);
                    if (!isInsideFootprint(mutable, plotOrigin)) {
                        continue;
                    }
                    BlockState state = interiorWorld.getBlockState(mutable);
                    if (!isBotiVisible(state)) {
                        continue;
                    }
                    BlockPos immutable = mutable.immutable();
                    blocks.put(immutable, state);
                    BlockEntity blockEntity = interiorWorld.getBlockEntity(mutable);
                    if (blockEntity != null) {
                        blockEntities.put(immutable, captureSyncNbt(blockEntity, registries));
                    }
                }
            }
        }
        int minX = Math.max(baseX, plotOrigin.getX());
        int maxX = Math.min(baseX + 15, plotOrigin.getX() + SIZE_X - 1);
        int minZ = Math.max(baseZ, plotOrigin.getZ());
        int maxZ = Math.min(baseZ + 15, plotOrigin.getZ() + SIZE_Z - 1);
        PortalLightData lightData = PortalLightData.sample(
                interiorWorld,
                new BlockPos(minX, minY, minZ),
                new BlockPos(maxX, maxY, maxZ)
        );
        // #region agent log
        try {
            int maxBlock = 0, maxSky = 0;
            for (byte value : lightData.packedCopy()) {
                int packed = Byte.toUnsignedInt(value);
                maxBlock = Math.max(maxBlock, packed & 0xF);
                maxSky = Math.max(maxSky, packed >>> 4);
            }
            BlockPos expectedLight = plotOrigin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));
            java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"),
                    "{\"hypothesisId\":\"A\",\"location\":\"BotiInteriorSampler.sampleStreamChunk\",\"message\":\"server sampled BOTI light\",\"data\":{\"chunkX\":" + chunkX + ",\"chunkZ\":" + chunkZ + ",\"blocks\":" + blocks.size() + ",\"minX\":" + lightData.min().getX() + ",\"minY\":" + lightData.min().getY() + ",\"minZ\":" + lightData.min().getZ() + ",\"sizeX\":" + lightData.sizeX() + ",\"sizeY\":" + lightData.sizeY() + ",\"sizeZ\":" + lightData.sizeZ() + ",\"maxBlock\":" + maxBlock + ",\"maxSky\":" + maxSky + ",\"expectedLightWorldBlock\":" + interiorWorld.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, expectedLight) + ",\"expectedLightSampleBlock\":" + lightData.brightness(net.minecraft.world.level.LightLayer.BLOCK, expectedLight, -1) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException ignored) {
        }
        // #endregion
        BlockPos expectedLight = plotOrigin.offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE.above(3));
        if ((expectedLight.getX() >> 4) == chunkX && (expectedLight.getZ() >> 4) == chunkZ) {
            var lightEngine = interiorWorld.getLightEngine();
            var sourceChunk = interiorWorld.getChunkAt(expectedLight);
            BlockState sourceState = interiorWorld.getBlockState(expectedLight);
            net.minecraft.core.SectionPos sourceSection = net.minecraft.core.SectionPos.of(expectedLight);
            // #region agent log
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("/opt/cursor/logs/debug.log"),
                        "{\"hypothesisId\":\"G,H,I,J\",\"location\":\"BotiInteriorSampler.sampleStreamChunk:source\",\"message\":\"light source state when BOTI samples\",\"data\":{\"block\":\"" + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(sourceState.getBlock()) + "\",\"isLight\":" + sourceState.is(Blocks.LIGHT) + ",\"emission\":" + sourceState.getLightEmission() + ",\"brightness\":" + interiorWorld.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, expectedLight) + ",\"rawBrightness\":" + lightEngine.getLayerListener(net.minecraft.world.level.LightLayer.BLOCK).getLightValue(expectedLight) + ",\"neighborBrightness\":" + interiorWorld.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, expectedLight.below()) + ",\"hasLightWork\":" + lightEngine.hasLightWork() + ",\"lightOnInColumn\":" + lightEngine.lightOnInColumn(sourceChunk.getPos().pack()) + ",\"chunkLightCorrect\":" + sourceChunk.isLightCorrect() + ",\"chunkStatus\":\"" + sourceChunk.getPersistedStatus() + "\",\"sectionType\":\"" + lightEngine.getDebugSectionType(net.minecraft.world.level.LightLayer.BLOCK, sourceSection) + "\",\"sectionDataPresent\":" + (lightEngine.getLayerListener(net.minecraft.world.level.LightLayer.BLOCK).getDataLayerData(sourceSection) != null) + ",\"engine\":\"" + lightEngine.getClass().getName() + "\",\"hasSkyLight\":" + interiorWorld.dimensionType().hasSkyLight() + ",\"ambientLight\":" + interiorWorld.dimensionType().ambientLight() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                        java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            } catch (java.io.IOException ignored) {
            }
            // #endregion
        }
        return new PortalStreamSample(
                chunkX, chunkZ, Map.copyOf(blocks), Map.copyOf(blockEntities), lightData
        );
    }

    public static List<Entity> collectStreamEntities(ServerLevel interiorWorld, UUID tardisId) {
        if (interiorWorld == null || tardisId == null) {
            return List.of();
        }
        BlockPos origin = TardisPlotAllocator.plotOrigin(tardisId);
        ensureFootprintChunksLoaded(interiorWorld, origin);
        return List.copyOf(interiorWorld.getEntities((Entity) null, footprintBox(origin), entity -> !entity.isRemoved()));
    }
}
