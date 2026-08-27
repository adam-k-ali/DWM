package com.adamkali.dwm.tardis.portal;

import com.adamkali.dwm.MinecraftTestBootstrap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalSamplerTest {
    private static PortalSampler SAMPLER;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        SAMPLER = new PortalSampler(
                3, 4, 5,
                new TicketType(80, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION)
        ) {
            @Override
            public boolean isVisible(BlockState state) {
                return state != null && !state.isAir() && !state.is(Blocks.LIGHT);
            }
        };
    }

    @Test
    void filterVisible_keepsOnlyVisibleBlocks() {
        Map<BlockPos, BlockState> input = new HashMap<>();
        input.put(new BlockPos(0, 0, 0), Blocks.STONE.defaultBlockState());
        input.put(new BlockPos(1, 0, 0), Blocks.AIR.defaultBlockState());
        input.put(new BlockPos(2, 0, 0), Blocks.LIGHT.defaultBlockState());
        input.put(new BlockPos(3, 0, 0), Blocks.CHEST.defaultBlockState());

        Map<BlockPos, BlockState> visible = SAMPLER.filterVisibleBlocks(input);

        assertEquals(2, visible.size());
        assertTrue(visible.containsKey(new BlockPos(0, 0, 0)));
        assertTrue(visible.containsKey(new BlockPos(3, 0, 0)));
    }

    @Test
    void isInsideFootprint_respectsConstructorSizes() {
        BlockPos origin = new BlockPos(10, 20, 30);

        assertTrue(SAMPLER.inFootprint(origin, origin));
        assertTrue(SAMPLER.inFootprint(origin.offset(2, 3, 4), origin));
        assertFalse(SAMPLER.inFootprint(origin.offset(3, 0, 0), origin));
        assertFalse(SAMPLER.inFootprint(origin.offset(0, 4, 0), origin));
        assertFalse(SAMPLER.inFootprint(origin.offset(0, 0, 5), origin));
        assertFalse(SAMPLER.inFootprint(origin.offset(0, -1, 0), origin));
    }

    @Test
    void sampleAtmosphere_nullInputsReturnDefault() {
        assertSame(PortalAtmosphere.DEFAULT, PortalSampler.sampleAtmosphere(null, BlockPos.ZERO));
        assertSame(PortalAtmosphere.DEFAULT, PortalSampler.sampleAtmosphere(null, null));
    }
}
