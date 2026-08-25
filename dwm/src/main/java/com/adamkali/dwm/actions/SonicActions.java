package com.adamkali.dwm.actions;

import com.adamkali.dwm.analytics.AnalyticsManager;
import com.adamkali.dwm.analytics.DWMStatistics;
import com.adamkali.dwm.advancement.DWMCriteria;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import java.util.HashMap;
import java.util.Objects;

public class SonicActions {
    private static SonicActions INSTANCE;

    private final HashMap<Block, BlockModificationAction> blockActions = new HashMap<>();
    private final HashMap<EntityType<?>, EntityInteractionAction> entityActions = new HashMap<>();


    private SonicActions() {
        this.blockActions.put(Blocks.TNT, (level, blockPos, blockState, player) -> {
            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            TntBlock.prime(level, blockPos);
        });
        this.blockActions.put(Blocks.IRON_DOOR, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(DoorBlock.OPEN);
            DoorBlock block = (DoorBlock) blockState.getBlock();
            boolean open = newState.getValue(DoorBlock.OPEN);
            level.setBlock(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? block.type().doorOpen() : block.type().doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
            if (player instanceof ServerPlayer serverPlayer) {
                DWMCriteria.SONIC_IRON_DOOR.trigger(serverPlayer);
            }
        });
        this.blockActions.put(Blocks.IRON_TRAPDOOR, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(TrapDoorBlock.OPEN);
            TrapDoorBlock block = (TrapDoorBlock) blockState.getBlock();
            boolean open = newState.getValue(TrapDoorBlock.OPEN);
            level.setBlock(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        });

        this.entityActions.put(EntityTypes.SLIME, (entity, player, level, hand) -> {
            entity.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
        });

        this.entityActions.put(EntityTypes.SHEEP, (entity, player, level, hand) -> {
            Sheep sheepEntity = (Sheep) entity;
            if (!sheepEntity.readyForShearing()) {
                return;
            }

            sheepEntity.shear(level, SoundSource.PLAYERS, player.getItemInHand(hand));
        });

        BlockModificationAction blockBreakAction = (level, blockPos, blockState, player) -> {
            level.destroyBlock(blockPos, false, player);
        };

        this.blockActions.put(Blocks.GLASS, blockBreakAction);
        this.blockActions.put(Blocks.GLASS_PANE, blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.white(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.orange(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.magenta(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.lightBlue(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.yellow(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.lime(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.pink(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.gray(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.lightGray(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.cyan(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.purple(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.blue(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.brown(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.green(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.red(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS.black(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.white(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.orange(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.magenta(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.lightBlue(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.yellow(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.lime(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.pink(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.gray(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.lightGray(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.cyan(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.purple(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.blue(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.brown(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.green(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.red(), blockBreakAction);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.black(), blockBreakAction);
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }

        return INSTANCE;
    }

    public void interactWithBlock(UseOnContext context) {
        context.getLevel().playLocalSound(context.getClickedPos(), DWMSounds.SONIC_SCREWDRIVER, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        Block blockClicked = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        boolean actionExists = this.blockActions.containsKey(blockClicked);
        AnalyticsManager.trackEvent(AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE, "item_name", context.getItemInHand().getHoverName().getString(), "action_exists", actionExists, "block", blockClicked.getName().toString());
        if (actionExists) {
            if (context.getPlayer() != null) {
                context.getPlayer().awardStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
            }
            this.blockActions.get(blockClicked).perform(context.getLevel(), context.getClickedPos(), context.getLevel().getBlockState(context.getClickedPos()), context.getPlayer());
        }
    }

    public void interactWithEntity(ItemStack itemStack, LivingEntity entity, Player player, InteractionHand hand) {
        Level level = entity.level();
        level.playLocalSound(player.blockPosition(), DWMSounds.SONIC_SCREWDRIVER, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        if (level.isClientSide()) {
            return;
        }

        ServerLevel serverWorld = Objects.requireNonNull(level.getServer()).getLevel(level.dimension());
        boolean actionExists = this.entityActions.containsKey(entity.getType());
        AnalyticsManager.trackEvent(AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE, "item_name", itemStack.getHoverName().getString(), "action_exists", actionExists, "entity_type", entity.getType().toString());
        if (actionExists) {
            player.awardStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
            this.entityActions.get(entity.getType()).perform(entity, player, serverWorld, hand);
        }
    }


}
