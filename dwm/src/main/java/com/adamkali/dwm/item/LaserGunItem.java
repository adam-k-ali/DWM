package com.adamkali.dwm.item;

import com.adamkali.dwm.entity.DalekLaserEntity;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class LaserGunItem extends Item {
    public LaserGunItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(
            @NonNull Level level,
            @NonNull Player player,
            @NonNull InteractionHand hand
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        Vec3 muzzle = LaserGunLogic.muzzlePosition(player.getEyePosition(), player.getLookAngle());
        DalekLaserEntity laser = new DalekLaserEntity(level, player);
        laser.setPos(muzzle.x, muzzle.y, muzzle.z);
        laser.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, LaserGunLogic.LASER_SPEED, 0.0F);
        level.addFreshEntity(laser);
        level.playSound(null, muzzle.x, muzzle.y, muzzle.z, DWMSounds.DALEK_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.1F);
        player.getCooldowns().addCooldown(stack, LaserGunLogic.COOLDOWN_TICKS);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack stack,
            @NonNull TooltipContext context,
            @NonNull TooltipDisplay display,
            @NonNull Consumer<Component> tooltip,
            @NonNull TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("item.dwm.laser_gun.tooltip"));
    }
}
