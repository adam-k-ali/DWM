package com.adamkali.dwm.gametest;

import com.adamkali.dwm.entity.CyberMondanEntity;
import com.adamkali.dwm.entity.CybermanEntity;
import com.adamkali.dwm.entity.CybermasterEntity;
import com.adamkali.dwm.entity.CybusCybermanEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.WeaponsGradeCybermanEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.List;

public class CybermanGameTests {
    private static final float LETHAL = 1000.0F;

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void cybermenMatchTheirDefinitions(GameTestHelper context) {
        List<EntityType<? extends CybermanEntity>> types = List.of(
                DWMEntityTypes.CYBERMONDAN,
                DWMEntityTypes.CYBUS_CYBERMAN,
                DWMEntityTypes.CYBERMASTER,
                DWMEntityTypes.WEAPONS_GRADE_CYBERMAN
        );
        for (EntityType<? extends CybermanEntity> type : types) {
            CybermanEntity mob = EntityDefinitionGameTestSupport.spawnOnGrass(context, type);
            var definition = EntityDefinitionGameTestSupport.definitionOf(mob);
            EntityDefinitionGameTestSupport.assertAttributesMatch(mob, definition);
            EntityDefinitionGameTestSupport.assertGoalsMatch(mob, definition);
            if (!CybermanEntity.isCyberman(mob)) {
                throw new AssertionError(type + " must be in #dwm:cybermen");
            }
            mob.discard();
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void cybusGripSlowsItsTarget(GameTestHelper context) {
        CybusCybermanEntity cybus = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.CYBUS_CYBERMAN);
        cybus.setNoAi(true);
        Pig pig = context.spawn(EntityTypes.PIG, new BlockPos(5, 2, 4));
        pig.setNoAi(true);
        if (!cybus.doHurtTarget(context.getLevel(), pig)) {
            throw new AssertionError("Cybus melee should land on a pig beside it");
        }
        if (!pig.hasEffect(MobEffects.SLOWNESS)) {
            throw new AssertionError("Cybus electric grip should leave the target slowed");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void cybermenDoNotHurtEachOther(GameTestHelper context) {
        CybusCybermanEntity cybus = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.CYBUS_CYBERMAN);
        cybus.setNoAi(true);
        CyberMondanEntity mondan = context.spawn(DWMEntityTypes.CYBERMONDAN, new BlockPos(5, 2, 4));
        mondan.setNoAi(true);
        float start = mondan.getHealth();
        cybus.doHurtTarget(context.getLevel(), mondan);
        if (mondan.getHealth() != start || mondan.hasEffect(MobEffects.SLOWNESS)) {
            throw new AssertionError("A Cyberman must not be hurt or gripped by another Cyberman");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void cybermasterLaserHurtsAPigButNotACyberman(GameTestHelper context) {
        CybermasterEntity master = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.CYBERMASTER);
        master.setNoAi(true);
        Pig pig = context.spawn(EntityTypes.PIG, new BlockPos(1, 2, 7));
        pig.setNoAi(true);
        CybusCybermanEntity cybus = context.spawn(DWMEntityTypes.CYBUS_CYBERMAN, new BlockPos(7, 2, 1));
        cybus.setNoAi(true);
        float pigStart = pig.getHealth();
        float cybusStart = cybus.getHealth();
        master.performRangedAttack(pig, 1.0F);
        master.performRangedAttack(cybus, 1.0F);
        context.succeedWhen(() -> {
            context.assertTrue(pig.getHealth() < pigStart, "Cybermaster laser should hurt the pig");
            context.assertTrue(cybus.getHealth() == cybusStart, "Cybermaster laser must not hurt a Cyberman");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void cybermasterRegeneratesOnceThenDies(GameTestHelper context) {
        CybermasterEntity master = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.CYBERMASTER);
        master.setNoAi(true);
        DamageSource hit = master.damageSources().generic();

        master.hurtServer(context.getLevel(), hit, LETHAL);
        if (!master.isAlive() || master.getHealth() != master.getMaxHealth() || !master.hasRegenerated()) {
            throw new AssertionError("First killing blow should regenerate the Cybermaster to full health, got health "
                    + master.getHealth() + ", alive " + master.isAlive());
        }
        if (!master.hasEffect(MobEffects.RESISTANCE)) {
            throw new AssertionError("Regeneration should grant brief Resistance");
        }

        master.removeAllEffects();
        master.invulnerableTime = 0;
        master.hurtServer(context.getLevel(), hit, LETHAL);
        if (master.isAlive()) {
            throw new AssertionError("A Cybermaster regenerates only once");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void cybermasterCannotRegenerateFromKill(GameTestHelper context) {
        CybermasterEntity master = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.CYBERMASTER);
        master.setNoAi(true);
        master.hurtServer(context.getLevel(), master.damageSources().genericKill(), LETHAL);
        if (master.isAlive() || master.hasRegenerated()) {
            throw new AssertionError("/kill damage must bypass Cybermaster regeneration");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void cybermasterRegeneratedFlagSurvivesSaveAndLoad(GameTestHelper context) {
        CybermasterEntity original = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.CYBERMASTER);
        original.setNoAi(true);
        for (boolean regenerated : new boolean[]{true, false}) {
            original.setRegenerated(regenerated);
            TagValueOutput output = TagValueOutput.createWithContext(
                    ProblemReporter.DISCARDING, context.getLevel().registryAccess());
            original.saveWithoutId(output);
            CybermasterEntity reloaded = DWMEntityTypes.CYBERMASTER.create(context.getLevel(), EntitySpawnReason.LOAD);
            if (reloaded == null) {
                throw new AssertionError("Could not create a Cybermaster to load into");
            }
            reloaded.load(TagValueInput.create(
                    ProblemReporter.DISCARDING, context.getLevel().registryAccess(), output.buildResult()));
            if (reloaded.hasRegenerated() != regenerated) {
                throw new AssertionError("Regenerated=" + regenerated + " reloaded as " + reloaded.hasRegenerated());
            }
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void weaponsGradeCybermanAdaptsToRepeatedDamage(GameTestHelper context) {
        WeaponsGradeCybermanEntity cyberman =
                EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.WEAPONS_GRADE_CYBERMAN);
        cyberman.setNoAi(true);
        DamageSource hit = cyberman.damageSources().generic();

        float before = cyberman.getHealth();
        cyberman.hurtServer(context.getLevel(), hit, 6.0F);
        float firstLoss = before - cyberman.getHealth();

        cyberman.invulnerableTime = 0;
        before = cyberman.getHealth();
        cyberman.hurtServer(context.getLevel(), hit, 6.0F);
        float secondLoss = before - cyberman.getHealth();

        if (firstLoss <= 0.0F || secondLoss >= firstLoss) {
            throw new AssertionError("Second hit of the same type should do less damage: first "
                    + firstLoss + ", second " + secondLoss);
        }
        if (cyberman.adaptationAgainst(cyberman.damageSources().magic()) != 0.0F) {
            throw new AssertionError("Adaptation must not carry over to a different damage type");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 60)
    public void weaponsGradeCybermanDashesAtADistantTarget(GameTestHelper context) {
        WeaponsGradeCybermanEntity cyberman =
                EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.WEAPONS_GRADE_CYBERMAN);
        cyberman.teleportTo(context.absolutePos(new BlockPos(8, 2, 8)).getX() + 0.5,
                cyberman.getY(), context.absolutePos(new BlockPos(8, 2, 8)).getZ() + 0.5);
        Pig pig = context.spawn(EntityTypes.PIG, new BlockPos(0, 2, 0));
        pig.setNoAi(true);
        cyberman.setTarget(pig);
        context.succeedWhen(() -> context.assertTrue(
                cyberman.isBursting(),
                "Weapons-grade Cyberman should dash at a target " + cyberman.distanceTo(pig) + " blocks away"));
    }
}
