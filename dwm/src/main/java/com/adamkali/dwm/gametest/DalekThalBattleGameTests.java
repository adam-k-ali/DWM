package com.adamkali.dwm.gametest;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.world.event.DalekThalBattleService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Mob;

public class DalekThalBattleGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 100)
    public void armedThalBoltDamagesDalekButNotThal(GameTestHelper context) {
        ThalEntity shooter = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        shooter.setArmed(true);
        shooter.setNoAi(true);
        DalekEntity dalek = context.spawn(DWMEntityTypes.DALEK, new BlockPos(2, 1, 6));
        dalek.setNoAi(true);
        ThalEntity bystander = context.spawn(DWMEntityTypes.THAL, new BlockPos(6, 1, 2));
        bystander.setNoAi(true);
        float dalekStart = dalek.getHealth();
        float thalStart = bystander.getHealth();
        shooter.performRangedAttack(dalek, 1.0F);
        context.succeedWhen(() -> {
            context.assertTrue(dalek.getHealth() < dalekStart, "Thal bolt should hurt the Dalek");
            context.assertTrue(bystander.getHealth() == thalStart, "Thal bolt must not hurt a Thal");
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void battleStartSpawnsSquadsAndStopCleansUp(GameTestHelper context) {
        ThalEntity resident = EntityDefinitionGameTestSupport.spawnOnGrass(context, DWMEntityTypes.THAL);
        resident.setNoAi(true);
        BlockPos center = context.absolutePos(new BlockPos(2, 1, 2));
        var area = new net.minecraft.world.phys.AABB(center).inflate(100);
        var before = new java.util.HashSet<>(context.getLevel().getEntitiesOfClass(DalekEntity.class, area));
        int daleks = DalekThalBattleService.start(context.getLevel(), center, 0L);
        context.assertTrue(daleks > 0, "A skirmish should spawn at least one Dalek");
        context.assertTrue(DalekThalBattleService.isActive(), "Battle should be active");
        context.assertTrue(resident.isArmed(), "Nearby resident Thal should be armed");
        DalekThalBattleService.stopAll(1L);
        context.assertTrue(!DalekThalBattleService.isActive(), "stopAll should end the battle");
        context.assertTrue(!resident.isArmed(), "Resident Thal should be disarmed afterwards");
        context.assertTrue(
                context.getLevel().getEntitiesOfClass(DalekEntity.class, area).stream()
                        .filter(d -> !before.contains(d))
                        .noneMatch(Mob::isAlive),
                "Battle Daleks should be removed");
        context.succeed();
    }
}
