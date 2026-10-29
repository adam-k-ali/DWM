package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.plant.ThalCropBlock;
import com.adamkali.dwm.block.plant.VargaPlantBlock;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SkaroPlantsGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void groundPlantsSurviveOnSandAndPopOffWhenSupportIsRemoved(GameTestHelper context) {
        List<Block> plants = List.of(DWMBlocks.VARGA_PLANT, DWMBlocks.RADIATION_FUNGUS, DWMBlocks.ASH_SCRUB);
        int x = 1;
        for (Block plant : plants) {
            BlockPos ground = new BlockPos(x++, 1, 1);
            context.setBlock(ground, Blocks.SAND.defaultBlockState());
            context.setBlock(ground.above(), plant.defaultBlockState());
            context.assertBlockPresent(plant, ground.above());
            // Support removal schedules a 1-tick break (not instant), so check on a later tick.
            context.setBlock(ground, Blocks.AIR.defaultBlockState());
        }
        context.runAfterDelay(5, () -> {
            for (int i = 0; i < plants.size(); i++) {
                context.assertBlockPresent(Blocks.AIR, new BlockPos(1 + i, 2, 1));
            }
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void fungusEmitsLowLight(GameTestHelper context) {
        BlockPos ground = new BlockPos(1, 1, 1);
        context.setBlock(ground, Blocks.GRAVEL.defaultBlockState());
        context.setBlock(ground.above(), DWMBlocks.RADIATION_FUNGUS.defaultBlockState());
        BlockState state = context.getBlockState(ground.above());
        if (state.getLightEmission() != 3) {
            throw new AssertionError("Expected radiation fungus light level 3, got " + state.getLightEmission());
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void vargaPoisonsOnceAndRespectsRefreshLimit(GameTestHelper context) {
        BlockPos ground = new BlockPos(1, 1, 1);
        context.setBlock(ground, Blocks.SAND.defaultBlockState());
        context.setBlock(ground.above(), DWMBlocks.VARGA_PLANT.defaultBlockState());
        LivingEntity cow = context.spawn(net.minecraft.world.entity.EntityTypes.COW, ground.above());
        cow.removeEffect(MobEffects.POISON);

        if (!VargaPlantBlock.applyPoison(cow)) {
            throw new AssertionError("First Varga contact should apply poison");
        }
        MobEffectInstance applied = cow.getEffect(MobEffects.POISON);
        if (applied == null || applied.getDuration() != VargaPlantBlock.POISON_DURATION_TICKS) {
            throw new AssertionError("Expected poison of " + VargaPlantBlock.POISON_DURATION_TICKS + " ticks, got " + applied);
        }
        if (VargaPlantBlock.applyPoison(cow)) {
            throw new AssertionError("Poison must not refresh while plenty of duration remains");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void reedsNeedAdjacentWater(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        BlockState reeds = DWMBlocks.MUTATED_REEDS.defaultBlockState();

        BlockPos dry = new BlockPos(1, 1, 1);
        context.setBlock(dry, Blocks.DIRT.defaultBlockState());
        BlockPos dryAbs = context.absolutePos(dry.above());
        if (reeds.canSurvive(level, dryAbs)) {
            throw new AssertionError("Reeds must not survive on dry dirt");
        }

        BlockPos wet = new BlockPos(4, 1, 1);
        context.setBlock(wet, Blocks.DIRT.defaultBlockState());
        context.setBlock(wet.east(), Blocks.WATER.defaultBlockState());
        if (!reeds.canSurvive(level, context.absolutePos(wet.above()))) {
            throw new AssertionError("Reeds should survive on dirt beside water");
        }
        context.setBlock(wet.above(), reeds);
        if (!reeds.canSurvive(level, context.absolutePos(wet.above(2)))) {
            throw new AssertionError("Reeds should stack on reeds");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void plantsDropThemselves(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        for (Block plant : DWMBlocks.SKARO_PLANTS) {
            BlockPos pos = new BlockPos(1, 2, 1);
            context.setBlock(pos, plant.defaultBlockState());
            List<ItemStack> drops = drops(context, player, pos);
            if (drops.size() != 1 || !drops.get(0).is(plant.asItem())) {
                throw new AssertionError("Expected " + plant + " to drop itself, got " + drops);
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void thalCropDropsSeedsAndOnlyRipeCropYieldsMore(GameTestHelper context) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = new BlockPos(1, 2, 1);
        context.setBlock(pos, DWMBlocks.THAL_CROP.defaultBlockState().setValue(ThalCropBlock.AGE, 7));
        int ripe = count(drops(context, player, pos));
        context.setBlock(pos, DWMBlocks.THAL_CROP.defaultBlockState().setValue(ThalCropBlock.AGE, 2));
        int young = count(drops(context, player, pos));
        if (young != 0 && young != 1) {
            throw new AssertionError("Young Thal crop should drop at most its seed, got " + young);
        }
        if (ripe < 2) {
            throw new AssertionError("Ripe Thal crop should drop seeds, got " + ripe);
        }
        context.succeed();
    }

    private static int count(List<ItemStack> drops) {
        int n = 0;
        for (ItemStack stack : drops) {
            if (stack.is(DWMItems.THAL_SEEDS)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    private static List<ItemStack> drops(GameTestHelper context, Player player, BlockPos relativePos) {
        BlockPos abs = context.absolutePos(relativePos);
        ServerLevel world = context.getLevel();
        return Block.getDrops(world.getBlockState(abs), world, abs, world.getBlockEntity(abs), player, ItemStack.EMPTY);
    }
}
