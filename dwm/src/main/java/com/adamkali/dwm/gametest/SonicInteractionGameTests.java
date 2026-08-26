package com.adamkali.dwm.gametest;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.item.SonicFieldMode;
import com.adamkali.dwm.item.SonicStateLogic;
import com.adamkali.dwm.network.SelectSonicFieldModeC2SPayload;
import com.adamkali.dwm.network.ServerPayloadTypeRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SonicInteractionGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void craftedOpenOnly_opensIronTrapdoor(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        BlockPos trapdoorRel = new BlockPos(1, 2, 1);
        BlockPos trapdoorAbs = context.absolutePos(trapdoorRel);
        context.setBlock(trapdoorRel, Blocks.IRON_TRAPDOOR);
        useOnBlock(player, sonic, trapdoorAbs);

        if (!context.getLevel().getBlockState(trapdoorAbs).getValue(TrapDoorBlock.OPEN)) {
            throw new AssertionError("Expected Open-only sonic to open iron trapdoor");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void glass_noopUntilShatterInstalledAndSelected(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        BlockPos glassRel = new BlockPos(1, 2, 1);
        BlockPos glassAbs = context.absolutePos(glassRel);
        context.setBlock(glassRel, Blocks.GLASS);
        useOnBlock(player, sonic, glassAbs);
        context.assertBlockPresent(Blocks.GLASS, glassRel);

        SonicStateLogic.install(sonic, SonicFieldMode.SHATTER);
        // Still Open selected → wrong setting, glass remains
        useOnBlock(player, sonic, glassAbs);
        context.assertBlockPresent(Blocks.GLASS, glassRel);

        SonicStateLogic.select(sonic, SonicFieldMode.SHATTER);
        useOnBlock(player, sonic, glassAbs);
        context.assertBlockPresent(Blocks.AIR, glassRel);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void selectViaC2S_thenShatterSucceeds(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        SonicStateLogic.install(sonic, SonicFieldMode.SHATTER);
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        boolean selected = ServerPayloadTypeRegistry.safelyHandleSelectSonicFieldMode(
                new SelectSonicFieldModeC2SPayload(SonicFieldMode.SHATTER),
                player
        );
        if (!selected) {
            throw new AssertionError("Expected C2S to select Shatter");
        }
        if (SonicStateLogic.selected(player.getMainHandItem()) != SonicFieldMode.SHATTER) {
            throw new AssertionError("Expected Shatter selected on stack after C2S");
        }

        BlockPos glassRel = new BlockPos(1, 2, 1);
        BlockPos glassAbs = context.absolutePos(glassRel);
        context.setBlock(glassRel, Blocks.GLASS);
        useOnBlock(player, player.getMainHandItem(), glassAbs);
        context.assertBlockPresent(Blocks.AIR, glassRel);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void c2s_rejectsLockedMode(GameTestHelper context) {
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack sonic = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_THIRD_DOCTOR));
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);

        boolean selected = ServerPayloadTypeRegistry.safelyHandleSelectSonicFieldMode(
                new SelectSonicFieldModeC2SPayload(SonicFieldMode.PRIME),
                player
        );
        if (selected) {
            throw new AssertionError("Expected C2S to reject locked Prime");
        }
        if (SonicStateLogic.selected(player.getMainHandItem()) != SonicFieldMode.OPEN) {
            throw new AssertionError("Expected selection to remain Open");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void shear_requiresShearMode(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack openOnly = SonicStateLogic.openOnlyStack(new ItemStack(DWMItems.SONIC_SECOND_DOCTOR));
        player.setItemInHand(InteractionHand.MAIN_HAND, openOnly);

        Sheep sheep = (Sheep) context.spawn(EntityTypes.SHEEP, 2, 2, 1);
        DWMItems.SONIC_SECOND_DOCTOR.interactLivingEntity(openOnly, player, sheep, InteractionHand.MAIN_HAND);
        if (sheep.isSheared()) {
            throw new AssertionError("Expected Open-only sonic not to shear");
        }

        ItemStack shearSonic = SonicStateLogic.withModes(
                new ItemStack(DWMItems.SONIC_SECOND_DOCTOR),
                EnumSet.of(SonicFieldMode.OPEN, SonicFieldMode.SHEAR),
                SonicFieldMode.SHEAR
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, shearSonic);
        DWMItems.SONIC_SECOND_DOCTOR.interactLivingEntity(shearSonic, player, sheep, InteractionHand.MAIN_HAND);
        if (!sheep.isSheared()) {
            throw new AssertionError("Expected Shear-selected sonic to shear sheep");
        }
        context.succeed();
    }

    private static void useOnBlock(Player player, ItemStack sonic, BlockPos absPos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, sonic);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
        sonic.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
