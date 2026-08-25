package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.item.DWMDataComponents;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.CloakLogic;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class ConsolePilotGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void ownerCanCloakVisitorCannot(GameTestHelper context) {
        Setup setup = arrangeConsole(context);

        context.runAtTickTime(1, () -> {
            Player owner = context.makeMockPlayer(GameType.SURVIVAL);
            if (!TardisOwnershipLogic.tryClaimOnEnter(setup.tardisId(), owner.getUUID())) {
                throw new AssertionError("Expected owner claim to succeed");
            }

            ConsoleControlInteractionEntity cloak = requireControl(context, setup.consoleAbs(), LookTarget.CLOAK);
            InteractionResult ownerResult = cloak.interact(owner, InteractionHand.MAIN_HAND, Vec3.ZERO);
            if (!ownerResult.consumesAction()) {
                throw new AssertionError("Owner cloak click should succeed, got " + ownerResult);
            }
            if (!CloakLogic.isCloaked(setup.model())) {
                throw new AssertionError("Owner should engage cloak");
            }

            Player visitor = context.makeMockPlayer(GameType.SURVIVAL);
            boolean before = CloakLogic.isCloaked(setup.model());
            InteractionResult visitorResult = cloak.interact(visitor, InteractionHand.MAIN_HAND, Vec3.ZERO);
            if (!visitorResult.consumesAction()) {
                throw new AssertionError("Visitor cloak click should consume, got " + visitorResult);
            }
            if (CloakLogic.isCloaked(setup.model()) != before) {
                throw new AssertionError("Visitor must not toggle cloak");
            }

            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void visitorWithBoundKeyCanToggleDoorLock(GameTestHelper context) {
        Setup setup = arrangeConsole(context);

        context.runAtTickTime(1, () -> {
            Player owner = context.makeMockPlayer(GameType.SURVIVAL);
            if (!TardisOwnershipLogic.tryClaimOnEnter(setup.tardisId(), owner.getUUID())) {
                throw new AssertionError("Expected owner claim to succeed");
            }
            if (!DoorLockLogic.areDoorsClosed(setup.model().doorState)) {
                throw new AssertionError("Doors must start closed for lock toggle");
            }

            Player visitor = context.makeMockPlayer(GameType.SURVIVAL);
            ItemStack key = new ItemStack(DWMItems.TARDIS_KEY);
            key.set(DWMDataComponents.BOUND_TARDIS_ID, setup.tardisId());
            visitor.setItemInHand(InteractionHand.MAIN_HAND, key);

            ConsoleControlInteractionEntity doorLock = requireControl(context, setup.consoleAbs(), LookTarget.DOOR_LOCK);
            boolean before = DoorLockLogic.isLocked(setup.model());
            InteractionResult result = doorLock.interact(visitor, InteractionHand.MAIN_HAND, Vec3.ZERO);
            if (!result.consumesAction()) {
                throw new AssertionError("Keyed visitor door lock should succeed, got " + result);
            }
            if (DoorLockLogic.isLocked(setup.model()) == before) {
                throw new AssertionError("Keyed visitor should toggle door lock");
            }

            Player other = context.makeMockPlayer(GameType.SURVIVAL);
            boolean lockedBefore = DoorLockLogic.isLocked(setup.model());
            otherInteract(doorLock, other);
            if (DoorLockLogic.isLocked(setup.model()) != lockedBefore) {
                throw new AssertionError("Visitor without key must not toggle door lock");
            }

            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void visitorCanUseOxygenReader(GameTestHelper context) {
        Setup setup = arrangeConsole(context);

        context.runAtTickTime(1, () -> {
            Player owner = context.makeMockPlayer(GameType.SURVIVAL);
            if (!TardisOwnershipLogic.tryClaimOnEnter(setup.tardisId(), owner.getUUID())) {
                throw new AssertionError("Expected owner claim to succeed");
            }

            Player visitor = context.makeMockPlayer(GameType.SURVIVAL);
            ConsoleControlInteractionEntity oxygen = requireControl(
                    context, setup.consoleAbs(), LookTarget.OXYGEN_READER);
            InteractionResult result = oxygen.interact(visitor, InteractionHand.MAIN_HAND, Vec3.ZERO);
            if (!result.consumesAction()) {
                throw new AssertionError("Visitor oxygen reader should respond, got " + result);
            }

            context.succeed();
        });
    }

    private static void otherInteract(ConsoleControlInteractionEntity control, Player player) {
        control.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
    }

    private static ConsoleControlInteractionEntity requireControl(
            GameTestHelper context,
            BlockPos consoleAbs,
            LookTarget target
    ) {
        List<ConsoleControlInteractionEntity> matches = context.getLevel().getEntitiesOfClass(
                ConsoleControlInteractionEntity.class,
                new AABB(consoleAbs).inflate(2.5),
                entity -> entity.isBoundTo(consoleAbs) && entity.getLookTarget() == target
        );
        if (matches.isEmpty()) {
            throw new AssertionError("Expected control entity for " + target);
        }
        return matches.getFirst();
    }

    private static Setup arrangeConsole(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected TardisBlockEntity");
        }
        UUID tardisId = tardis.getTardisId();
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }

        BlockPos consoleRel = new BlockPos(4, 2, 4);
        BlockPos consoleAbs = context.absolutePos(consoleRel);
        context.setBlock(
                consoleRel,
                DWMBlocks.FIRST_DOCTOR_CONSOLE.defaultBlockState()
                        .setValue(FirstDoctorConsoleBlock.FACING, Direction.NORTH)
        );
        if (!(context.getLevel().getBlockEntity(consoleAbs) instanceof FirstDoctorConsoleBlockEntity console)) {
            throw new AssertionError("Expected FirstDoctorConsoleBlockEntity");
        }
        console.setTardisId(tardisId);
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                tardisAbs.getX(),
                tardisAbs.getY(),
                tardisAbs.getZ(),
                0
        );
        return new Setup(tardisId, model, consoleAbs);
    }

    private record Setup(UUID tardisId, TardisDataModel model, BlockPos consoleAbs) {
    }
}
