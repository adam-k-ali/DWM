package com.adamkali.dwm.world;

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
 * Skaro material rules: vanilla-only DWM-064 palette, distinct per biome.
 * Petrified wood is reserved for flora placement (DWM-068).
 */
public final class SkaroSurfaceRules {
    private static final MaterialCondition ON_FLOOR = MaterialRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR);
    private static final MaterialCondition UNDER_FLOOR = MaterialRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR);
    private static final MaterialCondition ON_CEILING = MaterialRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING);

    private static final MaterialRule BEDROCK = block(Blocks.BEDROCK);
    private static final MaterialRule STONE = block(Blocks.STONE);
    private static final MaterialRule TUFF = block(Blocks.TUFF);
    private static final MaterialRule GRAVEL = block(Blocks.GRAVEL);
    private static final MaterialRule SAND = block(Blocks.SAND);
    private static final MaterialRule RED_SAND = block(Blocks.RED_SAND);
    private static final MaterialRule TERRACOTTA = block(Blocks.TERRACOTTA);
    private static final MaterialRule DIRT = block(Blocks.DIRT);
    private static final MaterialRule COARSE_DIRT = block(Blocks.COARSE_DIRT);
    private static final MaterialRule ROOTED_DIRT = block(Blocks.ROOTED_DIRT);
    private static final MaterialRule MUD = block(Blocks.MUD);
    private static final MaterialRule PODZOL = block(Blocks.PODZOL);

    private SkaroSurfaceRules() {
    }

    public static MaterialRule create(HolderGetter<Biome> biomes) {
        MaterialCondition isWastes = MaterialRules.isBiome(biomes, DWMBiomeKeys.SKARO_IRRADIATED_WASTES);
        MaterialCondition isJungle = MaterialRules.isBiome(biomes, DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE);
        MaterialCondition isMire = MaterialRules.isBiome(biomes, DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE);
        MaterialCondition isMountains = MaterialRules.isBiome(biomes, DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS);
        MaterialCondition isPlateau = MaterialRules.isBiome(biomes, DWMBiomeKeys.SKARO_THAL_PLATEAU);
        MaterialCondition atOrAboveWater = MaterialRules.waterBlockCheck(-1, 0);
        MaterialCondition aboveWater = MaterialRules.waterBlockCheck(0, 0);

        MaterialRule wastesTop = MaterialRules.sequence(
                MaterialRules.ifTrue(ON_CEILING, TERRACOTTA),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.35),
                        TERRACOTTA
                ),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, 0.35, 1.0),
                        RED_SAND
                ),
                SAND
        );

        MaterialRule jungleTop = MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        ROOTED_DIRT
                ),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, 0.45, 1.0),
                        COARSE_DIRT
                ),
                PODZOL
        );

        MaterialRule mireTop = MUD;

        MaterialRule mountainsTop = MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.35),
                        GRAVEL
                ),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, 0.4, 1.0),
                        TUFF
                ),
                STONE
        );

        MaterialRule plateauTop = MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        TERRACOTTA
                ),
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(Noises.SURFACE, 0.4, 1.0),
                        COARSE_DIRT
                ),
                DIRT
        );

        MaterialRule floorSurface = MaterialRules.sequence(
                MaterialRules.ifTrue(isWastes, wastesTop),
                MaterialRules.ifTrue(isJungle, jungleTop),
                MaterialRules.ifTrue(isMire, mireTop),
                MaterialRules.ifTrue(isMountains, mountainsTop),
                MaterialRules.ifTrue(isPlateau, plateauTop),
                DIRT
        );

        MaterialRule underSurface = MaterialRules.sequence(
                MaterialRules.ifTrue(isWastes, MaterialRules.sequence(
                        MaterialRules.ifTrue(ON_CEILING, TERRACOTTA),
                        SAND
                )),
                MaterialRules.ifTrue(isJungle, DIRT),
                MaterialRules.ifTrue(isMire, DIRT),
                MaterialRules.ifTrue(isMountains, TUFF),
                MaterialRules.ifTrue(isPlateau, DIRT),
                DIRT
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
                STONE
        );
    }

    private static MaterialRule block(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }
}
