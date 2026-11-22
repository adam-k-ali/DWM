package com.adamkali.dwm.command;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.interior.ars.ArsGrid;
import com.adamkali.dwm.tardis.interior.ars.ArsRegistries;
import com.adamkali.dwm.tardis.interior.ars.ArsService;
import com.adamkali.dwm.tardis.logic.FirstHourLogic;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Registers {@code /tardis rebuild}, ops {@code /tardis claim} and ops {@code /tardis ars place|remove}.
 */
public final class TardisCommands {
    private TardisCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> register(dispatcher));
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tardis")
                        .then(Commands.literal("rebuild")
                                .executes(ctx -> rebuildOwned(ctx.getSource()))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .executes(ctx -> rebuildByUuid(
                                                ctx.getSource(),
                                                UuidArgument.getUuid(ctx, "uuid")
                                        ))
                                )
                        )
                        .then(Commands.literal("ars")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("place")
                                        .then(Commands.argument("piece", IdentifierArgument.id())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                                        ctx.getSource().registryAccess()
                                                                .lookupOrThrow(ArsRegistries.PIECE).keySet(),
                                                        builder))
                                                .then(Commands.argument("cellX", IntegerArgumentType.integer(ArsGrid.MIN_CELL, ArsGrid.MAX_CELL))
                                                        .then(Commands.argument("cellZ", IntegerArgumentType.integer(ArsGrid.MIN_CELL, ArsGrid.MAX_CELL))
                                                                .executes(ctx -> arsPlace(ctx, 0))
                                                                .then(Commands.argument("rotation",
                                                                                IntegerArgumentType.integer(0, ArsGrid.ROTATION_COUNT - 1))
                                                                        .executes(ctx -> arsPlace(ctx,
                                                                                IntegerArgumentType.getInteger(ctx, "rotation")))
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("cellX", IntegerArgumentType.integer(ArsGrid.MIN_CELL, ArsGrid.MAX_CELL))
                                                .then(Commands.argument("cellZ", IntegerArgumentType.integer(ArsGrid.MIN_CELL, ArsGrid.MAX_CELL))
                                                        .executes(TardisCommands::arsRemove)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("claim")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> claimInside(ctx.getSource()))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .executes(ctx -> claimByUuid(
                                                ctx.getSource(),
                                                UuidArgument.getUuid(ctx, "uuid")
                                        ))
                                )
                        )
        );
    }

    private static int rebuildOwned(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<TardisDataModel> owned = TardisDataLoader.findOwnedBy(player.getUUID());
        if (owned.isEmpty()) {
            source.sendFailure(Component.translatable("dwm.command.tardis.rebuild.no_owned"));
            return 0;
        }
        return rebuild(source, owned.get().uuid);
    }

    private static int rebuildByUuid(CommandSourceStack source, UUID tardisId) {
        if (TardisDataLoader.get(tardisId) == null) {
            source.sendFailure(Component.translatable("dwm.command.tardis.rebuild.unknown", tardisId.toString()));
            return 0;
        }
        return rebuild(source, tardisId);
    }

    private static int rebuild(CommandSourceStack source, UUID tardisId) {
        if (TardisTravelService.isTraveling(tardisId)) {
            source.sendFailure(Component.translatable("dwm.command.tardis.rebuild.in_flight"));
            return 0;
        }
        @Nullable BlockPos entrance = TardisInteriorService.regenerateInterior(source.getServer(), tardisId);
        if (entrance == null) {
            source.sendFailure(Component.translatable("dwm.command.tardis.rebuild.failed"));
            return 0;
        }
        source.sendSuccess(
                () -> Component.translatable("dwm.command.tardis.rebuild.success", tardisId.toString()),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int claimInside(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<TardisDataModel> atPos = tardisAt(player);
        if (atPos.isEmpty()) {
            source.sendFailure(Component.translatable("dwm.command.tardis.claim.not_inside"));
            return 0;
        }
        return applyClaim(source, player.getUUID(), atPos.get().uuid);
    }

    /** The TARDIS whose interior plot the player is standing in, if any. */
    private static Optional<TardisDataModel> tardisAt(ServerPlayer player) {
        if (!TardisDimensions.isTardisWorld(player.level())) {
            return Optional.empty();
        }
        return TardisDataLoader.findAtInteriorPos(player.blockPosition());
    }

    private static int arsPlace(CommandContext<CommandSourceStack> ctx, int rotation) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Optional<TardisDataModel> tardis = tardisAt(player);
        if (tardis.isEmpty()) {
            source.sendFailure(Component.translatable("dwm.command.tardis.ars.not_inside"));
            return 0;
        }
        ArsService.Result result = ArsService.grow(
                source.getServer(),
                tardis.get().uuid,
                IdentifierArgument.getId(ctx, "piece"),
                IntegerArgumentType.getInteger(ctx, "cellX"),
                IntegerArgumentType.getInteger(ctx, "cellZ"),
                rotation,
                player.isCreative()
        );
        return report(source, result);
    }

    private static int arsRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Optional<TardisDataModel> tardis = tardisAt(player);
        if (tardis.isEmpty()) {
            source.sendFailure(Component.translatable("dwm.command.tardis.ars.not_inside"));
            return 0;
        }
        ArsService.Result result = ArsService.jettison(
                source.getServer(),
                tardis.get().uuid,
                IntegerArgumentType.getInteger(ctx, "cellX"),
                IntegerArgumentType.getInteger(ctx, "cellZ")
        );
        return report(source, result);
    }

    private static int report(CommandSourceStack source, ArsService.Result result) {
        if (!result.success()) {
            source.sendFailure(result.message());
            return 0;
        }
        source.sendSuccess(result::message, true);
        return Command.SINGLE_SUCCESS;
    }

    private static int claimByUuid(CommandSourceStack source, UUID tardisId) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return applyClaim(source, player.getUUID(), tardisId);
    }

    private static int applyClaim(CommandSourceStack source, UUID playerUuid, UUID tardisId) {
        return switch (TardisOwnershipLogic.tryForceClaim(tardisId, playerUuid)) {
            case CLAIMED -> {
                source.sendSuccess(
                        () -> Component.translatable("dwm.command.tardis.claim.success", tardisId.toString()),
                        true
                );
                ServerPlayer player = source.getPlayer();
                if (player != null) {
                    FirstHourLogic.notifyClaimed(player);
                }
                yield Command.SINGLE_SUCCESS;
            }
            case ALREADY_OWNER -> {
                source.sendSuccess(
                        () -> Component.translatable("dwm.command.tardis.claim.already_owner", tardisId.toString()),
                        true
                );
                yield Command.SINGLE_SUCCESS;
            }
            case PLAYER_OWNS_ANOTHER -> {
                source.sendFailure(Component.translatable("dwm.command.tardis.claim.already_owns_another"));
                yield 0;
            }
            case UNKNOWN, INVALID -> {
                source.sendFailure(Component.translatable("dwm.command.tardis.claim.unknown", tardisId.toString()));
                yield 0;
            }
        };
    }
}
