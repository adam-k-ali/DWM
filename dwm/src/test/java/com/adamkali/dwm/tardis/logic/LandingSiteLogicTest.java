package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

class LandingSiteLogicTest {
    private static final Direction DOOR = Direction.NORTH;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void parseBiome_acceptsValidId() {
        Optional<ResourceKey<Biome>> key = LandingSiteLogic.parseBiome("minecraft:plains");
        assertTrue(key.isPresent());
        assertEquals(ResourceKey.create(Registries.BIOME, Identifier.parse("minecraft:plains")), key.get());
    }

    @Test
    void parseBiome_rejectsNullBlankAndInvalid() {
        assertTrue(LandingSiteLogic.parseBiome(null).isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("").isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("   ").isEmpty());
        assertTrue(LandingSiteLogic.parseBiome("not a biome").isEmpty());
    }

    @Test
    void ceilingExclusiveY_isMinYPlusLogicalHeightWhenHasCeiling() {
        assertEquals(128, LandingSiteLogic.ceilingExclusiveY(true, 0, 128).orElseThrow());
        assertEquals(320, LandingSiteLogic.ceilingExclusiveY(true, -64, 384).orElseThrow());
    }

    @Test
    void ceilingExclusiveY_emptyWhenNoCeilingOrNullWorld() {
        assertTrue(LandingSiteLogic.ceilingExclusiveY(false, 0, 128).isEmpty());
        assertTrue(LandingSiteLogic.ceilingExclusiveY(null).isEmpty());
    }

    @Test
    void findSurfaceInColumn_netherLikeSkipsBedrockCeiling() {
        LevelReader world = column(0, 128, y -> {
            if (y >= 123 && y <= 127) {
                return Blocks.BEDROCK.defaultBlockState();
            }
            if (y == 69) {
                return Blocks.NETHERRACK.defaultBlockState();
            }
            if (y >= 72 && y <= 122) {
                return Blocks.NETHERRACK.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        });

        Optional<BlockPos> landing = LandingSiteLogic.findSurfaceInColumn(
                world, 8, 8, DOOR, OptionalInt.of(128));

        assertEquals(new BlockPos(8, 70, 8), landing.orElseThrow());
    }

    @Test
    void findSurfaceInColumn_overworldUsesHeightmapNotCaveBelow() {
        LevelReader world = column(-64, 64, y -> {
            if (y == 63) {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            if (y == 39) {
                return Blocks.STONE.defaultBlockState();
            }
            if (y < 63 && y != 39 && y != 40 && y != 41) {
                return Blocks.STONE.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        });

        Optional<BlockPos> landing = LandingSiteLogic.findSurfaceInColumn(
                world, 0, 0, DOOR, OptionalInt.empty());

        assertEquals(new BlockPos(0, 64, 0), landing.orElseThrow());
    }

    @Test
    void findSurfaceInColumn_hasCeilingButHeightmapBelowCapUsesHeightmap() {
        LevelReader world = column(0, 15, y -> {
            if (y == 14) {
                return Blocks.STONE.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        });

        Optional<BlockPos> landing = LandingSiteLogic.findSurfaceInColumn(
                world, 4, 4, DOOR, OptionalInt.of(256));

        assertEquals(new BlockPos(4, 15, 4), landing.orElseThrow());
    }

    @Test
    void findSurfaceInColumn_lavaSeaUnderCeilingIsNotALanding() {
        LevelReader world = column(0, 128, y -> {
            if (y >= 123 && y <= 127) {
                return Blocks.BEDROCK.defaultBlockState();
            }
            if (y <= 31) {
                return Blocks.NETHERRACK.defaultBlockState();
            }
            return Blocks.LAVA.defaultBlockState();
        });

        assertTrue(LandingSiteLogic.findSurfaceInColumn(
                world, 3, 3, DOOR, OptionalInt.of(128)).isEmpty());
    }

    @Test
    void isValidLanding_acceptsNetherRoofForExactCoords() {
        LevelReader world = column(0, 128, y -> {
            if (y <= 127) {
                return Blocks.BEDROCK.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        });

        assertTrue(LandingSiteLogic.isValidLanding(world, new BlockPos(0, 128, 0), DOOR));
    }

    private static LevelReader column(int minY, int heightmapY, IntFunction<BlockState> atY) {
        LevelReader world = Mockito.mock(LevelReader.class);
        Mockito.when(world.getMinY()).thenReturn(minY);
        Mockito.when(world.getHeight(eq(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), anyInt(), anyInt()))
                .thenReturn(heightmapY);
        Mockito.when(world.isOutsideBuildHeight(any(BlockPos.class))).thenReturn(false);
        Mockito.when(world.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos pos = invocation.getArgument(0);
            return atY.apply(pos.getY());
        });
        return world;
    }
}
