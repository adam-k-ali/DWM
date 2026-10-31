package com.adamkali.dwm.item;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.K9Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/** Using a K-9 unit on a block activates it: a K9 spawns tamed to the player and the unit is consumed. */
public class K9UnitItem extends Item {
    public K9UnitItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        K9Entity k9 = DWMEntityTypes.K9.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (k9 == null) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        k9.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
        k9.tame(player);
        k9.setOrderedToSit(false);
        if (!serverLevel.addFreshEntity(k9)) {
            return InteractionResult.FAIL;
        }
        context.getItemInHand().consume(1, player);
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
        tooltip.accept(Component.translatable("item.dwm.k9_unit.tooltip"));
    }
}
