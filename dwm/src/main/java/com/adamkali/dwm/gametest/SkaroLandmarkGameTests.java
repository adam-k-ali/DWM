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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Placement checks for Skaro landmarks (DWM-069): the modular Dalek outpost. */
public class SkaroLandmarkGameTests {
    private static final Identifier CORE = id("skaro/dalek_outpost/core");
    private static final Set<Identifier> WINGS = Set.of(
            id("skaro/dalek_outpost/watchtower"),
            id("skaro/dalek_outpost/barricade"),
            id("skaro/dalek_outpost/sensor_mast")
    );
    private static final ResourceKey<LootTable> LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, id("chests/skaro/dalek_outpost"));

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void outpostCorePlacesOneLootChestAndAnOpenDoorway(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        BlockPos origin = context.absolutePos(new BlockPos(0, 1, 0));
        StructureTemplate core = template(level, CORE);
        core.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
        BoundingBox box = BoundingBox.fromCorners(origin, origin.offset(core.getSize()).offset(-1, -1, -1));

        assertSingleLootChest(level, box);
        assertNoEntities(level, box);

        // Doorway: x 4-6 on the south wall (z = 9), two blocks high over a solid floor,
        // with the apron in front (z = 10) carved so the approach is walkable.
        for (int x = 4; x <= 6; x++) {
            for (int z = 9; z <= 10; z++) {
                BlockPos floor = origin.offset(x, 0, z);
                if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                    throw new AssertionError("Expected solid floor under the outpost doorway at " + floor);
                }
                for (int y = 1; y <= 2; y++) {
                    if (!level.getBlockState(origin.offset(x, y, z)).isAir()) {
                        throw new AssertionError("Expected an open doorway at " + origin.offset(x, y, z));
                    }
                }
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void outpostWingsPlaceWithoutContainersOrEntities(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        int offsetX = 0;
        for (Identifier wing : WINGS) {
            BlockPos origin = context.absolutePos(new BlockPos(offsetX, 1, 0));
            StructureTemplate template = template(level, wing);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
            BoundingBox box = BoundingBox.fromCorners(origin, origin.offset(template.getSize()).offset(-1, -1, -1));
            for (BlockPos pos : cells(box)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && !level.getBlockState(pos).is(Blocks.JIGSAW)) {
                    throw new AssertionError(wing + " must not contain a container, found " + blockEntity.getType() + " at " + pos);
                }
            }
            assertNoEntities(level, box);
            offsetX += template.getSize().getX() + 1;
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void intactOutpostAssemblesCoreAndWings(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        List<PoolElementStructurePiece> pieces = assembleAndAssert(context, "dalek_outpost");
        BoundingBox coreBox = pieces.getFirst().getBoundingBox();
        assertIntactGarrison(level, pieces);
        if (countSilverWalls(level, coreBox) != templateSilverWalls(level)) {
            throw new AssertionError("Intact outpost core should keep every silver Dalekanium wall");
        }
        discardDaleks(level, StructureGameTestSupport.bounds(pieces));
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void ruinedOutpostDecaysWallsButKeepsItsChest(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        List<PoolElementStructurePiece> pieces = assembleAndAssert(context, "dalek_outpost_ruined");
        BoundingBox coreBox = pieces.getFirst().getBoundingBox();
        assertRuinedGarrison(level, pieces);
        if (countSilverWalls(level, coreBox) >= templateSilverWalls(level)) {
            throw new AssertionError("Ruined outpost core should lose some silver walls to block rot");
        }
        discardDaleks(level, StructureGameTestSupport.bounds(pieces));
        context.succeed();
    }

    /**
     * Places the real worldgen structure on the test origin, picking the first fixed seed that attaches
     * at least two wings so the jigsaw connections are exercised, then checks the assembled outpost.
     */
    private static List<PoolElementStructurePiece> assembleAndAssert(GameTestHelper context, String structureId) {
        ServerLevel level = context.getLevel();
        List<PoolElementStructurePiece> pieces = StructureGameTestSupport.placeJigsaw(
                level, id(structureId), context.absolutePos(new BlockPos(8, 1, 8)), 3);
        assertPieceTemplates(structureId, pieces);

        BoundingBox box = StructureGameTestSupport.bounds(pieces);
        assertSingleLootChest(level, box);
        for (BlockPos pos : cells(box)) {
            if (level.getBlockState(pos).is(Blocks.JIGSAW)) {
                throw new AssertionError(structureId + " left a jigsaw block at " + pos);
            }
        }
        return pieces;
    }

    private static void assertPieceTemplates(String structureId, List<PoolElementStructurePiece> pieces) {
        for (int i = 0; i < pieces.size(); i++) {
            if (!(pieces.get(i).getElement() instanceof GarrisonedPoolElement element)) {
                throw new AssertionError(structureId + " placed an unexpected element " + pieces.get(i).getElement());
            }
            Identifier location = element.getTemplateLocation();
            boolean expected = i == 0 ? location.equals(CORE) : WINGS.contains(location);
            if (!expected) {
                throw new AssertionError(structureId + " piece " + i + " used unexpected template " + location);
            }
        }
    }

    /** Core: 2 sentries, 1 technician, 1-2 patrols. Each wing adds exactly its guaranteed post. */
    private static void assertIntactGarrison(ServerLevel level, List<PoolElementStructurePiece> pieces) {
        List<DalekEntity> daleks = daleks(level, StructureGameTestSupport.bounds(pieces));
        Map<DalekJob, Integer> jobs = countJobs(daleks);
        int watchtowers = countWings(pieces, "watchtower");
        int barricades = countWings(pieces, "barricade");
        int masts = countWings(pieces, "sensor_mast");
        expect(jobs, DalekJob.SENTRY, 2 + barricades, 2 + barricades);
        expect(jobs, DalekJob.TECHNICIAN, 1 + masts, 1 + masts);
        expect(jobs, DalekJob.LOOKOUT, watchtowers, watchtowers);
        expect(jobs, DalekJob.PATROL, 1, 2);
        for (PoolElementStructurePiece piece : pieces) {
            if (piece.getElement() instanceof GarrisonedPoolElement element
                    && element.getTemplateLocation().getPath().endsWith("watchtower")) {
                long onDeck = daleks.stream()
                        .filter(dalek -> dalek.getJob() == DalekJob.LOOKOUT)
                        .filter(dalek -> piece.getBoundingBox().isInside(dalek.getPost()))
                        .filter(dalek -> dalek.getPost().getY() >= piece.getBoundingBox().minY() + 8)
                        .count();
                if (onDeck != 1) {
                    throw new AssertionError("Expected one lookout on each watchtower deck but found " + onDeck);
                }
            }
        }
    }

    /** Remnant only: a sentry guards the core, optionally a patrol and barricade sentries; no technicians or lookouts. */
    private static void assertRuinedGarrison(ServerLevel level, List<PoolElementStructurePiece> pieces) {
        List<DalekEntity> daleks = daleks(level, StructureGameTestSupport.bounds(pieces));
        Map<DalekJob, Integer> jobs = countJobs(daleks);
        int barricades = countWings(pieces, "barricade");
        expect(jobs, DalekJob.TECHNICIAN, 0, 0);
        expect(jobs, DalekJob.LOOKOUT, 0, 0);
        expect(jobs, DalekJob.SENTRY, 1, 1 + barricades);
        expect(jobs, DalekJob.PATROL, 0, 1);
    }

    /** Posts belong to the chunk containing them: placing a piece in two halves spawns what one full pass does. */
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void garrisonSpawnsOncePerPostAcrossChunkBoxes(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        List<PoolElementStructurePiece> pieces = assembleAndAssert(context, "dalek_outpost");
        PoolElementStructurePiece core = pieces.getFirst();
        GarrisonedPoolElement element = (GarrisonedPoolElement) core.getElement();
        BoundingBox box = core.getBoundingBox();
        BoundingBox all = StructureGameTestSupport.bounds(pieces);
        discardDaleks(level, all);

        int whole = placeGarrison(level, core, element, box);
        discardDaleks(level, all);
        int mid = (box.minX() + box.maxX()) / 2;
        int halves = placeGarrison(level, core, element,
                new BoundingBox(box.minX(), box.minY(), box.minZ(), mid, box.maxY(), box.maxZ()))
                + placeGarrison(level, core, element,
                new BoundingBox(mid + 1, box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()));
        if (whole == 0 || whole != halves) {
            throw new AssertionError("Expected split placement to spawn " + whole + " Daleks, got " + halves);
        }
        discardDaleks(level, all);
        context.succeed();
    }

    private static int placeGarrison(
            ServerLevel level, PoolElementStructurePiece piece, GarrisonedPoolElement element, BoundingBox chunkBox
    ) {
        element.place(level.getStructureManager(), level, level.structureManager(), level.getChunkSource().getGenerator(),
                piece.getPosition(), piece.getPosition(), piece.getRotation(), chunkBox, RandomSource.create(7L),
                LiquidSettings.APPLY_WATERLOGGING, false);
        int count = 0;
        for (DalekEntity dalek : daleks(level, chunkBox)) {
            if (chunkBox.isInside(dalek.getPost())) {
                count++;
            }
        }
        return count;
    }

    private static void discardDaleks(ServerLevel level, BoundingBox box) {
        daleks(level, box).forEach(Entity::discard);
    }

    private static List<DalekEntity> daleks(ServerLevel level, BoundingBox box) {
        // Full height: terrain-matching pieces (the barricade) put their post on the surface, not inside the template box.
        AABB column = new AABB(box.minX() - 1, level.getMinY(), box.minZ() - 1, box.maxX() + 2, level.getMaxY(), box.maxZ() + 2);
        return level.getEntitiesOfClass(DalekEntity.class, column);
    }

    private static Map<DalekJob, Integer> countJobs(List<DalekEntity> daleks) {
        Map<DalekJob, Integer> jobs = new EnumMap<>(DalekJob.class);
        for (DalekEntity dalek : daleks) {
            if (dalek.getJob() == DalekJob.NONE || dalek.getPost() == null) {
                throw new AssertionError("Garrison Dalek without a job or post: " + dalek);
            }
            if (!dalek.isPersistenceRequired()) {
                throw new AssertionError("Garrison Dalek must be persistent: " + dalek);
            }
            jobs.merge(dalek.getJob(), 1, Integer::sum);
        }
        return jobs;
    }

    private static int countWings(List<PoolElementStructurePiece> pieces, String name) {
        return (int) pieces.stream()
                .filter(piece -> piece.getElement() instanceof GarrisonedPoolElement element
                        && element.getTemplateLocation().getPath().endsWith(name))
                .count();
    }

    private static void expect(Map<DalekJob, Integer> jobs, DalekJob job, int min, int max) {
        int count = jobs.getOrDefault(job, 0);
        if (count < min || count > max) {
            throw new AssertionError("Expected " + min + ".." + max + " " + job + " Daleks but found " + count + " in " + jobs);
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
                throw new AssertionError("Outpost chest at " + pos + " has loot table " + chest.getLootTable());
            }
            chests++;
        }
        if (chests != 1) {
            throw new AssertionError("Expected exactly one outpost loot chest but found " + chests);
        }
    }

    private static void assertNoEntities(ServerLevel level, BoundingBox box) {
        List<Entity> entities = level.getEntities((Entity) null, AABB.of(box).inflate(1.0), entity -> !(entity instanceof Player));
        if (!entities.isEmpty()) {
            throw new AssertionError("Outpost templates must not embed entities, found " + entities);
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
            count += template(level, CORE).filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), silver).size();
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
