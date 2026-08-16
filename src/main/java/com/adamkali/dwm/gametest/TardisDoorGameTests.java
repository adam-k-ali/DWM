package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.item.DWMDataComponents;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TardisDoorGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void tardisDoorStateSmokeFlow(GameTestHelper context) {
        context.setBlock(1, 2, 1, DWMBlocks.TARDIS_BLOCK);
        context.assertBlockPresent(DWMBlocks.TARDIS_BLOCK, 1, 2, 1);
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        TardisDataModel model = TardisDataLoader.create();
        InteractionResult toggleResult = TardisLogic.toggleDoor(model.uuid);
        if (toggleResult != InteractionResult.SUCCESS) {
            throw new AssertionError("Expected successful door toggle in smoke flow");
        }
        TardisLogic.updateDoorState(model.uuid);

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    @SuppressWarnings("null")
    public void tardisKey_BindsThenTogglesDoors(GameTestHelper context) {
        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected placed TARDIS block entity");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        model.setOwner(player.getUUID());

        ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);
        player.setItemInHand(InteractionHand.MAIN_HAND, key);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(tardisAbs), Direction.UP, tardisAbs, false);

        DWMItems.TARDIS_KEY.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        if (!tardisId.equals(key.get(DWMDataComponents.BOUND_TARDIS_ID))) {
            throw new AssertionError("Expected owner to bind key to the TARDIS");
        }
        if (model.doorsLocked) {
            throw new AssertionError("Binding a key must not lock the doors");
        }

        DWMItems.TARDIS_KEY.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        if (!model.doorsLocked) {
            throw new AssertionError("Expected bound key to lock the TARDIS doors");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    @SuppressWarnings("null")
    public void tardisKey_RefusesLockWhileDoorsOpen(GameTestHelper context) {
        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("gametest_tardis_data");
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected placed TARDIS block entity");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        model.setOwner(player.getUUID());

        ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);
        player.setItemInHand(InteractionHand.MAIN_HAND, key);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(tardisAbs), Direction.UP, tardisAbs, false);

        DWMItems.TARDIS_KEY.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        if (!tardisId.equals(key.get(DWMDataComponents.BOUND_TARDIS_ID))) {
            throw new AssertionError("Expected owner to bind key to the TARDIS");
        }

        InteractionResult openResult = TardisLogic.toggleDoor(tardisId);
        if (openResult != InteractionResult.SUCCESS || !model.doorState.isOpen) {
            throw new AssertionError("Expected doors to open before lock attempt");
        }

        DWMItems.TARDIS_KEY.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        if (model.doorsLocked) {
            throw new AssertionError("Lock must be refused while doors are open");
        }

        context.succeed();
    }
}
