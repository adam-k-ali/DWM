package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

/**
 * Gallifrey material rules: deep-red grass / dirt / sand / orange sand tops over Gallifrey stone.
 */
public final class GallifreySurfaceRules {
    private static final MaterialCondition ON_FLOOR = MaterialRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR);
    private static final MaterialCondition UNDER_FLOOR = MaterialRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR);
    private static final MaterialCondition ON_CEILING = MaterialRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING);

    private static final MaterialRule BEDROCK = block(Blocks.BEDROCK);
    private static final MaterialRule GALLIFREY_STONE = block(DWMBlocks.GALLIFREY_STONE);
    private static final MaterialRule GALLIFREY_DIRT = block(DWMBlocks.GALLIFREY_DIRT);
    private static final MaterialRule GALLIFREY_COARSE_DIRT = block(DWMBlocks.GALLIFREY_COARSE_DIRT);
    private static final MaterialRule GALLIFREY_GRASS_BLOCK = block(DWMBlocks.GALLIFREY_GRASS_BLOCK);
    private static final MaterialRule GALLIFREY_SAND = block(DWMBlocks.GALLIFREY_SAND);
    private static final MaterialRule GALLIFREY_SANDSTONE = block(DWMBlocks.GALLIFREY_SANDSTONE);
    private static final MaterialRule ORANGE_SAND = block(DWMBlocks.ORANGE_SAND);
    private static final MaterialRule ORANGE_SANDSTONE = block(DWMBlocks.ORANGE_SANDSTONE);

    private GallifreySurfaceRules() {
    }

    public static MaterialRule create(HolderGetter<Biome> biomes) {
        MaterialCondition isWastes = MaterialRules.isBiome(biomes, DWMBiomeKeys.GALLIFREY_WASTES);
        MaterialCondition isBadlands = MaterialRules.isBiome(biomes, DWMBiomeKeys.GALLIFREY_BADLANDS);
        MaterialCondition atOrAboveWater = MaterialRules.waterBlockCheck(-1, 0);
        MaterialCondition aboveWater = MaterialRules.waterBlockCheck(0, 0);

        MaterialRule wastesTop = MaterialRules.sequence(
                MaterialRules.ifTrue(ON_CEILING, GALLIFREY_SANDSTONE),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_SAND
        );

        MaterialRule badlandsTop = MaterialRules.sequence(
                MaterialRules.ifTrue(ON_CEILING, ORANGE_SANDSTONE),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        GALLIFREY_COARSE_DIRT
                ),
                ORANGE_SAND
        );

        MaterialRule dirtTop = MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, 0.45, 1.0),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_GRASS_BLOCK
        );

        MaterialRule floorSurface = MaterialRules.sequence(
                MaterialRules.ifTrue(isWastes, wastesTop),
                MaterialRules.ifTrue(isBadlands, badlandsTop),
                dirtTop
        );

        MaterialRule underSurface = MaterialRules.sequence(
                MaterialRules.ifTrue(isWastes, MaterialRules.sequence(
                        MaterialRules.ifTrue(ON_CEILING, GALLIFREY_SANDSTONE),
                        GALLIFREY_SAND
                )),
                MaterialRules.ifTrue(isBadlands, MaterialRules.sequence(
                        MaterialRules.ifTrue(ON_CEILING, ORANGE_SANDSTONE),
                        ORANGE_SAND
                )),
                GALLIFREY_DIRT
        );

        MaterialRule surfaceBlock = MaterialRules.ifTrue(
                MaterialRules.abovePreliminarySurface(),
                MaterialRules.sequence(
                        MaterialRules.ifTrue(
                                ON_FLOOR,
                                MaterialRules.ifTrue(atOrAboveWater, floorSurface)
                        ),
                        MaterialRules.ifTrue(
                                UNDER_FLOOR,
                                MaterialRules.ifTrue(aboveWater, underSurface)
                        )
                )
        );

        return MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.verticalGradient("dwm:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                        BEDROCK
                ),
                surfaceBlock,
                GALLIFREY_STONE
        );
    }

    private static MaterialRule block(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }
}
