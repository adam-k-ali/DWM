package com.adamkali.dwm.world;

import com.adamkali.dwm.block.DWMBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

/**
 * Gallifrey surface rules: dirt/sand tops over Gallifrey stone (no grass block yet).
 */
public final class GallifreySurfaceRules {
    private static final MaterialRules.MaterialRule BEDROCK = block(Blocks.BEDROCK);
    private static final MaterialRules.MaterialRule GALLIFREY_STONE = block(DWMBlocks.GALLIFREY_STONE);
    private static final MaterialRules.MaterialRule GALLIFREY_DIRT = block(DWMBlocks.GALLIFREY_DIRT);
    private static final MaterialRules.MaterialRule GALLIFREY_COARSE_DIRT = block(DWMBlocks.GALLIFREY_COARSE_DIRT);
    private static final MaterialRules.MaterialRule GALLIFREY_SAND = block(DWMBlocks.GALLIFREY_SAND);
    private static final MaterialRules.MaterialRule GALLIFREY_SANDSTONE = block(DWMBlocks.GALLIFREY_SANDSTONE);

    private GallifreySurfaceRules() {
    }

    public static MaterialRules.MaterialRule create() {
        MaterialRules.MaterialCondition isWastes = MaterialRules.biome(DWMBiomeKeys.GALLIFREY_WASTES);
        MaterialRules.MaterialCondition atOrAboveWater = MaterialRules.water(-1, 0);
        MaterialRules.MaterialCondition aboveWater = MaterialRules.water(0, 0);

        MaterialRules.MaterialRule wastesTop = MaterialRules.sequence(
                MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING, GALLIFREY_SANDSTONE),
                MaterialRules.condition(
                        MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.95, -0.4),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_SAND
        );

        MaterialRules.MaterialRule dirtTop = MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, 0.45, 1.0),
                        GALLIFREY_COARSE_DIRT
                ),
                GALLIFREY_DIRT
        );

        MaterialRules.MaterialRule floorSurface = MaterialRules.sequence(
                MaterialRules.condition(isWastes, wastesTop),
                dirtTop
        );

        MaterialRules.MaterialRule underSurface = MaterialRules.sequence(
                MaterialRules.condition(isWastes, MaterialRules.sequence(
                        MaterialRules.condition(MaterialRules.STONE_DEPTH_CEILING, GALLIFREY_SANDSTONE),
                        GALLIFREY_SAND
                )),
                GALLIFREY_DIRT
        );

        MaterialRules.MaterialRule surfaceBlock = MaterialRules.condition(
                MaterialRules.surface(),
                MaterialRules.sequence(
                        MaterialRules.condition(
                                MaterialRules.STONE_DEPTH_FLOOR,
                                MaterialRules.condition(atOrAboveWater, floorSurface)
                        ),
                        MaterialRules.condition(
                                MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH,
                                MaterialRules.condition(aboveWater, underSurface)
                        )
                )
        );

        return MaterialRules.sequence(
                MaterialRules.condition(
                        MaterialRules.verticalGradient("dwm:bedrock_floor", YOffset.getBottom(), YOffset.aboveBottom(5)),
                        BEDROCK
                ),
                surfaceBlock,
                GALLIFREY_STONE
        );
    }

    private static MaterialRules.MaterialRule block(Block block) {
        return MaterialRules.block(block.getDefaultState());
    }
}
