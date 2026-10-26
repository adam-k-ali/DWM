package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.world.DWMBiomeKeys;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The Thal plateau settlement. Code-built (no NBT): {@link ThalSettlementLayout} plans the buildings and paths,
 * this class checks the site is flat, dry and in the Thal Plateau biome, then emits one piece per building plus a
 * path piece, all sharing one floor level so the path network is walkable end to end.
 *
 * <p>Biome membership, rarity and spawn suppression are data ({@code worldgen/structure/thal_settlement.json},
 * {@code worldgen/structure_set/thal_settlement.json}); only the shape lives here.
 */
public class ThalSettlementStructure extends Structure {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "thal_settlement");
    public static final ResourceKey<Structure> KEY = ResourceKey.create(Registries.STRUCTURE, ID);
    public static final MapCodec<ThalSettlementStructure> CODEC = simpleCodec(ThalSettlementStructure::new);

    /** Largest allowed difference between the highest and lowest sampled surface across the settlement. */
    public static final int MAX_HEIGHT_SPREAD = 6;
    /** Largest share of sampled building columns that may be under deep water or lava. */
    public static final double MAX_WET_FRACTION = 0.25;
    /** Liquid up to this deep over a sampled column is built over (the foundation replaces it); deeper counts as wet. */
    public static final int MAX_SHALLOW_LIQUID_DEPTH = 2;
    /** Every n-th path cell is height-sampled (footprints are sampled at their corners and centre). */
    private static final int PATH_SAMPLE_STRIDE = 4;

    public ThalSettlementStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return DWMStructureTypes.THAL_SETTLEMENT;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centreX = chunkPos.getMiddleBlockX();
        int centreZ = chunkPos.getMiddleBlockZ();
        ThalSettlementLayout.Layout layout = ThalSettlementLayout.plan(context.random());

        ChunkGenerator generator = context.chunkGenerator();
        Map<Long, Integer> surface = new HashMap<>();
        List<Integer> heights = new ArrayList<>();
        int wetSamples = 0;
        int footprintSamples = 0;
        boolean centreWet = false;

        for (ThalSettlementLayout.PlannedPiece piece : layout.pieces()) {
            ThalFrame frame = piece.frame();
            int[][] samples = {
                    {frame.minX(), frame.minZ()}, {frame.maxX(), frame.minZ()},
                    {frame.minX(), frame.maxZ()}, {frame.maxX(), frame.maxZ()},
                    {(frame.minX() + frame.maxX()) / 2, (frame.minZ() + frame.maxZ()) / 2}
            };
            for (int[] sample : samples) {
                int x = centreX + sample[0];
                int z = centreZ + sample[1];
                int top = surface.computeIfAbsent(BlockPos.asLong(x, 0, z), key -> generator.getBaseHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()));
                int ground = generator.getBaseHeight(
                        x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
                footprintSamples++;
                wetSamples += top - ground > MAX_SHALLOW_LIQUID_DEPTH ? 1 : 0;
                // Sample 5 (the centre of the first, shelter piece) is the settlement centre and must be dry.
                centreWet |= top != ground && footprintSamples == 5;
                heights.add(top);
            }
        }
        int index = 0;
        for (ThalSettlementLayout.Cell cell : layout.pathCells()) {
            if (index++ % PATH_SAMPLE_STRIDE != 0) {
                continue;
            }
            int x = centreX + cell.x();
            int z = centreZ + cell.z();
            heights.add(surface.computeIfAbsent(BlockPos.asLong(x, 0, z), key -> generator.getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState())));
        }

        if (isTooWet(centreWet, wetSamples, footprintSamples)) {
            return Optional.empty();
        }
        OptionalInt baseY = chooseBaseY(heights);
        if (baseY.isEmpty()) {
            return Optional.empty();
        }
        int floorY = baseY.getAsInt() - 1;
        if (floorY + ThalShelterPiece.HEIGHT >= context.heightAccessor().getMaxY()
                || floorY < context.heightAccessor().getMinY() + 4) {
            return Optional.empty();
        }

        // Belt and braces on top of the structure's biome tag: the centre must be the Thal Plateau itself and
        // every building must stand in a biome the structure allows.
        if (!biomeAt(context, centreX, baseY.getAsInt(), centreZ).is(DWMBiomeKeys.SKARO_THAL_PLATEAU)) {
            return Optional.empty();
        }
        for (ThalSettlementLayout.PlannedPiece piece : layout.pieces()) {
            ThalFrame frame = piece.frame();
            int x = centreX + (frame.minX() + frame.maxX()) / 2;
            int z = centreZ + (frame.minZ() + frame.maxZ()) / 2;
            if (!context.validBiome().test(biomeAt(context, x, baseY.getAsInt(), z))) {
                return Optional.empty();
            }
        }
        return Optional.of(new GenerationStub(
                new BlockPos(centreX, baseY.getAsInt(), centreZ),
                builder -> buildPieces(builder, layout, centreX, centreZ, floorY)));
    }

    private static Holder<Biome> biomeAt(GenerationContext context, int x, int y, int z) {
        return context.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), context.randomState().sampler());
    }

    /**
     * Whether the site is too wet: the settlement centre must be on dry land and at most
     * {@link #MAX_WET_FRACTION} of the sampled building columns may lie under liquid deeper than
     * {@link #MAX_SHALLOW_LIQUID_DEPTH}. Pure for unit testing.
     */
    public static boolean isTooWet(boolean centreWet, int wetSamples, int totalSamples) {
        return centreWet || wetSamples > totalSamples * MAX_WET_FRACTION;
    }

    /**
     * Picks the common surface level (the median sample) or rejects the site when there are no samples or the
     * height spread exceeds {@link #MAX_HEIGHT_SPREAD}. Pure so it can be unit tested.
     */
    public static OptionalInt chooseBaseY(List<Integer> surfaceHeights) {
        if (surfaceHeights.isEmpty()) {
            return OptionalInt.empty();
        }
        List<Integer> sorted = new ArrayList<>(surfaceHeights);
        sorted.sort(Integer::compare);
        int spread = sorted.get(sorted.size() - 1) - sorted.get(0);
        if (spread > MAX_HEIGHT_SPREAD) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(sorted.get(sorted.size() / 2));
    }

    /**
     * Adds the settlement's pieces to {@code builder}: the shelter first (it is the structure's reference piece),
     * then fields, huts and the watch post, then the path network. {@code floorY} is the shared floor layer.
     */
    public static void buildPieces(
            StructurePiecesBuilder builder, ThalSettlementLayout.Layout layout, int centreX, int centreZ, int floorY
    ) {
        for (ThalSettlementLayout.PlannedPiece planned : layout.pieces()) {
            ThalFrame frame = planned.frame().translated(centreX, centreZ);
            ThalSettlementPiece piece = switch (planned.kind()) {
                case SHELTER -> new ThalShelterPiece(frame, floorY, planned.residents());
                case HUT -> new ThalHutPiece(frame, floorY, planned.residents());
                case FIELD -> new ThalFieldPiece(frame, floorY);
                case WATCH_POST -> new ThalWatchPostPiece(frame, floorY);
            };
            piece.setSettlementCentre(centreX, centreZ);
            builder.addPiece(piece);
        }
        List<ThalSettlementLayout.Cell> worldCells = new ArrayList<>();
        for (ThalSettlementLayout.Cell cell : layout.pathCells()) {
            worldCells.add(new ThalSettlementLayout.Cell(centreX + cell.x(), centreZ + cell.z()));
        }
        builder.addPiece(new ThalPathPiece(worldCells, floorY));
    }
}
