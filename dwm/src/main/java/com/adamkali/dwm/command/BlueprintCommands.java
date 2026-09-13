package com.adamkali.dwm.command;

import com.adamkali.dwm.blueprint.BlueprintLoadException;
import com.adamkali.dwm.blueprint.BlueprintLoader;
import com.adamkali.dwm.blueprint.BlueprintPlacer;
import com.adamkali.dwm.blueprint.BlueprintTooLargeException;
import com.adamkali.dwm.blueprint.UnknownBlueprintBlockException;
import com.adamkali.dwm.blueprint.model.Blueprint;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Registers {@code /blueprint place <name> <origin>} (ops only).
 */
public final class BlueprintCommands {
    private static final SuggestionProvider<CommandSourceStack> BLUEPRINT_NAME_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(BlueprintLoader.listBlueprintNames(), builder);

    private BlueprintCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> register(dispatcher));
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("blueprint")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("place")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests(BLUEPRINT_NAME_SUGGESTIONS)
                                        .then(Commands.argument("origin", BlockPosArgument.blockPos())
                                                .executes(ctx -> place(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "name"),
                                                        BlockPosArgument.getLoadedBlockPos(ctx, "origin")
                                                ))
                                        )
                                )
                        )
        );
    }

    private static int place(CommandSourceStack source, String name, BlockPos origin) {
        if (!BlueprintLoader.isSafeName(name)) {
            source.sendFailure(Component.translatable("dwm.command.blueprint.unsafe_name", name));
            return 0;
        }

        final Blueprint blueprint;
        try {
            blueprint = BlueprintLoader.load(name);
        } catch (BlueprintLoadException e) {
            source.sendFailure(switch (e.reason()) {
                case UNSAFE_NAME -> Component.translatable("dwm.command.blueprint.unsafe_name", name);
                case MISSING -> Component.translatable("dwm.command.blueprint.missing", name);
                case INVALID, IO_ERROR -> Component.translatable("dwm.command.blueprint.invalid", name);
            });
            return 0;
        }

        ServerLevel world = source.getLevel();
        try {
            int placed = BlueprintPlacer.place(world, origin, blueprint);
            source.sendSuccess(
                    () -> Component.translatable("dwm.command.blueprint.success", name, placed),
                    true
            );
            return Command.SINGLE_SUCCESS;
        } catch (UnknownBlueprintBlockException e) {
            source.sendFailure(Component.translatable("dwm.command.blueprint.unknown_block", e.blockId()));
            return 0;
        } catch (BlueprintTooLargeException e) {
            source.sendFailure(Component.translatable("dwm.command.blueprint.too_large", e.voxelCount()));
            return 0;
        }
    }
}
