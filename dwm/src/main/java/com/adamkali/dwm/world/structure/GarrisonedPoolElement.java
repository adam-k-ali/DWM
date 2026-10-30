package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekVariant;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

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
    public static final MapCodec<GarrisonedPoolElement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec(),
            GarrisonPost.CODEC.listOf().optionalFieldOf("garrison", List.of()).forGetter(GarrisonedPoolElement::garrison)
    ).apply(i, GarrisonedPoolElement::new));

    private final List<GarrisonPost> garrison;

    protected GarrisonedPoolElement(
            Either<Identifier, StructureTemplate> template,
            Holder<StructureProcessorList> processors,
            StructureTemplatePool.Projection projection,
            Optional<LiquidSettings> overrideLiquidSettings,
            List<GarrisonPost> garrison
    ) {
        super(template, processors, projection, overrideLiquidSettings);
        this.garrison = List.copyOf(garrison);
    }

    public List<GarrisonPost> garrison() {
        return garrison;
    }

    @Override
    public boolean place(
            StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager,
            ChunkGenerator generator, BlockPos position, BlockPos referencePos, Rotation rotation,
            BoundingBox chunkBB, RandomSource random, LiquidSettings liquidSettings, boolean keepJigsaws
    ) {
        if (!super.place(structureTemplateManager, level, structureManager, generator, position, referencePos,
                rotation, chunkBB, random, liquidSettings, keepJigsaws)) {
            return false;
        }
        // super.place consumes `random` depending on which blocks fall inside chunkBB, so garrison rolls use their own
        // stream seeded by the piece position: every chunk draws the same results for the same post.
        RandomSource garrisonRandom = RandomSource.create(Mth.getSeed(position));
        for (GarrisonPost post : garrison) {
            spawn(level, post, position, rotation, chunkBB, garrisonRandom);
        }
        return true;
    }

    private void spawn(
            WorldGenLevel level, GarrisonPost post, BlockPos position, Rotation rotation, BoundingBox chunkBB,
            RandomSource random
    ) {
        // Rolls are drawn for every post, in order, whichever chunk owns it, so results do not depend on chunk order.
        boolean manned = random.nextFloat() < post.chance();
        long variantSeed = random.nextLong();
        BlockPos world = toWorld(post.pos(), position, rotation);
        if (!manned || !chunkBB.isInside(world)) {
            return;
        }
        if (getProjection() == StructureTemplatePool.Projection.TERRAIN_MATCHING) {
            world = new BlockPos(world.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, world.getX(), world.getZ()), world.getZ());
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(post.entity()).orElse(null);
        if (type == null) {
            return;
        }
        Entity entity = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (!(entity instanceof DalekEntity dalek)) {
            return;
        }
        Direction facing = rotation.rotate(post.facing());
        List<BlockPos> workSpots = post.workSpots().stream().map(spot -> toWorld(spot, position, rotation)).toList();
        dalek.setPersistenceRequired();
        dalek.snapTo(world.getX() + 0.5, world.getY(), world.getZ() + 0.5, facing.toYRot(), 0.0F);
        dalek.finalizeSpawn(level, level.getCurrentDifficultyAt(world), EntitySpawnReason.STRUCTURE, null);
        dalek.setVariant(post.variant()
                .map(id -> DalekVariant.byId(id, dalek.family()))
                .orElseGet(() -> DalekVariant.pick(RandomSource.create(variantSeed), dalek.family())));
        dalek.assignJob(post.job(), world, facing.toYRot(), post.radius(), workSpots);
        level.addFreshEntityWithPassengers(dalek);
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
