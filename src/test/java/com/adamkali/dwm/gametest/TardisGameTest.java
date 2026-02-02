package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import net.minecraft.util.WorldSavePath;
import net.minecraft.world.GameMode;

import java.util.UUID;

public class TardisGameTest implements FabricGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testTardisDoorOpen(TestContext context) {
        // Ensure TardisDataLoader has a save directory
        if (TardisDataLoader.tardisSaveDirectory == null) {
            TardisDataLoader.tardisSaveDirectory = context.getWorld().getServer().getSavePath(WorldSavePath.ROOT).resolve("tardis_data");
        }

        BlockPos pos = new BlockPos(0, 1, 0);
        context.setBlockState(pos, DWMBlocks.TARDIS_BLOCK.getDefaultState());

        TardisBlockEntity be = (TardisBlockEntity) context.getBlockEntity(pos);
        // Force NBT write/read to initialize tardisId if it's null (it usually is until first save)
        // or just rely on the fact that writeNbt initializes it if we can access it.
        // Actually be.getTardisId() might return null if not initialized.
        UUID tardisId = be.getTardisId();
        if (tardisId == null) {
            // Placeholders or trigger NBT
            context.getWorld().getBlockEntity(pos).createNbt(context.getWorld().getRegistryManager());
            tardisId = be.getTardisId();
        }

        // Verify initial state
        TardisDoorState doorState = TardisLogic.getDoorState(tardisId);
        context.assertTrue(doorState != null, "Tardis door state should not be null");
        context.assertFalse(doorState.isOpen, "Tardis door should be closed initially");

        // Simulate right click
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        player.setSneaking(false);
        
        // We use context.useBlock because it simulates the player interaction correctly on server side
        context.useBlock(pos, player);

        // Verify final state
        doorState = TardisLogic.getDoorState(tardisId);
        context.assertTrue(doorState.isOpen, "Tardis door should be open after click");
        
        context.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testTardisDoorNoOpenWhenSneaking(TestContext context) {
        // Ensure TardisDataLoader has a save directory
        if (TardisDataLoader.tardisSaveDirectory == null) {
            TardisDataLoader.tardisSaveDirectory = context.getWorld().getServer().getSavePath(WorldSavePath.ROOT).resolve("tardis_data");
        }

        BlockPos pos = new BlockPos(0, 1, 0);
        context.setBlockState(pos, DWMBlocks.TARDIS_BLOCK.getDefaultState());

        TardisBlockEntity be = (TardisBlockEntity) context.getBlockEntity(pos);
        UUID tardisId = be.getTardisId();
        if (tardisId == null) {
            context.getWorld().getBlockEntity(pos).createNbt(context.getWorld().getRegistryManager());
            tardisId = be.getTardisId();
        }

        // Simulate right click while sneaking
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        player.setSneaking(true);
        
        context.useBlock(pos, player);

        // Verify final state
        TardisDoorState doorState = TardisLogic.getDoorState(tardisId);
        context.assertFalse(doorState.isOpen, "Tardis door should remain closed when clicking while sneaking");
        
        context.complete();
    }
}
