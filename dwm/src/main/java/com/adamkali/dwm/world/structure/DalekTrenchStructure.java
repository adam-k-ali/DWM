package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.DWMReference;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.List;
import java.util.Optional;
import java.util.function.IntBinaryOperator;

/**
 * The Dalek battlefield trench: a plain jigsaw assembly (pools, start height, piece rules all stay data in
 * {@code worldgen/structure/dalek_trench*.json}) that is rejected when any piece would sit on liquid or leave the
 * structure's biome tag. Vanilla only checks the start point's biome and never checks liquid, so a trench would
 * flood in lakes and overhang into neighbouring biomes. {@link JigsawStructure} is final, so this wraps it.
 */
public class DalekTrenchStructure extends Structure {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek_trench");
    public static final ResourceKey<Structure> KEY = ResourceKey.create(Registries.STRUCTURE, ID);
    /** Same JSON shape as {@code minecraft:jigsaw}, so the structure files only change their {@code type}. */
    public static final MapCodec<DalekTrenchStructure> CODEC =
            JigsawStructure.CODEC.xmap(DalekTrenchStructure::new, structure -> structure.jigsaw);

    private final JigsawStructure jigsaw;

    public DalekTrenchStructure(JigsawStructure jigsaw) {
        super(new StructureSettings(jigsaw.biomes(), jigsaw.spawnOverrides(), jigsaw.step(), jigsaw.terrainAdaptation()));
        this.jigsaw = jigsaw;
    }

    @Override
    public StructureType<?> type() {
        return DWMStructureTypes.DALEK_TRENCH;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<GenerationStub> stub = jigsaw.findGenerationPoint(context);
        if (stub.isEmpty()) {
            return stub;
        }
        // Run the (deterministic, one-shot) assembly now and hand the same builder on, so what is checked is what is built.
        StructurePiecesBuilder builder = stub.get().getPiecesBuilder();
        List<BoundingBox> boxes = builder.build().pieces().stream().map(StructurePiece::getBoundingBox).toList();
        boolean fits = isSiteValid(
                boxes,
                (x, z) -> context.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()),
                (x, z) -> context.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState()),
                (x, y, z) -> context.validBiome().test(context.biomeSource().getNoiseBiome(
                        QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), context.randomState().sampler())));
        if (!fits) {
            return Optional.empty();
        }
        return Optional.of(new GenerationStub(stub.get().position(), Either.right(builder)));
    }

    /** Whether the block column at {@code (x, y, z)} lies in a biome the structure may generate in. */
    @FunctionalInterface
    public interface BiomeCheck {
        boolean isValid(int x, int y, int z);
    }

    /**
     * Accepts the trench only if, at each piece's four corners and centre, the surface is dry (the water-counting
     * surface height equals the ocean-floor height: no tolerance, a flooded lane is a failed trench) and the biome is
     * valid. Pure so it can be unit tested.
     */
    public static boolean isSiteValid(
            List<BoundingBox> pieces, IntBinaryOperator surfaceHeight, IntBinaryOperator floorHeight, BiomeCheck biome
    ) {
        for (BoundingBox box : pieces) {
            int centreX = (box.minX() + box.maxX()) / 2;
            int centreZ = (box.minZ() + box.maxZ()) / 2;
            int[][] columns = {
                    {box.minX(), box.minZ()}, {box.maxX(), box.minZ()},
                    {box.minX(), box.maxZ()}, {box.maxX(), box.maxZ()},
                    {centreX, centreZ}
            };
            int y = (box.minY() + box.maxY()) / 2;
            for (int[] column : columns) {
                if (surfaceHeight.applyAsInt(column[0], column[1]) != floorHeight.applyAsInt(column[0], column[1])
                        || !biome.isValid(column[0], y, column[1])) {
                    return false;
                }
            }
        }
        return true;
    }
}
