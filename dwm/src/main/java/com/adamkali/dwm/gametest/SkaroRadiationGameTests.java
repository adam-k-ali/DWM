package com.adamkali.dwm.gametest;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.world.SkaroDimensions;
import com.adamkali.dwm.world.radiation.RadiationExposureLogic;
import com.adamkali.dwm.world.radiation.RadiationExposureService;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;

public class SkaroRadiationGameTests {
    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void unprotectedPlayerTakesRadiationDamageOnSkaro(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        Player player = placeSurvivalPlayer(context, skaro);
        float ambient = ambientAt(skaro, player);
        if (ambient <= 0.0F) {
            throw new AssertionError("Expected Skaro biome ambient radiation > 0 at test spawn");
        }
        float before = player.getHealth();
        clearHurtCooldown(player);
        RadiationExposureService.applyExposure(player, skaro);
        if (!(player.getHealth() < before)) {
            throw new AssertionError("Expected unprotected player to take Skaro radiation damage, health stayed "
                    + player.getHealth() + " (before " + before + ", ambient " + ambient + ")");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void partialSuitReducesExposurePredictably(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        Player player = placeSurvivalPlayer(context, skaro);
        float ambient = ambientAt(skaro, player);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(DWMItems.PROTECTIVE_SUIT_HELMET));
        float expectedEffective = RadiationExposureLogic.effectiveExposure(ambient, 1);
        float before = player.getHealth();
        clearHurtCooldown(player);
        RadiationExposureService.applyExposure(player, skaro);
        float after = player.getHealth();
        if (expectedEffective > 0.0F) {
            if (!(after < before)) {
                throw new AssertionError("Expected one suit piece to leave residual radiation damage at ambient "
                        + ambient);
            }
            float lost = before - after;
            float expectedDamage = RadiationExposureLogic.damageAmount(expectedEffective);
            if (Math.abs(lost - expectedDamage) > 0.05F) {
                throw new AssertionError("Expected ~" + expectedDamage + " damage with one piece, lost " + lost);
            }
        } else if (after != before) {
            throw new AssertionError("Expected one suit piece to fully block low ambient " + ambient);
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void fullSuitPreventsAmbientRadiationDamage(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        Player player = placeSurvivalPlayer(context, skaro);
        equipFullSuit(player);
        float before = player.getHealth();
        clearHurtCooldown(player);
        RadiationExposureService.applyExposure(player, skaro);
        if (player.getHealth() != before) {
            throw new AssertionError("Expected full protective suit to prevent ambient Skaro radiation damage");
        }
        context.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void leavingSkaroStopsExposure(GameTestHelper context) {
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

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 200)
    public void creativePlayersAreIgnored(GameTestHelper context) {
        ServerLevel skaro = requireSkaro(context);
        Player player = context.makeMockPlayer(GameType.CREATIVE);
        teleportToSkaroSurface(skaro, player);
        float before = player.getHealth();
        clearHurtCooldown(player);
        RadiationExposureService.applyExposure(player, skaro);
        if (player.getHealth() != before) {
            throw new AssertionError("Expected creative players to ignore Skaro radiation");
        }
        context.succeed();
    }

    private static float ambientAt(ServerLevel skaro, Player player) {
        return RadiationExposureLogic.ambientForBiome(
                skaro.getBiome(player.blockPosition()).unwrapKey().orElse(null)
        );
    }

    private static ServerLevel requireSkaro(GameTestHelper context) {
        ServerLevel skaro = context.getLevel().getServer().getLevel(SkaroDimensions.SKARO_WORLD_KEY);
        if (skaro == null) {
            throw new AssertionError("Expected dwm:skaro level to be loaded");
        }
        return skaro;
    }

    private static Player placeSurvivalPlayer(GameTestHelper context, ServerLevel skaro) {
        Player player = context.makeMockPlayer(GameType.SURVIVAL);
        player.setHealth(20.0F);
        teleportToSkaroSurface(skaro, player);
        return player;
    }

    private static void teleportToSkaroSurface(ServerLevel skaro, Player player) {
        int x = 0;
        int z = 0;
        skaro.getChunk(x >> 4, z >> 4);
        int y = skaro.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        player.snapTo(x + 0.5, y, z + 0.5);
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
