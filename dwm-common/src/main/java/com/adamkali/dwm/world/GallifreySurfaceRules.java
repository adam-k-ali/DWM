package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/**
 * Gallifrey surface rules: deep-red grass / dirt / sand / orange sand tops over Gallifrey stone.
 */
public final class GallifreySurfaceRules {
    private static final SurfaceRules.RuleSource BEDROCK = block(Blocks.BEDROCK);
    private static final SurfaceRules.RuleSource GALLIFREY_STONE = block(DWMBlocks.GALLIFREY_STONE);
    private static final SurfaceRules.RuleSource GALLIFREY_DIRT = block(DWMBlocks.GALLIFREY_DIRT);
    private static final SurfaceRules.RuleSource GALLIFREY_COARSE_DIRT = block(DWMBlocks.GALLIFREY_COARSE_DIRT);
    private static final SurfaceRules.RuleSource GALLIFREY_GRASS_BLOCK = block(DWMBlocks.GALLIFREY_GRASS_BLOCK);
    private static final SurfaceRules.RuleSource GALLIFREY_SAND = block(DWMBlocks.GALLIFREY_SAND);
    private static final SurfaceRules.RuleSource GALLIFREY_SANDSTONE = block(DWMBlocks.GALLIFREY_SANDSTONE);
    private static final SurfaceRules.RuleSource ORANGE_SAND = block(DWMBlocks.ORANGE_SAND);
    private static final SurfaceRules.RuleSource ORANGE_SANDSTONE = block(DWMBlocks.ORANGE_SANDSTONE);

    private GallifreySurfaceRules() {
    }

    public static SurfaceRules.RuleSource create(HolderGetter<Biome> biomes) {
        SurfaceRules.ConditionSource isWastes = SurfaceRules.isBiome(biomes, DWMBiomeKeys.GALLIFREY_WASTES);
        SurfaceRules.ConditionSource isBadlands = SurfaceRules.isBiome(biomes, DWMBiomeKeys.GALLIFREY_BADLANDS);
        SurfaceRules.ConditionSource atOrAboveWater = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource aboveWater = SurfaceRules.waterBlockCheck(0, 0);

        SurfaceRules.RuleSource wastesTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, GALLIFREY_SANDSTONE),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_SAND
        );

        SurfaceRules.RuleSource badlandsTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, ORANGE_SANDSTONE),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        GALLIFREY_COARSE_DIRT
                ),
                ORANGE_SAND
        );

        SurfaceRules.RuleSource dirtTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, 0.45, 1.0),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_GRASS_BLOCK
        );

        SurfaceRules.RuleSource floorSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isWastes, wastesTop),
                SurfaceRules.ifTrue(isBadlands, badlandsTop),
                dirtTop
        );

        SurfaceRules.RuleSource underSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isWastes, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, GALLIFREY_SANDSTONE),
                        GALLIFREY_SAND
                )),
                SurfaceRules.ifTrue(isBadlands, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, ORANGE_SANDSTONE),
                        ORANGE_SAND
                )),
                GALLIFREY_DIRT
        );

        SurfaceRules.RuleSource surfaceBlock = SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.ifTrue(atOrAboveWater, floorSurface)
                        ),
                        SurfaceRules.ifTrue(
                                SurfaceRules.UNDER_FLOOR,
                                SurfaceRules.ifTrue(aboveWater, underSurface)
                        )
                )
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("dwm:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                        BEDROCK
                ),
                surfaceBlock,
                GALLIFREY_STONE
        );
    }

    private static SurfaceRules.RuleSource block(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
