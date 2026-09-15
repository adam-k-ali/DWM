package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.blueprint.model.BlueprintBlock;
import com.adamkali.dwm.blueprint.model.BlueprintBlockState;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintBlockResolverTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void resolve_stone_defaultState() {
        Optional<BlockState> state = BlueprintBlockResolver.resolve(new BlueprintBlock("minecraft:stone"));
        assertTrue(state.isPresent());
        assertEquals(Blocks.STONE.defaultBlockState(), state.get());
    }

    @Test
    void resolve_facingAppliedWhenSupported() {
        // Dispenser has FACING including all 6 directions.
        Optional<BlockState> state = BlueprintBlockResolver.resolve(
                new BlueprintBlock("minecraft:dispenser", new BlueprintBlockState("north"))
        );
        assertTrue(state.isPresent());
        assertEquals(Direction.NORTH, state.get().getValue(BlockStateProperties.FACING));
    }

    @Test
    void resolve_facingIgnoredWhenBlockHasNoFacing() {
        Optional<BlockState> state = BlueprintBlockResolver.resolve(
                new BlueprintBlock("minecraft:stone", new BlueprintBlockState("north"))
        );
        assertTrue(state.isPresent());
        assertEquals(Blocks.STONE.defaultBlockState(), state.get());
    }

    @Test
    void resolve_unknownId_empty() {
        assertTrue(BlueprintBlockResolver.resolve(new BlueprintBlock("minecraft:not_a_real_block_xyz")).isEmpty());
    }

    @Test
    void resolve_malformedId_empty() {
        assertTrue(BlueprintBlockResolver.resolve(new BlueprintBlock("not valid!!")).isEmpty());
    }

    @Test
    void resolve_oakLog_axisNotFacing_ignoredGracefully() {
        // Oak log uses AXIS, not FACING — facing should be ignored.
        Optional<BlockState> state = BlueprintBlockResolver.resolve(
                new BlueprintBlock("minecraft:oak_log", new BlueprintBlockState("north"))
        );
        assertTrue(state.isPresent());
        assertEquals(Blocks.OAK_LOG.defaultBlockState(), state.get());
        assertTrue(state.get().hasProperty(RotatedPillarBlock.AXIS));
    }
}
