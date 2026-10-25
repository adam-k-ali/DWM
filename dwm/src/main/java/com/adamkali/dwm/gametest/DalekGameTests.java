package com.adamkali.dwm.gametest;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekFamily;
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
        assertVariantsFitFamily(dalek, DalekFamily.CLASSIC);
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
        assertVariantsFitFamily(dalek, DalekFamily.PARADIGM);
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
    public void classicMuzzleFollowsTheVariantChassis(GameTestHelper context) {
        BlockPos grassRel = new BlockPos(2, 1, 2);
        context.setBlock(grassRel, DWMBlocks.GALLIFREY_GRASS_BLOCK.defaultBlockState());

        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, grassRel.above());
        for (DalekVariant variant : new DalekVariant[]{DalekVariant.CLASSIC_1963, DalekVariant.BRONZE_TIME_WAR}) {
            dalek.setVariant(variant);
            double muzzleHeight = dalek.gunMuzzleWorldPos().y - dalek.getY();
            if (Math.abs(muzzleHeight - variant.chassis().gunHeight()) > 1.0e-4) {
                throw new AssertionError(variant + " muzzle height " + muzzleHeight + " does not match its chassis");
            }
        }
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

    /**
     * finalizeSpawn picked a variant of {@code family}; own variants stick (and switch the chassis
     * with them), foreign ones fall back.
     */
    private static void assertVariantsFitFamily(DalekEntity dalek, DalekFamily family) {
        if (dalek.getVariant().family() != family) {
            throw new AssertionError("finalizeSpawn set a variant from the wrong family: " + dalek.getVariant());
        }
        for (DalekVariant variant : DalekVariant.values()) {
            dalek.setVariant(variant);
            DalekVariant expected = variant.family() == family ? variant : family.defaultVariant();
            if (dalek.getVariant() != expected) {
                throw new AssertionError("Setting " + variant + " on a " + family + " Dalek gave " + dalek.getVariant());
            }
            if (dalek.chassis() != expected.chassis()) {
                throw new AssertionError(variant + " should put the Dalek on the " + expected.chassis() + " chassis");
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
}
