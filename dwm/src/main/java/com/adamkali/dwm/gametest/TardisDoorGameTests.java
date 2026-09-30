package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
        Setup setup = placeOwnedExterior(context);
        ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);

        useKeyThroughGameMode(context, setup.owner(), key, setup.tardisAbs());
        if (!setup.tardisId().equals(key.get(DWMDataComponents.BOUND_TARDIS_ID))) {
            throw new AssertionError("Expected owner to bind key to the TARDIS");
        }
        if (setup.model().doorsLocked) {
            throw new AssertionError("Binding a key must not lock the doors");
        }
        if (setup.model().doorState.isOpen) {
            throw new AssertionError("Using a key must not toggle TARDIS doors");
        }

        useKeyThroughGameMode(context, setup.owner(), key, setup.tardisAbs());
        if (!setup.model().doorsLocked) {
            throw new AssertionError("Expected bound key to lock the TARDIS doors");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    @SuppressWarnings("null")
    public void tardisKey_BindsOnInteriorDoors(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        BlockPos interiorAbs = placeInteriorDoor(context, setup.tardisId());
        ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);

        useKeyThroughGameMode(context, setup.owner(), key, interiorAbs);
        if (!setup.tardisId().equals(key.get(DWMDataComponents.BOUND_TARDIS_ID))) {
            throw new AssertionError("Expected owner to bind key on interior doors");
        }
        if (setup.model().doorState.isOpen) {
            throw new AssertionError("Using a key on interior doors must not toggle them");
        }

        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    @SuppressWarnings("null")
    public void tardisKey_RefusesLockWhileDoorsOpen(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);

        useKeyThroughGameMode(context, setup.owner(), key, setup.tardisAbs());
        if (!setup.tardisId().equals(key.get(DWMDataComponents.BOUND_TARDIS_ID))) {
            throw new AssertionError("Expected owner to bind key to the TARDIS");
        }

        InteractionResult openResult = TardisLogic.toggleDoor(setup.tardisId());
        if (openResult != InteractionResult.SUCCESS || !setup.model().doorState.isOpen) {
            throw new AssertionError("Expected doors to open before lock attempt");
        }

        useKeyThroughGameMode(context, setup.owner(), key, setup.tardisAbs());
        if (setup.model().doorsLocked) {
            throw new AssertionError("Lock must be refused while doors are open");
        }

        context.succeed();
    }

    /**
     * 26.2 GameMode runs {@code useWithoutItem} before {@code Item.useOn}. A consuming door
     * click would swallow bind / lock, so the block must PASS when a key is held.
     */
    private static void useKeyThroughGameMode(
            GameTestHelper context,
            Player player,
            ItemStack key,
            BlockPos absPos
    ) {
        player.setItemInHand(InteractionHand.MAIN_HAND, key);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
        InteractionResult yielded = context.getLevel().getBlockState(absPos)
                .useWithoutItem(context.getLevel(), player, hit);
        if (yielded.consumesAction()) {
            throw new AssertionError("Doors must PASS when a TARDIS key is in the main hand");
        }
        DWMItems.TARDIS_KEY.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static Setup placeOwnedExterior(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");
        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected placed TARDIS block entity");
        }
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        Player owner = context.makeMockPlayer(GameType.SURVIVAL);
        model.setOwner(owner.getUUID());
        return new Setup(tardisId, model, tardisAbs, owner);
    }

    private static BlockPos placeInteriorDoor(GameTestHelper context, UUID tardisId) {
        Direction facing = Direction.SOUTH;
        BlockPos originRel = new BlockPos(4, 2, 4);
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            for (int slot = 0; slot < TardisInteriorDoorBlock.BANK_WIDTH; slot++) {
                BlockPos cellRel = TardisInteriorDoorBlock.cellPos(originRel, facing, half, slot);
                context.setBlock(
                        cellRel.getX(), cellRel.getY(), cellRel.getZ(),
                        TardisInteriorDoorBlock.bankCellState(facing, half, slot, false));
            }
        }
        BlockPos originAbs = context.absolutePos(originRel);
        if (!(context.getLevel().getBlockEntity(originAbs) instanceof TardisInteriorDoorBlockEntity door)) {
            throw new AssertionError("Expected origin interior door");
        }
        door.setTardisId(tardisId);
        return originAbs;
    }

    private record Setup(UUID tardisId, TardisDataModel model, BlockPos tardisAbs, Player owner) {
    }
}
