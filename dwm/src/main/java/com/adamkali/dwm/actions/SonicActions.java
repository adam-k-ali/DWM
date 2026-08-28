package com.adamkali.dwm.actions;

import com.adamkali.dwm.advancement.DWMCriteria;
import com.adamkali.dwm.analytics.AnalyticsManager;
import com.adamkali.dwm.analytics.DWMStatistics;
import com.adamkali.dwm.item.SonicFieldMode;
import com.adamkali.dwm.item.SonicStateLogic;
import com.adamkali.dwm.item.SonicTardisLogic;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.network.chat.Component;
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

    private final HashMap<Block, GatedBlockAction> blockActions = new HashMap<>();
    private final HashMap<EntityType<?>, GatedEntityAction> entityActions = new HashMap<>();

    private SonicActions() {
        this.blockActions.put(Blocks.TNT, new GatedBlockAction(SonicFieldMode.PRIME, (level, blockPos, blockState, player) -> {
            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            TntBlock.prime(level, blockPos);
            if (player instanceof ServerPlayer serverPlayer) {
                DWMCriteria.SONIC_PRIME.trigger(serverPlayer);
            }
        }));
        this.blockActions.put(Blocks.IRON_DOOR, new GatedBlockAction(SonicFieldMode.OPEN, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(DoorBlock.OPEN);
            DoorBlock block = (DoorBlock) blockState.getBlock();
            boolean open = newState.getValue(DoorBlock.OPEN);
            level.setBlock(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? block.type().doorOpen() : block.type().doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
            if (player instanceof ServerPlayer serverPlayer) {
                DWMCriteria.SONIC_IRON_DOOR.trigger(serverPlayer);
            }
        }));
        this.blockActions.put(Blocks.IRON_TRAPDOOR, new GatedBlockAction(SonicFieldMode.OPEN, (level, blockPos, blockState, player) -> {
            BlockState newState = blockState.cycle(TrapDoorBlock.OPEN);
            TrapDoorBlock block = (TrapDoorBlock) blockState.getBlock();
            boolean open = newState.getValue(TrapDoorBlock.OPEN);
            level.setBlock(blockPos, newState, 10);
            level.playSound(player, blockPos, open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
        }));

        this.entityActions.put(EntityTypes.SLIME, new GatedEntityAction(SonicFieldMode.DISRUPT, (entity, player, level, hand) -> {
            entity.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                DWMCriteria.SONIC_DISRUPT.trigger(serverPlayer);
            }
            return true;
        }));

        this.entityActions.put(EntityTypes.SHEEP, new GatedEntityAction(SonicFieldMode.SHEAR, (entity, player, level, hand) -> {
            Sheep sheepEntity = (Sheep) entity;
            if (!sheepEntity.readyForShearing()) {
                return false;
            }
            sheepEntity.shear(level, SoundSource.PLAYERS, player.getItemInHand(hand));
            if (player instanceof ServerPlayer serverPlayer) {
                DWMCriteria.SONIC_SHEAR.trigger(serverPlayer);
            }
            return true;
        }));

        BlockModificationAction blockBreakAction = (level, blockPos, blockState, player) -> {
            level.destroyBlock(blockPos, false, player);
            if (player instanceof ServerPlayer serverPlayer) {
                DWMCriteria.SONIC_SHATTER.trigger(serverPlayer);
            }
        };

        GatedBlockAction shatter = new GatedBlockAction(SonicFieldMode.SHATTER, blockBreakAction);
        this.blockActions.put(Blocks.GLASS, shatter);
        this.blockActions.put(Blocks.GLASS_PANE, shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.white(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.orange(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.magenta(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.lightBlue(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.yellow(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.lime(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.pink(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.gray(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.lightGray(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.cyan(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.purple(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.blue(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.brown(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.green(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.red(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS.black(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.white(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.orange(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.magenta(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.lightBlue(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.yellow(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.lime(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.pink(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.gray(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.lightGray(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.cyan(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.purple(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.blue(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.brown(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.green(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.red(), shatter);
        this.blockActions.put(Blocks.STAINED_GLASS_PANE.black(), shatter);
    }

    public static SonicActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SonicActions();
        }
        return INSTANCE;
    }

    public void interactWithBlock(UseOnContext context) {
        Level level = context.getLevel();
        level.playLocalSound(context.getClickedPos(), DWMSounds.SONIC_SCREWDRIVER, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        Block blockClicked = level.getBlockState(context.getClickedPos()).getBlock();
        if (SonicTardisLogic.isTardisTarget(blockClicked)) {
            AnalyticsManager.trackEvent(
                    AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE,
                    "item_name", context.getItemInHand().getHoverName().getString(),
                    "action_exists", true,
                    "block", blockClicked.getName().toString()
            );
            if (!level.isClientSide()) {
                SonicTardisLogic.useOn(context);
                Player player = context.getPlayer();
                if (player != null) {
                    player.awardStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
                }
            }
            return;
        }
        GatedBlockAction gated = this.blockActions.get(blockClicked);
        boolean actionExists = gated != null;
        AnalyticsManager.trackEvent(
                AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE,
                "item_name", context.getItemInHand().getHoverName().getString(),
                "action_exists", actionExists,
                "block", blockClicked.getName().toString()
        );
        if (!actionExists) {
            return;
        }
        if (level.isClientSide()) {
            return;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!gateMode(player, stack, gated.requiredMode())) {
            return;
        }
        if (player != null) {
            player.awardStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
        }
        gated.action().perform(level, context.getClickedPos(), level.getBlockState(context.getClickedPos()), player);
    }

    public void interactWithEntity(ItemStack itemStack, LivingEntity entity, Player player, InteractionHand hand) {
        Level level = entity.level();
        level.playLocalSound(player.blockPosition(), DWMSounds.SONIC_SCREWDRIVER, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        if (level.isClientSide()) {
            return;
        }

        ServerLevel serverWorld = Objects.requireNonNull(level.getServer()).getLevel(level.dimension());
        GatedEntityAction gated = this.entityActions.get(entity.getType());
        boolean actionExists = gated != null;
        AnalyticsManager.trackEvent(
                AnalyticsManager.EVENT_SONIC_SCREWDRIVER_USE,
                "item_name", itemStack.getHoverName().getString(),
                "action_exists", actionExists,
                "entity_type", entity.getType().toString()
        );
        if (!actionExists) {
            return;
        }
        if (!gateMode(player, itemStack, gated.requiredMode())) {
            return;
        }
        player.awardStat(DWMStatistics.SONIC_SCREWDRIVER_USE);
        gated.action().perform(entity, player, serverWorld, hand);
    }

    /**
     * @return true if the selected mode matches and the action may proceed
     */
    private static boolean gateMode(Player player, ItemStack stack, SonicFieldMode required) {
        if (SonicStateLogic.isUnlocked(stack, required)
                && SonicStateLogic.selected(stack) == required) {
            return true;
        }
        if (player == null) {
            return false;
        }
        Component modeName = Component.translatable(required.translationKey());
        if (!SonicStateLogic.isUnlocked(stack, required)) {
            player.sendOverlayMessage(Component.translatable(
                    SonicStateLogic.SETTING_NOT_INSTALLED_DETAIL_KEY,
                    modeName
            ));
        } else {
            player.sendOverlayMessage(Component.translatable(
                    SonicStateLogic.WRONG_SETTING_DETAIL_KEY,
                    modeName
            ));
        }
        return false;
    }

    private record GatedBlockAction(SonicFieldMode requiredMode, BlockModificationAction action) {
    }

    @FunctionalInterface
    private interface EntityActionWithResult {
        /**
         * @return false for quiet no-op (e.g. sheep not ready); true when the action applied
         */
        boolean perform(LivingEntity entity, Player player, ServerLevel level, InteractionHand hand);
    }

    private record GatedEntityAction(SonicFieldMode requiredMode, EntityActionWithResult action) {
    }
}
