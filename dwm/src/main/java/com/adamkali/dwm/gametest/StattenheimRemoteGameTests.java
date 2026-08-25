package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisTravelPhase;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class StattenheimRemoteGameTests {

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void sneakUseOnGround_StartsSummonDemat(GameTestHelper context) {
        TardisDataLoader.tardisSaveDirectory = context.getLevel().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("gametest_tardis_data");

        BlockPos tardisRel = new BlockPos(1, 2, 1);
        context.setBlock(tardisRel.below(), Blocks.STONE);
        context.setBlock(tardisRel, DWMBlocks.TARDIS_BLOCK);
        BlockPos tardisAbs = context.absolutePos(tardisRel);
        if (!(context.getLevel().getBlockEntity(tardisAbs) instanceof TardisBlockEntity tardis)) {
            throw new AssertionError("Expected placed TARDIS block entity");
        }

        TardisDataModel model = TardisDataLoader.get(tardis.getTardisId());
        if (model == null) {
            throw new AssertionError("Expected TARDIS data model");
        }
        model.setExteriorLocation(
                context.getLevel().dimension().identifier().toString(),
                tardisAbs.getX(),
                tardisAbs.getY(),
                tardisAbs.getZ(),
                0
        );

        BlockPos groundRel = new BlockPos(4, 1, 4);
        context.setBlock(groundRel, Blocks.STONE);
        context.setBlock(groundRel.above(), Blocks.AIR);
        context.setBlock(groundRel.above(2), Blocks.AIR);
        BlockPos landingRel = groundRel.above();
        BlockPos doorRel = landingRel.relative(Direction.SOUTH);
        context.setBlock(doorRel, Blocks.AIR);
        context.setBlock(doorRel.above(), Blocks.AIR);

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        model.setOwner(player.getUUID());
        model.doorState.isOpen = true;
        model.doorState.doorSwing = 1.0f;
        player.setShiftKeyDown(true);
        BlockPos landingAbs = context.absolutePos(landingRel);
        player.setPos(landingAbs.getX() + 0.5, landingAbs.getY(), landingAbs.getZ() + 2.5);

        ItemStack remote = new ItemStack(DWMItems.STATTENHEIM_REMOTE);
        player.setItemInHand(InteractionHand.MAIN_HAND, remote);
        BlockPos groundAbs = context.absolutePos(groundRel);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(groundAbs), Direction.UP, groundAbs, false);
        InteractionResult used = DWMItems.STATTENHEIM_REMOTE.useOn(
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit)
        );
        if (used != InteractionResult.SUCCESS) {
            throw new AssertionError("Expected sneak-use to succeed, got " + used);
        }
        if (model.getTravelPhase() != TardisTravelPhase.DEMATERIALISING) {
            throw new AssertionError("Expected DEMATERIALISING, got " + model.getTravelPhase());
        }
        if (model.travelDestinationX != landingAbs.getX()
                || model.travelDestinationY != landingAbs.getY()
                || model.travelDestinationZ != landingAbs.getZ()) {
            throw new AssertionError("Expected destination snapshot at landing "
                    + landingAbs
                    + " but was "
                    + model.travelDestinationX + "," + model.travelDestinationY + "," + model.travelDestinationZ);
        }
        if (model.doorState.isOpen || model.doorState.doorSwing != 0.0f) {
            throw new AssertionError("Expected doors slammed closed, isOpen="
                    + model.doorState.isOpen
                    + " doorSwing="
                    + model.doorState.doorSwing);
        }

        context.succeed();
    }
}
