package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
                DWMBlocks.FIRST_DOCTOR_CONSOLE.getDefaultState()));
    }

    @Test
    void isPlayerBreakDenied_falseForOtherBlocks() {
        assertFalse(FirstDoctorConsoleBlock.isPlayerBreakDenied(Blocks.STONE.getDefaultState()));
        assertFalse(FirstDoctorConsoleBlock.isPlayerBreakDenied(
                DWMBlocks.WHITE_TARDIS_WALL.getDefaultState()));
    }
}
