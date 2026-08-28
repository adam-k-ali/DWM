package com.adamkali.dwm.item;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.TardisBlock;
import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.block.entities.TardisInteriorDoorBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.DoorLockLogic;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.TardisOwnershipLogic;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Owner / handshake / door-mode decisions for sonic use on a TARDIS.
 */
public final class SonicTardisLogic {
    private SonicTardisLogic() {
    }

    public enum Target {
        EXTERIOR,
        INTERIOR_DOOR,
        CONSOLE
    }

    public enum Decision {
        NOT_RECOGNISED,
        HANDSHAKE_ONLY,
        HANDSHAKE_THEN_SEAL,
        HANDSHAKE_THEN_SCAN,
        SEAL,
        SCAN,
        WRONG_SETTING,
        IGNORE
    }

    public static boolean isDoor(Target target) {
        return target == Target.EXTERIOR || target == Target.INTERIOR_DOOR;
    }

    /**
     * Resolves what a sonic use should do. Companion bound keys never grant sonic Seal / Scan / Ping.
     */
    public static Decision decide(
            @Nullable TardisDataModel model,
            @Nullable UUID playerUuid,
            @Nullable SonicState sonicState,
            Target target
    ) {
        if (model == null || playerUuid == null) {
            return Decision.NOT_RECOGNISED;
        }
        if (!TardisOwnershipLogic.isOwner(model, playerUuid)) {
            return Decision.NOT_RECOGNISED;
        }
        SonicState state = sonicState == null ? SonicState.craftedOpenOnly() : sonicState;
        SonicFieldMode selected = state.selected() == null ? SonicFieldMode.OPEN : state.selected();
        boolean handshake = SonicStateLogic.needsHandshake(state);
        if (handshake) {
            if (isDoor(target) && selected == SonicFieldMode.SEAL) {
                return Decision.HANDSHAKE_THEN_SEAL;
            }
            if (isDoor(target) && selected == SonicFieldMode.SCAN) {
                return Decision.HANDSHAKE_THEN_SCAN;
            }
            return Decision.HANDSHAKE_ONLY;
        }
        if (!isDoor(target)) {
            return Decision.IGNORE;
        }
        if (selected == SonicFieldMode.SEAL) {
            return Decision.SEAL;
        }
        if (selected == SonicFieldMode.SCAN) {
            return Decision.SCAN;
        }
        return Decision.WRONG_SETTING;
    }

    public static boolean isTardisTarget(Block block) {
        return block instanceof TardisBlock
                || block instanceof TardisInteriorDoorBlock
                || block instanceof FirstDoctorConsoleBlock;
    }

    /**
     * Server-side sonic use on a TARDIS exterior, interior door, or console.
     *
     * @return {@code true} when the clicked block is a TARDIS target
     */
    public static boolean useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (!isTardisTarget(block)) {
            return false;
        }
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return true;
        }

        Target target = targetOf(block);
        UUID tardisId = resolveTardisId(level, pos, state, target);
        TardisDataModel model = tardisId == null ? null : TardisDataLoader.get(tardisId);
        Decision decision = decide(model, player.getUUID(), SonicStateLogic.effective(stack), target);

        switch (decision) {
            case NOT_RECOGNISED -> player.sendOverlayMessage(
                    Component.translatable(SonicStateLogic.TARDIS_NOT_RECOGNISED_KEY));
            case HANDSHAKE_ONLY -> handshake(player, stack);
            case HANDSHAKE_THEN_SEAL -> {
                handshake(player, stack);
                DoorLockLogic.toggleForPlayer(model, player, serverLevel.getServer(), tardisId);
            }
            case HANDSHAKE_THEN_SCAN -> {
                handshake(player, stack);
                sendScan(player, serverLevel, tardisId, model);
            }
            case SEAL -> DoorLockLogic.toggleForPlayer(model, player, serverLevel.getServer(), tardisId);
            case SCAN -> sendScan(player, serverLevel, tardisId, model);
            case WRONG_SETTING -> player.sendOverlayMessage(
                    Component.translatable(SonicStateLogic.WRONG_SETTING_SEAL_OR_SCAN_KEY));
            case IGNORE -> {
            }
        }
        return true;
    }

    private static void handshake(Player player, ItemStack stack) {
        SonicStateLogic.pairWithTardis(stack);
        player.sendOverlayMessage(Component.translatable(SonicStateLogic.TARDIS_PAIRED_KEY));
    }

    private static void sendScan(
            Player player,
            ServerLevel serverLevel,
            @Nullable UUID tardisId,
            @Nullable TardisDataModel model
    ) {
        ScanSample sample = sampleExterior(serverLevel, tardisId, model);
        player.sendOverlayMessage(SonicScanLogic.overlay(model, sample.reading(), sample.waterlogged()));
    }

    private static ScanSample sampleExterior(
            ServerLevel origin,
            @Nullable UUID tardisId,
            @Nullable TardisDataModel model
    ) {
        if (tardisId == null || model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return ScanSample.none();
        }
        boolean inFlight = TardisTravelService.isTraveling(tardisId);
        Identifier identifier = Identifier.tryParse(model.exteriorDimension);
        if (identifier == null) {
            return ScanSample.none();
        }
        ServerLevel exterior = origin.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, identifier));
        BlockPos exteriorPos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        ExteriorEnvironmentReadout.Reading reading =
                ExteriorEnvironmentReadout.sample(exterior, exteriorPos, inFlight);
        boolean waterlogged = false;
        if (exterior != null && !inFlight) {
            waterlogged = ExteriorEnvironmentReadout.sampleFacts(exterior, exteriorPos).waterlogged();
        }
        return new ScanSample(reading, waterlogged);
    }

    private static Target targetOf(Block block) {
        if (block instanceof TardisInteriorDoorBlock) {
            return Target.INTERIOR_DOOR;
        }
        if (block instanceof FirstDoctorConsoleBlock) {
            return Target.CONSOLE;
        }
        return Target.EXTERIOR;
    }

    private static @Nullable UUID resolveTardisId(
            Level level,
            BlockPos pos,
            BlockState state,
            Target target
    ) {
        if (target == Target.EXTERIOR && level.getBlockEntity(pos) instanceof TardisBlockEntity exterior) {
            return exterior.getTardisId();
        }
        if (target == Target.INTERIOR_DOOR) {
            TardisInteriorDoorBlockEntity interior = TardisInteriorDoorBlock.getOriginEntity(level, pos, state);
            return interior == null ? null : interior.getTardisId();
        }
        if (level.getBlockEntity(pos) instanceof FirstDoctorConsoleBlockEntity console) {
            return console.getTardisId();
        }
        return null;
    }

    private record ScanSample(ExteriorEnvironmentReadout.Reading reading, boolean waterlogged) {
        static ScanSample none() {
            return new ScanSample(ExteriorEnvironmentReadout.Reading.none(), false);
        }
    }
}
