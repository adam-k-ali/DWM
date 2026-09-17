package com.adamkali.dwm.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Handheld environmental radiation meter. Its synchronized component drives the item screen.
 */
public class RadiationMeterItem extends Item {
    static final int UPDATE_INTERVAL_TICKS = 10;

    public RadiationMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            ServerLevel level,
            Entity owner,
            @Nullable EquipmentSlot slot
    ) {
        if (!(owner instanceof Player) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)) {
            return;
        }
        if (stack.has(DWMDataComponents.RADIATION_LEVEL)
                && owner.tickCount % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }

        updateReading(stack, RadiationMeterReadout.percent(level, owner.blockPosition()));
    }

    static boolean updateReading(ItemStack stack, int percent) {
        int clamped = clampReading(percent);
        if (!shouldUpdate(stack.get(DWMDataComponents.RADIATION_LEVEL), clamped)) {
            return false;
        }
        stack.set(DWMDataComponents.RADIATION_LEVEL, clamped);
        return true;
    }

    static int clampReading(int percent) {
        return Math.max(0, Math.min(100, percent));
    }

    static boolean shouldUpdate(@Nullable Integer current, int next) {
        return current == null || current != next;
    }
}
