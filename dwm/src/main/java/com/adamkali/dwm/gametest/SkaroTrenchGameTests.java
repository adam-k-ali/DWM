package com.adamkali.dwm.gametest;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekJob;
import com.adamkali.dwm.world.structure.GarrisonedPoolElement;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Placement checks for the Dalek battlefield trench (DWM-069). */
public class SkaroTrenchGameTests {
    private static final Identifier CHECKPOINT = id("skaro/dalek_trench/checkpoint");
    private static final Set<Identifier> PARTS = Set.of(
            id("skaro/dalek_trench/segment"),
            id("skaro/dalek_trench/junction"),
            id("skaro/dalek_trench/bunker"),
            id("skaro/dalek_trench/end_cap"),
            id("skaro/dalek_trench/zigzag"),
            id("skaro/dalek_trench/zigzag_mirrored")
    );
    private static final Set<DalekJob> TRENCH_JOBS = EnumSet.of(
            DalekJob.SENTRY, DalekJob.PATROL, DalekJob.LOOKOUT, DalekJob.TECHNICIAN, DalekJob.GUNNER, DalekJob.COURIER);
    /** Blocks the trench is carved below ground; the structure's {@code start_height} is its negative. */
    private static final int SINK = 2;
    private static final ResourceKey<LootTable> LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, id("chests/skaro/dalek_outpost"));

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void checkpointPlacesOneLootChestAndOpenGates(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        BlockPos origin = context.absolutePos(new BlockPos(0, 1, 0));
        StructureTemplate checkpoint = template(level, CHECKPOINT);
        checkpoint.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
        BoundingBox box = BoundingBox.fromCorners(origin, origin.offset(checkpoint.getSize()).offset(-1, -1, -1));

        assertSingleLootChest(level, box);
        assertNoEntities(level, box);

        // Gates: x 4-6 on the north (z = 0) and south (z = 10) walls, three high over a solid floor.
        for (int x = 4; x <= 6; x++) {
            for (int z : new int[] {0, 10}) {
                BlockPos floor = origin.offset(x, 0, z);
                if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                    throw new AssertionError("Expected solid floor under the checkpoint gate at " + floor);
                }
                for (int y = 1; y <= 3; y++) {
                    if (!level.getBlockState(origin.offset(x, y, z)).isAir()) {
                        throw new AssertionError("Expected an open gate at " + origin.offset(x, y, z));
                    }
                }
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void trenchPartsPlaceWithoutContainersOrEntities(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        int offsetX = 0;
        for (Identifier part : PARTS) {
            BlockPos origin = context.absolutePos(new BlockPos(offsetX, 1, 0));
            StructureTemplate template = template(level, part);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
            BoundingBox box = BoundingBox.fromCorners(origin, origin.offset(template.getSize()).offset(-1, -1, -1));
            for (BlockPos pos : cells(box)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && !level.getBlockState(pos).is(Blocks.JIGSAW)) {
                    throw new AssertionError(part + " must not contain a container, found " + blockEntity.getType() + " at " + pos);
                }
            }
            assertNoEntities(level, box);
            offsetX += template.getSize().getX() + 1;
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void intactTrenchAssemblesAndIsGarrisoned(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        List<PoolElementStructurePiece> pieces = assembleAndAssert(context, "dalek_trench");
        assertGarrison(level, pieces);
        BoundingBox checkpointBox = pieces.getFirst().getBoundingBox();
        if (countSilverWalls(level, checkpointBox) != templateSilverWalls(level)) {
            throw new AssertionError("Intact trench checkpoint should keep every silver Dalekanium wall");
        }
        garrison(level, pieces).forEach(Entity::discard);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void ruinedTrenchDecaysWallsButKeepsItsChest(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        List<PoolElementStructurePiece> pieces = assembleAndAssert(context, "dalek_trench_ruined");
        assertGarrison(level, pieces);
        if (countSilverWalls(level, pieces.getFirst().getBoundingBox()) >= templateSilverWalls(level)) {
            throw new AssertionError("Ruined trench checkpoint should lose some silver walls to block rot");
        }
        garrison(level, pieces).forEach(Entity::discard);
        context.succeed();
    }

    /** Places the real worldgen structure, preferring a start that chains at least four pieces. */
    private static List<PoolElementStructurePiece> assembleAndAssert(GameTestHelper context, String structureId) {
        ServerLevel level = context.getLevel();
        // Anchor the checkpoint floor SINK below the ground, as worldgen's start_height does.
        BlockPos at = context.absolutePos(new BlockPos(8, 1, 8));
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, at.getX(), at.getZ());
        List<PoolElementStructurePiece> pieces = StructureGameTestSupport.placeJigsaw(
                level, id(structureId), new BlockPos(at.getX(), surface - 1 - SINK, at.getZ()), 4);
        for (int i = 0; i < pieces.size(); i++) {
            if (!(pieces.get(i).getElement() instanceof GarrisonedPoolElement element)) {
                throw new AssertionError(structureId + " placed an unexpected element " + pieces.get(i).getElement());
            }
            Identifier location = element.getTemplateLocation();
            boolean expected = i == 0 ? location.equals(CHECKPOINT) : PARTS.contains(location);
            if (!expected) {
                throw new AssertionError(structureId + " piece " + i + " used unexpected template " + location);
            }
        }
        // Per piece, not the union box: a long trench's bounds sweep in neighbouring tests' blocks and entities.
        assertSingleLootChest(level, pieces.getFirst().getBoundingBox());
        for (int i = 0; i < pieces.size(); i++) {
            BoundingBox box = pieces.get(i).getBoundingBox();
            if (i > 0) {
                assertNoContainers(level, box);
            }
            for (BlockPos pos : cells(box)) {
                if (level.getBlockState(pos).is(Blocks.JIGSAW)) {
                    throw new AssertionError(structureId + " left a jigsaw block at " + pos);
                }
            }
        }
        return pieces;
    }

    /** The checkpoint's four gate sentries always spawn; every Dalek has a trench job, a post and persistence. */
    private static void assertGarrison(ServerLevel level, List<PoolElementStructurePiece> pieces) {
        List<DalekEntity> daleks = garrison(level, pieces);
        long gateSentries = daleks.stream()
                .filter(dalek -> dalek.getJob() == DalekJob.SENTRY)
                .filter(dalek -> pieces.getFirst().getBoundingBox().isInside(dalek.getPost()))
                .count();
        if (gateSentries < 4) {
            throw new AssertionError("Expected four checkpoint gate sentries but found " + gateSentries);
        }
        for (DalekEntity dalek : daleks) {
            if (!TRENCH_JOBS.contains(dalek.getJob()) || dalek.getPost() == null || !dalek.isPersistenceRequired()) {
                throw new AssertionError("Bad trench garrison Dalek " + dalek + " job=" + dalek.getJob());
            }
            if (dalek.getJob() == DalekJob.COURIER && dalek.getWorkSpots().isEmpty()) {
                throw new AssertionError("Courier without a route: " + dalek);
            }
            // Every post, carved or not, stands on solid floor with headroom for a tall Dalek.
            BlockPos post = dalek.getPost();
            if (!level.getBlockState(post.below()).isFaceSturdy(level, post.below(), Direction.UP)) {
                throw new AssertionError(dalek.getJob() + " post " + post + " has no floor");
            }
            for (int y = 0; y < 3; y++) {
                if (!level.getBlockState(post.above(y)).getCollisionShape(level, post.above(y)).isEmpty()) {
                    throw new AssertionError(dalek.getJob() + " post " + post + " is blocked at +" + y);
                }
            }
        }
    }

    /**
     * Persistent Daleks posted within a piece's footprint. Full height, because terrain-matching pieces put their
     * posts on the surface, not inside the template box; persistence excludes other tests' Daleks nearby.
     */
    private static List<DalekEntity> garrison(ServerLevel level, List<PoolElementStructurePiece> pieces) {
        List<DalekEntity> result = new ArrayList<>();
        for (PoolElementStructurePiece piece : pieces) {
            BoundingBox box = piece.getBoundingBox();
            AABB column = new AABB(box.minX() - 1, level.getMinY(), box.minZ() - 1, box.maxX() + 2, level.getMaxY(), box.maxZ() + 2);
            for (DalekEntity dalek : level.getEntitiesOfClass(DalekEntity.class, column)) {
                BlockPos post = dalek.getPost();
                if (dalek.isPersistenceRequired() && post != null && post.getX() >= box.minX() && post.getX() <= box.maxX()
                        && post.getZ() >= box.minZ() && post.getZ() <= box.maxZ() && !result.contains(dalek)) {
                    result.add(dalek);
                }
            }
        }
        return result;
    }

    private static void assertNoContainers(ServerLevel level, BoundingBox box) {
        for (BlockPos pos : cells(box)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && !level.getBlockState(pos).is(Blocks.JIGSAW)) {
                throw new AssertionError("Only the checkpoint may hold a container, found " + blockEntity.getType() + " at " + pos);
            }
        }
    }

    private static void assertSingleLootChest(ServerLevel level, BoundingBox box) {
        int chests = 0;
        for (BlockPos pos : cells(box)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null || level.getBlockState(pos).is(Blocks.JIGSAW)) {
                continue;
            }
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                throw new AssertionError("Unexpected block entity " + blockEntity.getType() + " at " + pos);
            }
            if (!LOOT_TABLE.equals(chest.getLootTable())) {
                throw new AssertionError("Trench chest at " + pos + " has loot table " + chest.getLootTable());
            }
            chests++;
        }
        if (chests != 1) {
            throw new AssertionError("Expected exactly one trench loot chest but found " + chests);
        }
    }

    private static void assertNoEntities(ServerLevel level, BoundingBox box) {
        List<Entity> entities = level.getEntities((Entity) null, AABB.of(box).inflate(1.0), entity -> !(entity instanceof Player));
        if (!entities.isEmpty()) {
            throw new AssertionError("Trench templates must not embed entities, found " + entities);
        }
    }

    private static int countSilverWalls(ServerLevel level, BoundingBox box) {
        int count = 0;
        for (BlockPos pos : cells(box)) {
            BlockState state = level.getBlockState(pos);
            if (DWMBlocks.SILVER_DALEKANIUM_ARCHITECTURE.contains(state.getBlock())) {
                count++;
            }
        }
        return count;
    }

    private static int templateSilverWalls(ServerLevel level) {
        int count = 0;
        for (Block silver : DWMBlocks.SILVER_DALEKANIUM_ARCHITECTURE) {
            count += template(level, CHECKPOINT).filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), silver).size();
        }
        return count;
    }

    private static Iterable<BlockPos> cells(BoundingBox box) {
        return BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    private static StructureTemplate template(ServerLevel level, Identifier id) {
        return level.getStructureManager().get(id)
                .orElseThrow(() -> new AssertionError("Missing structure template " + id));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
    }
}
