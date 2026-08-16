package com.adamkali.dwm.command;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisInteriorService;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Registers {@code /tardis rebuild} and ops {@code /tardis claim}.
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
        if (!TardisDimensions.isTardisWorld(player.level())) {
            source.sendFailure(Component.translatable("dwm.command.tardis.claim.not_inside"));
            return 0;
        }
        Optional<TardisDataModel> atPos = TardisDataLoader.findAtInteriorPos(player.blockPosition());
        if (atPos.isEmpty()) {
            source.sendFailure(Component.translatable("dwm.command.tardis.claim.not_inside"));
            return 0;
        }
        return applyClaim(source, player.getUUID(), atPos.get().uuid);
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
