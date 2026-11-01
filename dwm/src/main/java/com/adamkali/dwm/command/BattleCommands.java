package com.adamkali.dwm.command;

import com.adamkali.dwm.world.event.DalekThalBattleService;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Registers ops-only {@code /dwm battle start|stop} for the Dalek/Thal skirmish event.
 */
public final class BattleCommands {
    private BattleCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> register(dispatcher));
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dwm")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("battle")
                                .then(Commands.literal("start").executes(ctx -> start(ctx.getSource())))
                                .then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource())))
                        )
        );
    }

    private static int start(CommandSourceStack source) {
        if (DalekThalBattleService.isActive()) {
            source.sendFailure(Component.translatable("dwm.command.battle.already_running"));
            return 0;
        }
        int daleks = DalekThalBattleService.start(source.getLevel(), BlockPos.containing(source.getPosition()), source.getServer().getTickCount());
        if (daleks == 0) {
            source.sendFailure(Component.translatable("dwm.command.battle.failed"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("dwm.command.battle.started", daleks), true);
        return daleks;
    }

    private static int stop(CommandSourceStack source) {
        int stopped = DalekThalBattleService.stopAll(source.getServer().getTickCount());
        if (stopped == 0) {
            source.sendFailure(Component.translatable("dwm.command.battle.none"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("dwm.command.battle.stopped"), true);
        return stopped;
    }
}
