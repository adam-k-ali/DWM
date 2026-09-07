package com.adamkali.dwm.world.radiation;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.world.DWMBiomeKeys;
import com.adamkali.dwm.world.SkaroDimensions;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

/**
 * Pure Skaro ambient-radiation math. No world mutation or stored dose.
 */
public final class RadiationExposureLogic {
    public static final float THAL_PLATEAU = 0.15F;
    public static final float PETRIFIED_JUNGLE = 0.40F;
    public static final float DRAMMANKIN_MOUNTAINS = 0.55F;
    public static final float DRAMMANKIN_MIRE = 0.80F;
    public static final float IRRADIATED_WASTES = 0.95F;

    /** Linear mitigation contributed by each correctly equipped suit piece. */
    public static final float MITIGATION_PER_PIECE = 0.25F;

    /** Server tick interval between ambient radiation damage checks. */
    public static final int TICK_INTERVAL = 40;

    /** Damage scale applied as {@code amount = DAMAGE_SCALE * effectiveExposure}. */
    public static final float DAMAGE_SCALE = 1.0F;

    private RadiationExposureLogic() {
    }

    public static float ambientForBiome(@Nullable ResourceKey<Biome> biome) {
        if (biome == null) {
            return 0.0F;
        }
        return ambientForBiomeId(biome.identifier());
    }

    public static float ambientForBiomeId(@Nullable Identifier biomeId) {
        if (biomeId == null) {
            return 0.0F;
        }
        if (biomeId.equals(DWMBiomeKeys.SKARO_THAL_PLATEAU.identifier())) {
            return THAL_PLATEAU;
        }
        if (biomeId.equals(DWMBiomeKeys.SKARO_PETRIFIED_JUNGLE.identifier())) {
            return PETRIFIED_JUNGLE;
        }
        if (biomeId.equals(DWMBiomeKeys.SKARO_DRAMMANKIN_MOUNTAINS.identifier())) {
            return DRAMMANKIN_MOUNTAINS;
        }
        if (biomeId.equals(DWMBiomeKeys.SKARO_DRAMMANKIN_MIRE.identifier())) {
            return DRAMMANKIN_MIRE;
        }
        if (biomeId.equals(DWMBiomeKeys.SKARO_IRRADIATED_WASTES.identifier())) {
            return IRRADIATED_WASTES;
        }
        return 0.0F;
    }

    public static int countSuitPieces(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {
        return countSuitPieces(
                itemOf(head),
                itemOf(chest),
                itemOf(legs),
                itemOf(feet)
        );
    }

    /**
     * Counts correctly slotted protective-suit pieces by item identity (unit-test friendly).
     */
    public static int countSuitPieces(@Nullable Item head, @Nullable Item chest, @Nullable Item legs, @Nullable Item feet) {
        int count = 0;
        if (head == DWMItems.PROTECTIVE_SUIT_HELMET) {
            count++;
        }
        if (chest == DWMItems.PROTECTIVE_SUIT_CHESTPLATE) {
            count++;
        }
        if (legs == DWMItems.PROTECTIVE_SUIT_LEGGINGS) {
            count++;
        }
        if (feet == DWMItems.PROTECTIVE_SUIT_BOOTS) {
            count++;
        }
        return count;
    }

    public static int countSuitPieces(Player player) {
        return countSuitPieces(
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
        );
    }

    public static float effectiveExposure(float ambient, int suitPieces) {
        return clamp01(ambient - MITIGATION_PER_PIECE * suitPieces);
    }

    public static float damageAmount(float effectiveExposure) {
        if (effectiveExposure <= 0.0F) {
            return 0.0F;
        }
        return DAMAGE_SCALE * effectiveExposure;
    }

    public static boolean shouldApplyDamage(float effectiveExposure) {
        return effectiveExposure > 0.0F;
    }

    /**
     * Environmental meter/console/sonic percent for a location (not mitigated by suit).
     */
    public static int meterPercent(float ambient) {
        return Math.round(clamp01(ambient) * 100.0F);
    }

    public static boolean isEligiblePlayer(Player player) {
        return player != null && !player.isCreative() && !player.isSpectator();
    }

    public static boolean isExposedDimension(Level level) {
        return SkaroDimensions.isSkaroWorld(level);
    }

    private static @Nullable Item itemOf(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.getItem();
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
