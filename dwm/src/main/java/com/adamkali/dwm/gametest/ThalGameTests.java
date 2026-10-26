package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.entity.ThalVariant;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.EnumSet;

public class ThalGameTests {
    private static final int FLOOR_SIZE = 16;

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnThalOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        ThalEntity thal = context.spawn(DWMEntityTypes.THAL, grassRel.above());
        if (thal == null || !thal.isAlive()) {
            throw new AssertionError("Expected a living Thal after spawn");
        }
        if (!EnumSet.allOf(ThalVariant.class).contains(thal.getVariant())) {
            throw new AssertionError("Unexpected Thal variant: " + thal.getVariant());
        }
        context.assertEntityPresent(DWMEntityTypes.THAL);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void thalMatchesDefinition(GameTestHelper context) {
        ThalEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
        EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
        if (!mob.targetSelector.getAvailableGoals().isEmpty()) {
            throw new AssertionError("Thals must have no target goals, got " + mob.targetSelector.getAvailableGoals());
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void thalVariantSurvivesSaveAndLoad(GameTestHelper context) {
        ThalEntity original = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        for (ThalVariant variant : ThalVariant.values()) {
            original.setVariant(variant);

            TagValueOutput output = TagValueOutput.createWithContext(
                    ProblemReporter.DISCARDING, context.getLevel().registryAccess());
            original.saveWithoutId(output);

            ThalEntity reloaded = DWMEntityTypes.THAL.create(context.getLevel(), EntitySpawnReason.LOAD);
            if (reloaded == null) {
                throw new AssertionError("Could not create a Thal to load into");
            }
            reloaded.load(TagValueInput.create(
                    ProblemReporter.DISCARDING, context.getLevel().registryAccess(), output.buildResult()));
            if (reloaded.getVariant() != variant) {
                throw new AssertionError("Variant " + variant + " reloaded as " + reloaded.getVariant());
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void thalFleesDalek(GameTestHelper context) {
        layFloor(context);
        ThalEntity thal = context.spawn(DWMEntityTypes.THAL, new BlockPos(8, 2, 8));
        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, new BlockPos(8, 2, 13));
        dalek.setNoAi(true);
        double start = thal.distanceTo(dalek);
        context.succeedWhen(() -> context.assertTrue(
                thal.distanceTo(dalek) > start + 3.0,
                "Thal should flee the Dalek: started " + start + " blocks away, now " + thal.distanceTo(dalek)));
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void thalNeverTargetsAPlayer(GameTestHelper context) {
        ThalEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        Player attacker = context.makeMockPlayer(GameType.SURVIVAL);
        mob.hurtServer(context.getLevel(), mob.damageSources().playerAttack(attacker), 1.0F);
        context.onEachTick(() -> {
            if (mob.getTarget() != null) {
                throw new AssertionError("A hurt Thal must never acquire a target, got " + mob.getTarget());
            }
        });
        context.succeedWhen(() -> context.assertTrue(
                EntityDefinitionGameTestSupport.isRunning(mob.goalSelector, PanicGoal.class),
                "Expected PanicGoal to run after being hurt"));
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void thalSpawnPlacementRejectsNaturalSpawns(GameTestHelper context) {
        layFloor(context);
        BlockPos pos = context.absolutePos(new BlockPos(8, 2, 8));
        var level = context.getLevel();
        var random = level.getRandom();
        for (EntitySpawnReason reason : new EntitySpawnReason[]{EntitySpawnReason.NATURAL, EntitySpawnReason.CHUNK_GENERATION}) {
            if (SpawnPlacements.checkSpawnRules(DWMEntityTypes.THAL, level, reason, pos, random)) {
                throw new AssertionError("Thals must not spawn with reason " + reason + " outside a settlement");
            }
        }
        for (EntitySpawnReason reason : new EntitySpawnReason[]{
                EntitySpawnReason.SPAWN_ITEM_USE, EntitySpawnReason.COMMAND, EntitySpawnReason.STRUCTURE}) {
            if (!SpawnPlacements.checkSpawnRules(DWMEntityTypes.THAL, level, reason, pos, random)) {
                throw new AssertionError("Thals should be allowed to spawn with reason " + reason);
            }
        }
        context.succeed();
    }

    private static void layFloor(GameTestHelper context) {
        for (int x = 0; x <= FLOOR_SIZE; x++) {
            for (int z = 0; z <= FLOOR_SIZE; z++) {
                context.setBlock(new BlockPos(x, 1, z), DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());
            }
        }
    }
}
