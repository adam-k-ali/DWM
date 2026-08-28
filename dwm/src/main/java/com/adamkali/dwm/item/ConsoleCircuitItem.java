package com.adamkali.dwm.item;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisCircuit;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.CircuitInstallLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

/**
 * Craftable console circuit. Use on the matching console control to install, or
 * (remote summon only) use while a Stattenheim remote is in the other hand.
 */
public class ConsoleCircuitItem extends Item {
    private final TardisCircuit circuit;

    public ConsoleCircuitItem(TardisCircuit circuit, Properties properties) {
        super(properties);
        this.circuit = circuit;
    }

    public TardisCircuit circuit() {
        return circuit;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (circuit != TardisCircuit.REMOTE_SUMMON) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        boolean otherHandIsRemote = CircuitInstallLogic.otherHandIsRemote(player, hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        TardisDataModel owned = TardisDataLoader.findOwnedBy(player.getUUID()).orElse(null);
        CircuitInstallLogic.Result result = CircuitInstallLogic.evaluateRemote(
                owned,
                player.getUUID(),
                circuit,
                otherHandIsRemote
        );
        CircuitInstallLogic.apply(
                result,
                player,
                stack,
                circuit,
                Component.translatable("item.dwm.stattenheim_remote"),
                owned
        );
        return InteractionResult.CONSUME;
    }
}
