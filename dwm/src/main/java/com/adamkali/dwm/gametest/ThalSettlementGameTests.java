package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.entity.ThalSpawnRules;
import com.adamkali.dwm.world.structure.ThalFrame;
import com.adamkali.dwm.world.structure.ThalSettlementLayout;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.Cell;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.Kind;
import com.adamkali.dwm.world.structure.ThalSettlementLayout.PlannedPiece;
import com.adamkali.dwm.world.structure.ThalSettlementStructure;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a Thal settlement from a fixed seed on a prepared plateau far from the test grid and checks the result.
 * The pieces are placed chunk by chunk through {@link StructureStart#placeInChunk}, exactly as world generation
 * does, so doors and walls that straddle chunk borders are exercised too.
 */
public class ThalSettlementGameTests {
    private static final long SEED = 20260725L;
    private static final int RADIUS = ThalSettlementLayout.BOUND + 10;
    private static final int FLOOR_Y_OFFSET = 8;
    private static final int ENTITY_VISIBLE_TICKS = 10;

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void settlementHasEveryPieceConnectedByPathsAndPopulatedOnce(GameTestHelper context) {
        Site site = Site.prepare(context, 20_000, true);
        try {
            site.placeAll();
        } catch (RuntimeException | AssertionError e) {
            site.release();
            throw e;
        }
        // Freshly spawned entities only become visible to queries once their chunk is entity-accessible.
        context.runAfterDelay(ENTITY_VISIBLE_TICKS, () -> verifySettlement(context, site));
    }

    private static void verifySettlement(GameTestHelper context, Site site) {
        try {
            ServerLevel level = site.level;
            int floorY = site.floorY;

            // Required pieces exist in the plan and are built.
            int fields = 0;
            for (PlannedPiece planned : site.layout.pieces()) {
                ThalFrame frame = site.world(planned);
                switch (planned.kind()) {
                    case SHELTER, HUT, WATCH_POST -> assertDoor(site, planned);
                    case FIELD -> {
                        fields++;
                        assertField(site, frame);
                    }
                }
                if (planned.kind() == Kind.WATCH_POST) {
                    for (int y = 1; y <= 6; y++) {
                        BlockPos ladder = new BlockPos(frame.worldX(1, 1), floorY + y, frame.worldZ(1, 1));
                        check(level.getBlockState(ladder).is(Blocks.LADDER), "watch post ladder missing at " + ladder);
                    }
                    BlockPos deck = new BlockPos(frame.worldX(1, 0), floorY + 5, frame.worldZ(1, 0));
                    check(level.getBlockState(deck).is(DWMBlocks.PETRIFIED_PLANKS), "watch post deck missing at " + deck);
                }
            }
            check(fields == ThalSettlementLayout.FIELD_COUNT, "expected 2 fields, planned " + fields);

            // Paths: every planned path cell is DIRT_PATH with headroom, and every entrance connects to the shelter's.
            for (Cell cell : site.layout.pathCells()) {
                BlockPos pos = new BlockPos(site.centreX + cell.x(), floorY, site.centreZ + cell.z());
                check(level.getBlockState(pos).is(Blocks.DIRT_PATH), "path cell not laid at " + pos);
                check(level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir(),
                        "path cell has no headroom at " + pos);
                check(!level.getBlockState(pos.below()).isAir(), "path cell has no support at " + pos);
            }
            Set<BlockPos> reached = floodPaths(site);
            for (PlannedPiece planned : site.layout.pieces()) {
                Cell entrance = planned.entranceCell();
                BlockPos pos = new BlockPos(site.centreX + entrance.x(), floorY, site.centreZ + entrance.z());
                check(reached.contains(pos), planned.kind() + " entrance " + pos + " is not connected to the shelter by paths");
            }

            // Terrain adaptation: the pit under the shelter was filled and the bumps were levelled.
            BlockPos pit = site.pit;
            check(level.getBlockState(pit).is(DWMBlocks.PETRIFIED_PLANKS), "shelter floor missing over the pit");
            check(level.getBlockState(pit.below()).is(Blocks.DIRT) && level.getBlockState(pit.below(2)).is(Blocks.DIRT),
                    "foundation did not fill the pit under the shelter");
            check(level.getBlockState(site.fieldBump.above()).isAir() && level.getBlockState(site.fieldBump.above(2)).isAir(),
                    "field bump was not cleared");
            check(level.getBlockState(site.pathBump.above()).isAir() && level.getBlockState(site.pathBump.above(2)).isAir(),
                    "path bump was not cleared");

            // Residents: the planned number per building, persistent, inside their building, spawned once.
            int expected = 0;
            for (PlannedPiece planned : site.layout.pieces()) {
                if (planned.residents() == 0) {
                    continue;
                }
                expected += planned.residents();
                List<ThalEntity> inside = site.thalsIn(site.world(planned));
                check(inside.size() == planned.residents(),
                        planned.kind() + " expected " + planned.residents() + " residents, found " + inside.size());
            }
            List<ThalEntity> all = site.allThals();
            check(all.size() == expected, "expected " + expected + " Thals in the settlement, found " + all.size());
            for (ThalEntity thal : all) {
                check(thal.isPersistenceRequired(), "settlement Thals must be persistent");
                check(thal.hasHome()
                                && thal.getHomePosition().getX() == site.centreX
                                && thal.getHomePosition().getZ() == site.centreZ
                                && thal.getHomeRadius() >= ThalSettlementLayout.BOUND,
                        "settlement Thals must be homed to the settlement centre, got " + thal.getHomePosition()
                                + " r=" + thal.getHomeRadius());
            }

            site.placeAll();
            check(site.allThals().size() == expected, "a second postProcess duplicated residents");

            // Save and reload the pieces, as a chunk save would, then place again: still no duplicates.
            StructurePieceSerializationContext serialization = StructurePieceSerializationContext.fromLevel(level);
            Tag saved = site.container.save(serialization);
            PiecesContainer reloaded = PiecesContainer.load((ListTag) saved, serialization);
            check(reloaded.pieces().size() == site.container.pieces().size(), "pieces lost in the save round trip");
            for (int i = 0; i < reloaded.pieces().size(); i++) {
                CompoundTag before = site.container.pieces().get(i).createTag(serialization);
                CompoundTag after = reloaded.pieces().get(i).createTag(serialization);
                check(before.equals(after), "piece " + i + " changed in the save round trip");
            }
            site.placeAll(reloaded);
            check(site.allThals().size() == expected, "reloaded pieces respawned residents");
        } finally {
            site.release();
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void naturalThalSpawnsSucceedOnlyInsideTheSettlement(GameTestHelper context) {
        Site site = Site.prepare(context, 21_000, false);
        try {
            ServerLevel level = site.level;
            PlannedPiece hut = site.layout.ofKind(Kind.HUT).get(0);
            ThalFrame frame = site.world(hut);
            BlockPos inside = new BlockPos(frame.worldX(frame.doorU(), 2), site.floorY + 1, frame.worldZ(frame.doorU(), 2));
            BlockPos outside = new BlockPos(site.centreX + ThalSettlementLayout.BOUND + 6, site.floorY + 1, site.centreZ);

            // Before the structure is registered with the chunks, nowhere counts as a settlement.
            check(!ThalSpawnRules.isInsideSettlement(level, inside), "no settlement should be known yet");
            site.placeAll();
            check(level.getBlockState(inside.below()).is(DWMBlocks.PETRIFIED_PLANKS), "hut floor not placed");
            check(level.getBlockState(outside.below()).is(Blocks.GRASS_BLOCK), "outside floor should be plain grass");

            check(ThalSpawnRules.isInsideSettlement(level, inside), "hut interior must be inside the settlement");
            check(!ThalSpawnRules.isInsideSettlement(level, outside), "ground outside must not count as settlement");

            var random = level.getRandom();
            for (EntitySpawnReason reason : new EntitySpawnReason[]{EntitySpawnReason.NATURAL, EntitySpawnReason.CHUNK_GENERATION}) {
                check(SpawnPlacements.checkSpawnRules(DWMEntityTypes.THAL, level, reason, inside, random),
                        "Thals should be allowed to spawn with reason " + reason + " inside the settlement");
                check(!SpawnPlacements.checkSpawnRules(DWMEntityTypes.THAL, level, reason, outside, random),
                        "Thals must not spawn with reason " + reason + " outside the settlement");
            }
            check(SpawnPlacements.checkSpawnRules(DWMEntityTypes.THAL, level, EntitySpawnReason.COMMAND, outside, random),
                    "deliberate spawns stay allowed everywhere");
        } catch (RuntimeException | AssertionError e) {
            site.release();
            throw e;
        }
        // Let the residents become visible before discarding them and releasing the chunks.
        context.runAfterDelay(ENTITY_VISIBLE_TICKS, () -> {
            site.release();
            context.succeed();
        });
    }

    private static void assertDoor(Site site, PlannedPiece planned) {
        Cell door = planned.doorCell();
        BlockPos lower = new BlockPos(site.centreX + door.x(), site.floorY + 1, site.centreZ + door.z());
        BlockState lowerState = site.level.getBlockState(lower);
        BlockState upperState = site.level.getBlockState(lower.above());
        check(lowerState.is(Blocks.OAK_DOOR) && lowerState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER,
                planned.kind() + " door missing at " + lower);
        check(upperState.is(Blocks.OAK_DOOR) && upperState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER,
                planned.kind() + " door top missing at " + lower.above());
        // The doorway has a floor to walk in on and walkable space in front of it.
        check(!site.level.getBlockState(lower.below()).isAir(), planned.kind() + " has no floor at its door");
    }

    private static void assertField(Site site, ThalFrame frame) {
        int farmland = 0;
        int crops = 0;
        int water = 0;
        Set<Integer> ages = new HashSet<>();
        for (int x = frame.minX(); x <= frame.maxX(); x++) {
            for (int z = frame.minZ(); z <= frame.maxZ(); z++) {
                BlockPos pos = new BlockPos(x, site.floorY, z);
                BlockState ground = site.level.getBlockState(pos);
                BlockState above = site.level.getBlockState(pos.above());
                if (ground.is(Blocks.FARMLAND)) {
                    farmland++;
                }
                if (ground.is(Blocks.WATER)) {
                    water++;
                }
                if (above.getBlock() instanceof CropBlock crop) {
                    crops++;
                    ages.add(crop.getAge(above));
                }
            }
        }
        check(farmland >= 40, "field has too little farmland: " + farmland);
        check(crops == farmland, "every farmland tile should be planted: " + crops + " crops on " + farmland + " farmland");
        check(water >= 5, "field needs a water channel, found " + water);
        check(ages.size() > 1, "field crops should vary in growth, got " + ages);
        Cell door = frame.doorCell();
        check(site.level.getBlockState(new BlockPos(door.x(), site.floorY, door.z())).is(Blocks.DIRT_PATH),
                "field entrance tile should be a path");
    }

    /** Flood fill over laid DIRT_PATH blocks from the shelter's entrance, 4-connected at floor level. */
    private static Set<BlockPos> floodPaths(Site site) {
        Cell root = site.layout.shelter().entranceCell();
        BlockPos start = new BlockPos(site.centreX + root.x(), site.floorY, site.centreZ + root.z());
        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        reached.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction step : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(step);
                if (site.level.getBlockState(next).is(Blocks.DIRT_PATH) && reached.add(next)) {
                    queue.add(next);
                }
            }
        }
        return reached;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /** A flat plateau with a few deliberate defects, plus the settlement planned and ready to place on it. */
    private static final class Site {
        final ServerLevel level;
        final int centreX;
        final int centreZ;
        final int floorY;
        final ThalSettlementLayout.Layout layout = ThalSettlementLayout.plan(SEED);
        final Structure structure;
        final PiecesContainer container;
        final ChunkPos startChunk;
        final List<ChunkPos> chunks = new ArrayList<>();
        BlockPos pit = BlockPos.ZERO;
        BlockPos fieldBump = BlockPos.ZERO;
        BlockPos pathBump = BlockPos.ZERO;

        private Site(GameTestHelper context, int offset) {
            this.level = context.getLevel();
            BlockPos origin = context.absolutePos(BlockPos.ZERO);
            this.centreX = origin.getX() + offset;
            this.centreZ = origin.getZ() + offset;
            this.floorY = origin.getY() + FLOOR_Y_OFFSET;
            this.structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .getValueOrThrow(ThalSettlementStructure.KEY);
            StructurePiecesBuilder builder = new StructurePiecesBuilder();
            ThalSettlementStructure.buildPieces(builder, layout, centreX, centreZ, floorY);
            this.container = builder.build();
            this.startChunk = ChunkPos.containing(new BlockPos(centreX, floorY, centreZ));

            BoundingBox box = container.calculateBoundingBox();
            for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
                for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                    chunks.add(new ChunkPos(cx, cz));
                }
            }
        }

        static Site prepare(GameTestHelper context, int offset, boolean withDefects) {
            Site site = new Site(context, offset);
            site.loadChunks();
            site.layPlateau();
            if (withDefects) {
                site.addDefects();
            }
            return site;
        }

        private void loadChunks() {
            for (int cx = (centreX - RADIUS) >> 4; cx <= (centreX + RADIUS) >> 4; cx++) {
                for (int cz = (centreZ - RADIUS) >> 4; cz <= (centreZ + RADIUS) >> 4; cz++) {
                    level.setChunkForced(cx, cz, true);
                    level.getChunk(cx, cz);
                }
            }
        }

        void release() {
            for (ThalEntity thal : allThals()) {
                thal.discard();
            }
            for (int cx = (centreX - RADIUS) >> 4; cx <= (centreX + RADIUS) >> 4; cx++) {
                for (int cz = (centreZ - RADIUS) >> 4; cz <= (centreZ + RADIUS) >> 4; cz++) {
                    level.setChunkForced(cx, cz, false);
                }
            }
        }

        /** Stone base, two dirt layers, a grass surface at {@code floorY} and clear air above. */
        private void layPlateau() {
            for (int x = centreX - RADIUS; x <= centreX + RADIUS; x++) {
                for (int z = centreZ - RADIUS; z <= centreZ + RADIUS; z++) {
                    level.setBlock(new BlockPos(x, floorY - 3, z), Blocks.STONE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, floorY - 2, z), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, floorY - 1, z), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(x, floorY, z), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    for (int y = 1; y <= 3; y++) {
                        level.setBlock(new BlockPos(x, floorY + y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        private void addDefects() {
            // A pit under the shelter floor, a hill in a field and a hill on a path.
            ThalFrame shelter = world(layout.shelter());
            pit = new BlockPos(shelter.minX() + 1, floorY, shelter.minZ() + 1);
            level.setBlock(pit, Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pit.below(), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(pit.below(2), Blocks.AIR.defaultBlockState(), 2);

            ThalFrame field = world(layout.ofKind(Kind.FIELD).get(0));
            // On the channel's end post, which is never planted: in a live level the cleared hill's light is
            // stale for a few ticks, and a crop planted there would pop off on its neighbour's shape update.
            // Real worldgen places into a WorldGenRegion (no shape updates, light computed afterwards).
            int channelV = field.depth() / 2;
            fieldBump = new BlockPos(field.worldX(0, channelV), floorY, field.worldZ(0, channelV));
            level.setBlock(fieldBump.above(), Blocks.DIRT.defaultBlockState(), 2);
            level.setBlock(fieldBump.above(2), Blocks.DIRT.defaultBlockState(), 2);

            Cell farthest = layout.pathCells().stream()
                    .max((a, b) -> Integer.compare(Math.abs(a.x()) + Math.abs(a.z()), Math.abs(b.x()) + Math.abs(b.z())))
                    .orElseThrow();
            pathBump = new BlockPos(centreX + farthest.x(), floorY, centreZ + farthest.z());
            level.setBlock(pathBump.above(), Blocks.DIRT.defaultBlockState(), 2);
            level.setBlock(pathBump.above(2), Blocks.DIRT.defaultBlockState(), 2);
        }

        ThalFrame world(PlannedPiece planned) {
            return planned.frame().translated(centreX, centreZ);
        }

        /** Registers the start with the chunks (so structure lookups work) and places it chunk by chunk. */
        void placeAll() {
            placeAll(container);
        }

        void placeAll(PiecesContainer pieces) {
            StructureStart start = new StructureStart(structure, startChunk, 0, pieces);
            for (ChunkPos chunkPos : chunks) {
                ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
                if (chunkPos.equals(startChunk)) {
                    chunk.setStartForStructure(structure, start);
                }
                chunk.addReferenceForStructure(structure, startChunk.pack());
            }
            for (ChunkPos chunkPos : chunks) {
                start.placeInChunk(
                        level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
                        new BoundingBox(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                                chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ()),
                        chunkPos);
            }
        }

        List<ThalEntity> allThals() {
            return level.getEntitiesOfClass(ThalEntity.class, new AABB(
                    centreX - RADIUS, floorY - 4, centreZ - RADIUS, centreX + RADIUS + 1, floorY + 16, centreZ + RADIUS + 1));
        }

        List<ThalEntity> thalsIn(ThalFrame frame) {
            return level.getEntitiesOfClass(ThalEntity.class, new AABB(
                    frame.minX(), floorY, frame.minZ(), frame.maxX() + 1, floorY + 4, frame.maxZ() + 1));
        }
    }
}
