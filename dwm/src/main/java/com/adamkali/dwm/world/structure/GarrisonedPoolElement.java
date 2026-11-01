package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekVariant;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link SinglePoolElement} that also mans its piece with garrison Daleks. Templates stay entity-free: the same
 * {@code .nbt} can be garrisoned heavily in an intact pool and lightly (or not at all) in its ruined twin.
 *
 * <p>Each post is spawned by the chunk that contains it, so a piece spanning chunks never spawns a post twice.
 * Rolls and variants come from a random seeded by the piece position, so garrisons are deterministic and do not
 * depend on which chunk happens to place the piece.
 */
public class GarrisonedPoolElement extends SinglePoolElement {
    /** Deepest a terrain-matching piece may be carved below the surface. */
    public static final int MAX_SINK = 8;
    /** Vanilla's terrain-matching gravity offset: template layer 0 sits on the top ground block. */
    private static final int TERRAIN_OFFSET = -1;

    public static final MapCodec<GarrisonedPoolElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec(),
            GarrisonPost.CODEC.listOf().optionalFieldOf("garrison", List.of()).forGetter(GarrisonedPoolElement::garrison),
            Codec.intRange(0, MAX_SINK).optionalFieldOf("sink", 0).forGetter(GarrisonedPoolElement::sink)
    ).apply(i, GarrisonedPoolElement::new));

    private final List<GarrisonPost> garrison;
    private final int sink;

    protected GarrisonedPoolElement(
            Either<Identifier, StructureTemplate> template,
            Holder<StructureProcessorList> processors,
            StructureTemplatePool.Projection projection,
            Optional<LiquidSettings> overrideLiquidSettings,
            List<GarrisonPost> garrison,
            int sink
    ) {
        super(template, processors, projection, overrideLiquidSettings);
        this.garrison = List.copyOf(garrison);
        this.sink = sink;
    }

    public List<GarrisonPost> garrison() {
        return garrison;
    }

    /** Blocks a terrain-matching piece is lowered below vanilla's placement, carving it into the ground. */
    public int sink() {
        return sink;
    }

    /**
     * Vanilla's terrain-matching gravity processor puts template layer 0 on the top ground block. A second
     * gravity processor works from each block's template-relative height, so it replaces the first and sets the
     * piece {@code sink} blocks deeper.
     */
    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox chunkBB, LiquidSettings liquidSettings, boolean keepJigsaws) {
        StructurePlaceSettings settings = super.getSettings(rotation, chunkBB, liquidSettings, keepJigsaws);
        if (sink > 0 && getProjection() == StructureTemplatePool.Projection.TERRAIN_MATCHING) {
            settings.addProcessor(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, TERRAIN_OFFSET - sink));
        }
        return settings;
    }

    @Override
    public boolean place(
            StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager,
            ChunkGenerator generator, BlockPos position, BlockPos referencePos, Rotation rotation,
            BoundingBox chunkBB, RandomSource random, LiquidSettings liquidSettings, boolean keepJigsaws
    ) {
        // Resolve post heights before placing: a sunk piece's own parapet raises the surface heightmap, which would
        // push posts up if they were snapped afterwards.
        // super.place consumes `random` depending on which blocks fall inside chunkBB, so garrison rolls use their own
        // stream seeded by the piece position: every chunk draws the same results for the same post.
        RandomSource garrisonRandom = RandomSource.create(Mth.getSeed(position));
        List<PlannedPost> planned = new ArrayList<>();
        for (GarrisonPost post : garrison) {
            plan(level, post, position, rotation, chunkBB, garrisonRandom).ifPresent(planned::add);
        }
        if (!super.place(structureTemplateManager, level, structureManager, generator, position, referencePos,
                rotation, chunkBB, random, liquidSettings, keepJigsaws)) {
            return false;
        }
        planned.forEach(post -> spawn(level, post, rotation));
        return true;
    }

    /** A garrison post resolved to world coordinates, ready to spawn once its piece is placed. */
    private record PlannedPost(GarrisonPost post, BlockPos world, List<BlockPos> workSpots, long variantSeed) {
    }

    private Optional<PlannedPost> plan(
            WorldGenLevel level, GarrisonPost post, BlockPos position, Rotation rotation, BoundingBox chunkBB,
            RandomSource random
    ) {
        // Rolls are drawn for every post, in order, whichever chunk owns it, so results do not depend on chunk order.
        boolean manned = random.nextFloat() < post.chance();
        long variantSeed = random.nextLong();
        BlockPos world = toWorld(post.pos(), position, rotation);
        if (!manned || !chunkBB.isInside(world)) {
            return Optional.empty();
        }
        BlockPos snapped = snapToTerrain(level, world, post.pos().getY());
        List<BlockPos> workSpots = post.workSpots().stream().map(spot -> {
            BlockPos spotWorld = toWorld(spot, position, rotation);
            if (getProjection() != StructureTemplatePool.Projection.TERRAIN_MATCHING) {
                return spotWorld;
            }
            // Columns outside this chunk may already hold a neighbouring chunk's placed blocks, so their surface can
            // no longer be read; keep the template's height difference from the post instead.
            return chunkBB.isInside(spotWorld)
                    ? snapToTerrain(level, spotWorld, spot.getY())
                    : new BlockPos(spotWorld.getX(), snapped.getY() + spot.getY() - post.pos().getY(), spotWorld.getZ());
        }).toList();
        return Optional.of(new PlannedPost(post, snapped, workSpots, variantSeed));
    }

    private void spawn(WorldGenLevel level, PlannedPost planned, Rotation rotation) {
        GarrisonPost post = planned.post();
        BlockPos world = planned.world();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(post.entity()).orElse(null);
        if (type == null) {
            return;
        }
        Entity entity = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (!(entity instanceof DalekEntity dalek)) {
            return;
        }
        Direction facing = rotation.rotate(post.facing());
        dalek.setPersistenceRequired();
        dalek.snapTo(world.getX() + 0.5, world.getY(), world.getZ() + 0.5, facing.toYRot(), 0.0F);
        dalek.finalizeSpawn(level, level.getCurrentDifficultyAt(world), EntitySpawnReason.STRUCTURE, null);
        dalek.setVariant(post.variant()
                .map(id -> DalekVariant.byId(id, dalek.family()))
                .orElseGet(() -> DalekVariant.pick(RandomSource.create(planned.variantSeed()), dalek.family())));
        dalek.assignJob(post.job(), world, facing.toYRot(), post.radius(), planned.workSpots());
        level.addFreshEntityWithPassengers(dalek);
    }

    /** Terrain-matching pieces follow the surface, so posts and work spots ride it too. */
    private BlockPos snapToTerrain(WorldGenLevel level, BlockPos world, int localY) {
        if (getProjection() != StructureTemplatePool.Projection.TERRAIN_MATCHING) {
            return world;
        }
        return new BlockPos(world.getX(), terrainY(surfaceHeight(level, world.getX(), world.getZ()), localY, sink), world.getZ());
    }

    /**
     * The height vanilla's gravity processor reads: the live surface when placing into a loaded level (for example
     * {@code /place}), the worldgen surface while generating. Posts must use the same one as the blocks.
     */
    private static int surfaceHeight(WorldGenLevel level, int x, int z) {
        Heightmap.Types heightmap = level instanceof ServerLevel ? Heightmap.Types.WORLD_SURFACE : Heightmap.Types.WORLD_SURFACE_WG;
        return level.getHeight(heightmap, x, z);
    }

    /** Where template height {@code localY} lands on a terrain-matching piece sunk {@code sink} blocks. */
    public static int terrainY(int surface, int localY, int sink) {
        return surface + TERRAIN_OFFSET - sink + localY;
    }

    private static BlockPos toWorld(BlockPos local, BlockPos position, Rotation rotation) {
        return StructureTemplate.transform(local, Mirror.NONE, rotation, BlockPos.ZERO).offset(position);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return DWMStructurePoolElementTypes.GARRISONED_SINGLE;
    }

    @Override
    public String toString() {
        return "Garrisoned[" + template + ", " + garrison.size() + " posts]";
    }
}
