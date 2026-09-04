package com.adamkali.dwm.world;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/**
 * Skaro surface rules: vanilla-only DWM-064 palette, distinct per biome.
 * Petrified wood is reserved for flora placement (DWM-068).
 */
public final class SkaroSurfaceRules {
    private static final SurfaceRules.RuleSource BEDROCK = block(Blocks.BEDROCK);
    private static final SurfaceRules.RuleSource STONE = block(Blocks.STONE);
    private static final SurfaceRules.RuleSource TUFF = block(Blocks.TUFF);
    private static final SurfaceRules.RuleSource GRAVEL = block(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SAND = block(Blocks.SAND);
    private static final SurfaceRules.RuleSource RED_SAND = block(Blocks.RED_SAND);
    private static final SurfaceRules.RuleSource TERRACOTTA = block(Blocks.TERRACOTTA);
    private static final SurfaceRules.RuleSource DIRT = block(Blocks.DIRT);
    private static final SurfaceRules.RuleSource COARSE_DIRT = block(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource ROOTED_DIRT = block(Blocks.ROOTED_DIRT);
    private static final SurfaceRules.RuleSource MUD = block(Blocks.MUD);
    private static final SurfaceRules.RuleSource PODZOL = block(Blocks.PODZOL);

    private SkaroSurfaceRules() {
    }

    public static SurfaceRules.RuleSource create(HolderGetter<Biome> biomes) {
        SurfaceRules.ConditionSource isWastes = SurfaceRules.isBiome(biomes, DWMBiomeKeys.SKARO_IRRADIATED_WASTES);
        SurfaceRules.ConditionSource isJungle = SurfaceRules.isBiome(biomes, DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE);
        SurfaceRules.ConditionSource isMire = SurfaceRules.isBiome(biomes, DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE);
        SurfaceRules.ConditionSource isMountains = SurfaceRules.isBiome(biomes, DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS);
        SurfaceRules.ConditionSource isPlateau = SurfaceRules.isBiome(biomes, DWMBiomeKeys.SKARO_THAL_PLATEAU);
        SurfaceRules.ConditionSource atOrAboveWater = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource aboveWater = SurfaceRules.waterBlockCheck(0, 0);

        SurfaceRules.RuleSource wastesTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, TERRACOTTA),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.35),
                        TERRACOTTA
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, 0.35, 1.0),
                        RED_SAND
                ),
                SAND
        );

        SurfaceRules.RuleSource jungleTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        ROOTED_DIRT
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, 0.45, 1.0),
                        COARSE_DIRT
                ),
                PODZOL
        );

        SurfaceRules.RuleSource mireTop = MUD;

        SurfaceRules.RuleSource mountainsTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.35),
                        GRAVEL
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, 0.4, 1.0),
                        TUFF
                ),
                STONE
        );

        SurfaceRules.RuleSource plateauTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, -0.95, -0.4),
                        TERRACOTTA
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition2d(Noises.SURFACE, 0.4, 1.0),
                        COARSE_DIRT
                ),
                DIRT
        );

        SurfaceRules.RuleSource floorSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isWastes, wastesTop),
                SurfaceRules.ifTrue(isJungle, jungleTop),
                SurfaceRules.ifTrue(isMire, mireTop),
                SurfaceRules.ifTrue(isMountains, mountainsTop),
                SurfaceRules.ifTrue(isPlateau, plateauTop),
                DIRT
        );

        SurfaceRules.RuleSource underSurface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(isWastes, SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, TERRACOTTA),
                        SAND
                )),
                SurfaceRules.ifTrue(isJungle, DIRT),
                SurfaceRules.ifTrue(isMire, DIRT),
                SurfaceRules.ifTrue(isMountains, TUFF),
                SurfaceRules.ifTrue(isPlateau, DIRT),
                DIRT
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
                STONE
        );
    }

    private static SurfaceRules.RuleSource block(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
