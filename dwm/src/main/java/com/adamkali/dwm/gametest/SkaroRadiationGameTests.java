package com.adamkali.dwm.gametest;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.world.radiation.RadiationExposureLogic;
import com.adamkali.dwm.world.radiation.RadiationExposureService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * In-world radiation damage/mitigation. Uses ambient overrides because the GameTest server
 * does not always load custom dimensions such as {@code dwm:skaro}.
 */
public class SkaroRadiationGameTests {
    private static final float TEST_AMBIENT = RadiationExposureLogic.IRRADIATED_WASTES;

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void unprotectedPlayerTakesRadiationDamage(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setHealth(20.0F);
        clearHurtCooldown(player);
        float before = player.getHealth();
        RadiationExposureService.applyExposure(player, level, TEST_AMBIENT);
        if (!(player.getHealth() < before)) {
            throw new AssertionError("Expected unprotected player to take radiation damage, health stayed "
                    + player.getHealth() + " (before " + before + ")");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void partialSuitReducesExposurePredictably(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setHealth(20.0F);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(DWMItems.PROTECTIVE_SUIT_HELMET));
        float expectedEffective = RadiationExposureLogic.effectiveExposure(TEST_AMBIENT, 1);
        float expectedDamage = RadiationExposureLogic.damageAmount(expectedEffective);
        clearHurtCooldown(player);
        float before = player.getHealth();
        RadiationExposureService.applyExposure(player, level, TEST_AMBIENT);
        float lost = before - player.getHealth();
        if (Math.abs(lost - expectedDamage) > 0.05F) {
            throw new AssertionError("Expected ~" + expectedDamage + " damage with one suit piece, lost " + lost);
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void fullSuitPreventsAmbientRadiationDamage(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setHealth(20.0F);
        equipFullSuit(player);
        clearHurtCooldown(player);
        float before = player.getHealth();
        RadiationExposureService.applyExposure(player, level, TEST_AMBIENT);
        if (player.getHealth() != before) {
            throw new AssertionError("Expected full protective suit to prevent ambient radiation damage");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void nonSkaroDimensionStopsExposureWithoutOverride(GameTestHelper context) {
        ServerLevel overworld = context.getLevel();
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setHealth(20.0F);
        clearHurtCooldown(player);
        float before = player.getHealth();
        RadiationExposureService.applyExposure(player, overworld);
        if (player.getHealth() != before) {
            throw new AssertionError("Expected non-Skaro dimension to apply no Skaro radiation damage");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void creativePlayersAreIgnored(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        Player player = context.makeMockPlayer(GameType.CREATIVE);
        clearHurtCooldown(player);
        float before = player.getHealth();
        RadiationExposureService.applyExposure(player, level, TEST_AMBIENT);
        if (player.getHealth() != before) {
            throw new AssertionError("Expected creative players to ignore radiation exposure");
        }
        context.succeed();
    }

    private static void equipFullSuit(Player player) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(DWMItems.PROTECTIVE_SUIT_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(DWMItems.PROTECTIVE_SUIT_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(DWMItems.PROTECTIVE_SUIT_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(DWMItems.PROTECTIVE_SUIT_BOOTS));
    }

    private static void clearHurtCooldown(Player player) {
        player.invulnerableTime = 0;
        player.hurtTime = 0;
    }
}
