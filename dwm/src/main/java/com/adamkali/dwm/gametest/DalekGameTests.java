package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekLaserEntity;
import com.adamkali.dwm.entity.DalekMutantEmergence;
import com.adamkali.dwm.entity.DalekMutantEntity;
import com.adamkali.dwm.entity.DalekVariant;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.ParadigmDalekEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class DalekGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnDalekOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Dalek after spawn");
        }
        dalek.finalizeSpawn(
                context.getLevel(),
                context.getLevel().getCurrentDifficultyAt(dalek.blockPosition()),
                EntitySpawnReason.NATURAL,
                null
        );
        assertVariantsFitChassis(dalek, DalekChassis.CLASSIC);
        context.assertEntityPresent(DWMEntityTypes.DALEK);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void spawnParadigmDalekOnGrass(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        ParadigmDalekEntity dalek = context.spawn(DWMEntityTypes.PARADIGM_DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Paradigm Dalek after spawn");
        }
        // Spawned without finalizeSpawn (like /summon with NBT): must already wear a Paradigm skin.
        if (dalek.getVariant() != DalekVariant.PARADIGM_DRONE) {
            throw new AssertionError("Fresh Paradigm Dalek should default to the Drone caste, got " + dalek.getVariant());
        }
        dalek.finalizeSpawn(
                context.getLevel(),
                context.getLevel().getCurrentDifficultyAt(dalek.blockPosition()),
                EntitySpawnReason.NATURAL,
                null
        );
        assertVariantsFitChassis(dalek, DalekChassis.PARADIGM);
        context.assertEntityPresent(DWMEntityTypes.PARADIGM_DALEK);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void paradigmRangedAttackFiresFromHigherGun(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        ParadigmDalekEntity dalek = context.spawn(DWMEntityTypes.PARADIGM_DALEK, grassRel.above());
        double muzzleHeight = dalek.gunMuzzleWorldPos().y - dalek.getY();
        if (Math.abs(muzzleHeight - DalekChassis.PARADIGM.gunHeight()) > 1.0e-4) {
            throw new AssertionError("Paradigm muzzle height " + muzzleHeight + " does not match its chassis");
        }
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(dalek.getX() + 4.0, dalek.getY(), dalek.getZ());
        dalek.performRangedAttack(player, 1.0F);
        context.assertEntityPresent(DWMEntityTypes.DALEK_LASER);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void paradigmDalekMatchesDefinition(GameTestHelper context) {
        ParadigmDalekEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.PARADIGM_DALEK);
        var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
        EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
        context.succeed();
    }

    /** finalizeSpawn picked a variant of {@code chassis}; own variants stick, foreign ones fall back. */
    private static void assertVariantsFitChassis(DalekEntity dalek, DalekChassis chassis) {
        if (dalek.getVariant().chassis() != chassis) {
            throw new AssertionError("finalizeSpawn set a variant from the wrong chassis: " + dalek.getVariant());
        }
        for (DalekVariant variant : DalekVariant.values()) {
            dalek.setVariant(variant);
            DalekVariant expected = variant.chassis() == chassis ? variant : chassis.defaultVariant();
            if (dalek.getVariant() != expected) {
                throw new AssertionError("Setting " + variant + " on a " + chassis + " Dalek gave " + dalek.getVariant());
            }
        }
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void rangedAttackSpawnsLaser(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Dalek after spawn");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(dalek.getX() + 4.0, dalek.getY(), dalek.getZ());
        dalek.performRangedAttack(player, 1.0F);
        context.assertEntityPresent(DWMEntityTypes.DALEK_LASER);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void fliesWhenTargetIsAbove(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        if (dalek == null || !dalek.isAlive()) {
            throw new AssertionError("Expected a living Dalek after spawn");
        }

        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(dalek.getX(), dalek.getY() + 6.0, dalek.getZ());
        dalek.setTarget(player);
        context.runAtTickTime(2, () -> {
            if (!dalek.isFlying()) {
                throw new AssertionError("Expected Dalek to fly when the target is well above ground pathing");
            }
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dalekMatchesDefinition(GameTestHelper context) {
        DalekEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.DALEK);
        var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
        EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dalekRetaliatesWhenHurt(GameTestHelper context) {
        DalekEntity dalek = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.DALEK);
        Player attacker = context.makeMockPlayer(GameType.SURVIVAL);
        attacker.snapTo(dalek.getX() + 2.0, dalek.getY(), dalek.getZ());
        // HurtByTargetGoal ignores a hit stamped on the spawn tick (timestamp 0), so let the Dalek age first.
        context.runAfterDelay(5, () -> {
            dalek.hurtServer(context.getLevel(), dalek.damageSources().playerAttack(attacker), 1.0F);
            context.succeedWhen(() -> context.assertTrue(dalek.getTarget() == attacker,
                    "Expected Dalek to target its attacker"));
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dalekMutantMatchesDefinition(GameTestHelper context) {
        DalekMutantEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.DALEK_MUTANT);
        var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
        EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
        EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void releasedMutantTargetsTheDalekKiller(GameTestHelper context) {
        DalekEntity dalek = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.DALEK);
        Player attacker = context.makeMockPlayer(GameType.SURVIVAL);
        attacker.snapTo(dalek.getX() + 2.0, dalek.getY(), dalek.getZ());
        context.runAfterDelay(5, () -> {
            dalek.hurtServer(context.getLevel(), dalek.damageSources().playerAttack(attacker), 1.0F);
            DalekMutantEntity mutant = DalekMutantEmergence.release(context.getLevel(), dalek);
            if (mutant == null || !mutant.isAlive()) {
                throw new AssertionError("Expected a living mutant from the destroyed casing");
            }
            if (mutant.position().distanceTo(dalek.position()) > 1.0e-6) {
                throw new AssertionError("Mutant should crawl out where the casing stood");
            }
            if (!mutant.isPersistenceRequired()) {
                throw new AssertionError("Mutant from a persistent casing should be persistent too");
            }
            if (mutant.getTarget() != attacker) {
                throw new AssertionError("Mutant should go for the player who destroyed its casing");
            }
            context.assertEntityPresent(DWMEntityTypes.DALEK_MUTANT);
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void killedDaleksSometimesReleaseMutants(GameTestHelper context) {
        // 48 independent 25% rolls: the chance of no mutant at all is about 1e-6.
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());
        for (int i = 0; i < 48; i++) {
            DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
            dalek.setNoAi(true);
            dalek.kill(context.getLevel());
        }
        context.succeedWhen(() -> {
            context.assertEntityNotPresent(DWMEntityTypes.DALEK);
            context.assertEntityPresent(DWMEntityTypes.DALEK_MUTANT);
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void discardedDalekDoesNotReleaseMutant(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());
        for (int i = 0; i < 48; i++) {
            context.spawn(DWMEntityTypes.DALEK, grassRel.above()).discard();
        }
        context.runAfterDelay(2, () -> {
            context.assertEntityNotPresent(DWMEntityTypes.DALEK_MUTANT);
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dyingDalekDiscardedMidAnimationDoesNotReleaseMutant(GameTestHelper context) {
        // Killed but discarded before the 20-tick death animation ends (as a command or chunk unload would).
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());
        for (int i = 0; i < 48; i++) {
            DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
            dalek.setNoAi(true);
            dalek.kill(context.getLevel());
            if (!dalek.isDeadOrDying()) {
                throw new AssertionError("Killed Dalek should be dying before it is discarded");
            }
            dalek.discard();
        }
        context.runAfterDelay(2, () -> {
            context.assertEntityNotPresent(DWMEntityTypes.DALEK_MUTANT);
            context.succeed();
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void dalekLaserPassesThroughMutant(GameTestHelper context) {
        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, new BlockPos(0, 2, 2));
        dalek.setNoAi(true);
        dalek.setNoGravity(true);
        DalekMutantEntity mutant = context.spawn(DWMEntityTypes.DALEK_MUTANT, new BlockPos(3, 2, 2));
        mutant.setNoAi(true);
        mutant.setNoGravity(true);
        float health = mutant.getHealth();

        DalekLaserEntity laser = new DalekLaserEntity(context.getLevel(), dalek);
        double y = mutant.getY() + mutant.getBbHeight() / 2.0;
        laser.setPos(mutant.getX() - 1.5, y, mutant.getZ());
        laser.shoot(1.0, 0.0, 0.0, 1.6F, 0.0F);
        context.getLevel().addFreshEntity(laser);

        context.runAfterDelay(3, () -> {
            if (mutant.getHealth() != health) {
                throw new AssertionError("Dalek laser hurt a mutant: " + mutant.getHealth() + " / " + health);
            }
            if (laser.isRemoved() || laser.getX() <= mutant.getX() + 1.0) {
                throw new AssertionError("Dalek laser should fly on through the mutant, laser x " + laser.getX());
            }
            context.succeed();
        });
    }
}
