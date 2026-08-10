package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/**
 * Gallifrey surface rules: dirt/sand tops over Gallifrey stone (no grass block yet).
 */
public final class GallifreySurfaceRules {
    private static final SurfaceRules.RuleSource BEDROCK = block(Blocks.BEDROCK);
    private static final SurfaceRules.RuleSource GALLIFREY_STONE = block(DWMBlocks.GALLIFREY_STONE);
    private static final SurfaceRules.RuleSource GALLIFREY_DIRT = block(DWMBlocks.GALLIFREY_DIRT);
    private static final SurfaceRules.RuleSource GALLIFREY_COARSE_DIRT = block(DWMBlocks.GALLIFREY_COARSE_DIRT);
    private static final SurfaceRules.RuleSource GALLIFREY_SAND = block(DWMBlocks.GALLIFREY_SAND);
    private static final SurfaceRules.RuleSource GALLIFREY_SANDSTONE = block(DWMBlocks.GALLIFREY_SANDSTONE);

    private GallifreySurfaceRules() {
    }

    public static SurfaceRules.RuleSource create() {
        SurfaceRules.ConditionSource isWastes = SurfaceRules.isBiome(DWMBiomeKeys.GALLIFREY_WASTES);
        SurfaceRules.ConditionSource atOrAboveWater = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource aboveWater = SurfaceRules.waterBlockCheck(0, 0);

        SurfaceRules.RuleSource wastesTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, GALLIFREY_SANDSTONE),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(Noises.SURFACE, -0.95, -0.4),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_SAND
        );

        SurfaceRules.RuleSource dirtTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(Noises.SURFACE, 0.45, 1.0),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_DIRT
        );

        SurfaceRules.RuleSource floorSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isWastes, wastesTop),
                dirtTop
        );

        SurfaceRules.RuleSource underSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isWastes, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, GALLIFREY_SANDSTONE),
                        GALLIFREY_SAND
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
