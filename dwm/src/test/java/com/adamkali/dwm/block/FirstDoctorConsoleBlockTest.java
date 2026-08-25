package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstDoctorConsoleBlockTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void isPlayerBreakDenied_trueForConsole() {
        assertTrue(FirstDoctorConsoleBlock.isPlayerBreakDenied(
                DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()));
    }

    @Test
    void isPlayerBreakDenied_falseForOtherBlocks() {
        assertFalse(FirstDoctorConsoleBlock.isPlayerBreakDenied(Blocks.STONE.defaultBlockState()));
        assertFalse(FirstDoctorConsoleBlock.isPlayerBreakDenied(
                DWMBlocks.WHITE_TARDIS_WALL.defaultBlockState()));
    }

    @Test
    void emitsLightLevel15() {
        assertEquals(15, DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState().getLightEmission());
    }
}
