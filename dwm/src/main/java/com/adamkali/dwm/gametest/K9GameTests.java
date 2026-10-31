package com.adamkali.dwm.gametest;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.K9Entity;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class K9GameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void k9UnitSpawnsTamedK9(GameTestHelper context) {
        BlockPos floor = new BlockPos(2, 1, 2);
        context.setBlock(floor, Blocks.STONE.defaultBlockState());
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        ItemStack unit = new ItemStack(DWMItems.K9_UNIT);
        player.setItemInHand(InteractionHand.MAIN_HAND, unit);

        BlockPos abs = context.absolutePos(floor);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        DWMItems.K9_UNIT.useOn(new UseOnContext(context.getLevel(), player, InteractionHand.MAIN_HAND, unit, hit));

        List<K9Entity> k9s = context.getLevel().getEntitiesOfClass(K9Entity.class, new net.minecraft.world.phys.AABB(abs).inflate(3.0));
        if (k9s.size() != 1) {
            throw new AssertionError("Expected exactly one K9 after using the K-9 unit, found " + k9s.size());
        }
        K9Entity k9 = k9s.get(0);
        if (!k9.isTame() || !k9.isOwnedBy(player)) {
            throw new AssertionError("Expected K9 to be tamed to the activating player");
        }
        if (!unit.isEmpty()) {
            throw new AssertionError("Expected the K-9 unit to be consumed");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void k9MatchesDefinition(GameTestHelper context) {
        K9Entity k9 = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.K9);
        var definition = EntityDefinitionGameTestSupport.definitionOf(k9);
        EntityDefinitionGameTestSupport.assertAttributesMatch(k9, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(k9, definition);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void ownerInteractionTogglesSit(GameTestHelper context) {
        K9Entity k9 = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.K9);
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        k9.tame(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
        k9.mobInteract(player, InteractionHand.MAIN_HAND);
        context.succeedWhen(() -> context.assertTrue(
                k9.isInSittingPose() && EntityDefinitionGameTestSupport.isRunning(k9.goalSelector, SitWhenOrderedToGoal.class),
                "Expected owner interaction to order K9 to sit"));
    }
}
