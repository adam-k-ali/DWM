package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.item.SonicFieldMode;
import com.adamkali.dwm.item.SonicStateLogic;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SonicTardisGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void seal_requiresSelectedSealOnExterior(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        Player owner = setup.owner();

        ItemStack open = SonicStateLogic.pairWithTardisStack(
                SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR)));
        useOn(owner, open, setup.tardisAbs());
        if (setup.model().doorsLocked) {
            throw new AssertionError("Open setting must not lock TARDIS doors");
        }

        ItemStack seal = SonicStateLogic.withModes(
                new ItemStack(DWMItems.SONIC_THIRD_DOCTOR),
                java.util.EnumSet.of(
                        SonicFieldMode.OPEN,
                        SonicFieldMode.SEAL,
                        SonicFieldMode.SCAN,
                        SonicFieldMode.PING
                ),
                SonicFieldMode.SEAL
        );
        seal = seal.copy();
        SonicStateLogic.pairWithTardis(seal);
        SonicStateLogic.select(seal, SonicFieldMode.SEAL);
        useOn(owner, seal, setup.tardisAbs());
        if (!setup.model().doorsLocked) {
            throw new AssertionError("Seal-selected sonic should lock closed exterior doors");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void seal_requiresSelectedSealOnInterior(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        BlockPos interiorAbs = placeInteriorDoor(context, setup.tardisId());
        ItemStack open = SonicStateLogic.pairWithTardisStack(
                SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR)));
        useOn(setup.owner(), open, interiorAbs);
        if (setup.model().doorsLocked) {
            throw new AssertionError("Open setting must not lock interior TARDIS doors");
        }

        ItemStack seal = SonicStateLogic.pairWithTardisStack(SonicStateLogic.openOnlyStack(
                new ItemStack(DWMItems.SONIC_THIRD_DOCTOR)));
        SonicStateLogic.select(seal, SonicFieldMode.SEAL);
        useOn(setup.owner(), seal, interiorAbs);
        if (!setup.model().doorsLocked) {
            throw new AssertionError("Seal-selected sonic should lock closed interior doors");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void scan_requiresSelectedScanOnInterior(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        BlockPos interiorAbs = placeInteriorDoor(context, setup.tardisId());
        boolean lockedBefore = setup.model().doorsLocked;
        boolean cloakedBefore = setup.model().cloaked;

        ItemStack open = SonicStateLogic.pairWithTardisStack(
                SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR)));
        useOn(setup.owner(), open, interiorAbs);
        if (setup.model().doorsLocked != lockedBefore || setup.model().cloaked != cloakedBefore) {
            throw new AssertionError("Open setting on interior doors must not change lock or cloak");
        }

        ItemStack scan = SonicStateLogic.pairWithTardisStack(SonicStateLogic.openOnlyStack(
                new ItemStack(DWMItems.SONIC_THIRD_DOCTOR)));
        SonicStateLogic.select(scan, SonicFieldMode.SCAN);
        useOn(setup.owner(), scan, interiorAbs);
        if (setup.model().doorsLocked != lockedBefore || setup.model().cloaked != cloakedBefore) {
            throw new AssertionError("Scan must be read-only");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void scan_requiresSelectedScanOnExterior(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        boolean lockedBefore = setup.model().doorsLocked;
        ItemStack scan = SonicStateLogic.pairWithTardisStack(SonicStateLogic.openOnlyStack(
                new ItemStack(DWMItems.SONIC_THIRD_DOCTOR)));
        SonicStateLogic.select(scan, SonicFieldMode.SCAN);
        useOn(setup.owner(), scan, setup.tardisAbs());
        if (setup.model().doorsLocked != lockedBefore) {
            throw new AssertionError("Scan on exterior doors must be read-only");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void handshake_unlocksTardisModesForOwner(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        useOn(setup.owner(), sonic, setup.tardisAbs());
        if (!SonicStateLogic.effective(sonic).tardisPaired()) {
            throw new AssertionError("Expected owner use to pair the sonic");
        }
        if (!SonicStateLogic.isUnlocked(sonic, SonicFieldMode.SEAL)
                || !SonicStateLogic.isUnlocked(sonic, SonicFieldMode.SCAN)
                || !SonicStateLogic.isUnlocked(sonic, SonicFieldMode.PING)) {
            throw new AssertionError("Handshake must unlock Seal, Scan, and Ping");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void stranger_cannotSeal(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        Player stranger = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack seal = SonicStateLogic.withModes(
                new ItemStack(DWMItems.SONIC_THIRD_DOCTOR),
                java.util.EnumSet.allOf(SonicFieldMode.class),
                SonicFieldMode.SEAL
        );
        useOn(stranger, seal, setup.tardisAbs());
        if (setup.model().doorsLocked) {
            throw new AssertionError("Stranger sonic must not lock another player's TARDIS");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void exteriorDoorsYieldToHeldSonic(GameTestHelper context) {
        Setup setup = placeOwnedExterior(context);
        ItemStack sonic = new ItemStack(DWMItems.SONIC_THIRD_DOCTOR);
        Player owner = setup.owner();
        owner.setItemInHand(InteractionHand.MAIN_HAND, sonic);
        boolean openBefore = setup.model().doorState.isOpen;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(setup.tardisAbs()), Direction.WEST, setup.tardisAbs(), false);
        InteractionResult yielded = context.getLevel().getBlockState(setup.tardisAbs())
                .useWithoutItem(context.getLevel(), owner, hit);
        if (yielded.consumesAction()) {
            throw new AssertionError("Exterior doors must PASS when a sonic is in the main hand");
        }
        if (setup.model().doorState.isOpen != openBefore) {
            throw new AssertionError("Yielding to a sonic must not toggle TARDIS doors");
        }
        context.succeed();
    }

    private static void useOn(Player player, ItemStack sonic, BlockPos absPos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
        sonic.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static Setup placeOwnedExterior(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");
        BlockPos tardisRel = new BlockPos(1, 2, 1);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        Player owner = context.makeMockPlayer(GameType.SURVIVAL);
        model.setOwner(owner.getUUID());
        if (!DoorLockLogic.areDoorsClosed(model.doorState)) {
            throw new AssertionError("Expected placed TARDIS doors to start closed");
        }
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
