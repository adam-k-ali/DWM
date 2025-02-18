package com.adamkali.dwm.actions;

import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.HashMap;
import java.util.Objects;

public class SonicActions {
    private static SonicActions INSTANCE;

    private final HashMap<Block, BlockModificationAction> blockActions = new HashMap<>();
    private final HashMap<EntityType<?>, EntityInteractionAction> entityActions = new HashMap<>();


    private SonicActions() {
        this.blockActions.put(Blocks.TNT, (level, blockPos, blockState, player) -> {
            level.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            TntBlock.primeTnt(level, blockPos);
        });
        this.blockActions.put(Blocks.IRON_DOOR, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(DoorBlock.OPEN);
            DoorBlock block = (DoorBlock) blockState.getBlock();
            boolean open = newState.get(DoorBlock.OPEN);
            level.setBlockState(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? block.getBlockSetType().doorOpen() : block.getBlockSetType().doorClose(), SoundCategory.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.emitGameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        });
        this.blockActions.put(Blocks.IRON_TRAPDOOR, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(TrapdoorBlock.OPEN);
            TrapdoorBlock block = (TrapdoorBlock) blockState.getBlock();
            boolean open = newState.get(TrapdoorBlock.OPEN);
            level.setBlockState(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.emitGameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        });

        this.entityActions.put(EntityType.SLIME, (entity, player, level) -> {
            RegistryEntry<DamageType> damageType = level.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getEntry(DamageTypes.PLAYER_ATTACK.getValue()).orElseThrow();
            DamageSource damageSource = new DamageSource(damageType, player, entity);
            entity.damage(level, damageSource, 1.0F);
        });
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }

        return INSTANCE;
    }

    public void tryPerformAction(ItemUsageContext context) {
        context.getWorld().playSoundAtBlockCenter(context.getBlockPos(), DWMSounds.SONIC_SCREWDRIVER, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        Block blockClicked = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (this.blockActions.containsKey(blockClicked)) {
            this.blockActions.get(blockClicked).perform(context.getWorld(), context.getBlockPos(), context.getWorld().getBlockState(context.getBlockPos()), context.getPlayer());
        }
    }

    public void interactWithEntity(LivingEntity entity, PlayerEntity player) {
        World level = entity.getEntityWorld();
        level.playSoundAtBlockCenter(player.getBlockPos(), DWMSounds.SONIC_SCREWDRIVER, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        if (level.isClient) {
            return;
        }

        ServerWorld serverWorld = Objects.requireNonNull(level.getServer()).getWorld(level.getRegistryKey());
        if (this.entityActions.containsKey(entity.getType())) {
            this.entityActions.get(entity.getType()).perform(entity, player, serverWorld);
        }
    }


}
