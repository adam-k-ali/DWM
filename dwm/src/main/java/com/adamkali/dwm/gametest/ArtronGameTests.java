package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;

import java.util.UUID;

public class ArtronGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void emptyTank_leverDoesNotStartTravel(GameTestHelper context) {
        Setup setup = arrangeConsole(context);
        setup.model().artron = 0;
        setup.model().selectedBiome = "minecraft:plains";
        TardisTravelService.clearActiveForTests();
        TardisTravelService.clearLastTravelFailureReason();

        Player owner = context.makeMockPlayer(GameType.SURVIVAL);
        if (!TardisOwnershipLogic.tryClaimOnEnter(setup.tardisId(), owner.getUUID())) {
            throw new AssertionError("Expected owner claim to succeed");
        }

        FirstDoctorConsoleBlock.activateControl(
                LookTarget.MATERIALISATION_LEVER, context.getLevel(), setup.consoleAbs(), owner);

        if (setup.model().getTravelPhase() != TardisTravelPhase.IDLE) {
            throw new AssertionError("Empty tank must not start a travel phase, was "
                    + setup.model().getTravelPhase());
        }
        if (TardisTravelService.isTraveling(setup.tardisId())) {
            throw new AssertionError("Empty tank must not mark travel active");
        }
        if (!TardisTravelService.FAIL_INSUFFICIENT_ARTRON.equals(TardisTravelService.peekLastTravelFailureReason())) {
            throw new AssertionError("Expected insufficient artron refuse, got "
                    + TardisTravelService.peekLastTravelFailureReason());
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void crystalsOnRefueler_addTwentyFiveAndConsume(GameTestHelper context) {
        Setup setup = arrangeConsole(context);
        setup.model().artron = 50;

        Player visitor = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack crystals = new ItemStack(DWMItems.ZEITON_CRYSTALS, 2);
        visitor.setItemInHand(InteractionHand.MAIN_HAND, crystals);

        FirstDoctorConsoleBlock.activateControl(
                LookTarget.REFUELER, context.getLevel(), setup.consoleAbs(), visitor);

        if (ArtronLogic.read(setup.model()) != 75) {
            throw new AssertionError("Expected artron 75 after fill, got " + ArtronLogic.read(setup.model()));
        }
        if (crystals.getCount() != 1) {
            throw new AssertionError("Expected one crystal consumed, remaining " + crystals.getCount());
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void alreadyFull_doesNotConsumeCrystals(GameTestHelper context) {
        Setup setup = arrangeConsole(context);
        if (ArtronLogic.read(setup.model()) != ArtronLogic.CAPACITY) {
            throw new AssertionError("Placed ships should start full");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack crystals = new ItemStack(DWMItems.ZEITON_CRYSTALS, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, crystals);

        FirstDoctorConsoleBlock.activateControl(
                LookTarget.REFUELER, context.getLevel(), setup.consoleAbs(), player);

        if (ArtronLogic.read(setup.model()) != ArtronLogic.CAPACITY) {
            throw new AssertionError("Full tank must stay full");
        }
        if (crystals.getCount() != 2) {
            throw new AssertionError("Already-full must not consume crystals, remaining " + crystals.getCount());
        }
        context.succeed();
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
