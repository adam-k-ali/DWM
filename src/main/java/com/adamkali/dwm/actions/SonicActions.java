package com.adamkali.dwm.actions;

import com.adamkali.dwm.analytics.AnalyticsManager;
import com.adamkali.dwm.analytics.DWMStatistics;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
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

        this.entityActions.put(EntityType.SLIME, (entity, player, level, hand) -> {
            RegistryEntry<DamageType> damageType = level.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getEntry(DamageTypes.PLAYER_ATTACK.getValue()).orElseThrow();
            DamageSource damageSource = new DamageSource(damageType, player, entity);
            entity.damage(level, damageSource, 1.0F);
        });

        this.entityActions.put(EntityType.SHEEP, (entity, player, level, hand) -> {
            SheepEntity sheepEntity = (SheepEntity) entity;
            if (!sheepEntity.isShearable()) {
                return;
            }

            sheepEntity.sheared(level, SoundCategory.PLAYERS, player.getStackInHand(hand));
        });

        BlockModificationAction blockBreakAction = (level, blockPos, blockState, player) -> {
            level.breakBlock(blockPos, false, player);
        };

        this.blockActions.put(Blocks.GLASS, blockBreakAction);
        this.blockActions.put(Blocks.GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.WHITE_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.ORANGE_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.MAGENTA_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.LIGHT_BLUE_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.YELLOW_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.LIME_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.PINK_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.GRAY_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.LIGHT_GRAY_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.CYAN_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.PURPLE_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.BLUE_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.BROWN_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.GREEN_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.RED_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.BLACK_STAINED_GLASS, blockBreakAction);
        this.blockActions.put(Blocks.WHITE_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.ORANGE_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.MAGENTA_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.YELLOW_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.LIME_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.PINK_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.GRAY_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.CYAN_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.PURPLE_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.BLUE_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.BROWN_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.GREEN_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.RED_STAINED_GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.BLACK_STAINED_GLASS_PANE, blockBreakAction);
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }

        return INSTANCE;
    }

    public void interactWithBlock(ItemUsageContext context) {
        context.getWorld().playSoundAtBlockCenter(context.getBlockPos(), DWMSounds.SONIC_SCREWDRIVER, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        Block blockClicked = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        boolean actionExists = this.blockActions.containsKey(blockClicked);
        AnalyticsManager.trackEvent(AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE, "item_name", context.getStack().getItem().getName(), "action_exists", actionExists, "block", blockClicked.getName().toString());
        if (actionExists) {
            if (context.getPlayer() != null) {
                context.getPlayer().incrementStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
            }
            this.blockActions.get(blockClicked).perform(context.getWorld(), context.getBlockPos(), context.getWorld().getBlockState(context.getBlockPos()), context.getPlayer());
        }
    }

    public void interactWithEntity(ItemStack itemStack, LivingEntity entity, PlayerEntity player, Hand hand) {
        World level = entity.getEntityWorld();
        level.playSoundAtBlockCenter(player.getBlockPos(), DWMSounds.SONIC_SCREWDRIVER, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        if (level.isClient) {
            return;
        }

        ServerWorld serverWorld = Objects.requireNonNull(level.getServer()).getWorld(level.getRegistryKey());
        boolean actionExists = this.entityActions.containsKey(entity.getType());
        AnalyticsManager.trackEvent(AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE, "item_name", itemStack.getItem().getName(), "action_exists", actionExists, "entity_type", entity.getType().toString());
        if (actionExists) {
            player.incrementStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
            this.entityActions.get(entity.getType()).perform(entity, player, serverWorld, hand);
        }
    }


}
