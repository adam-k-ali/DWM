package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.FirstDoctorConsoleRoomLayout;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Syncs console-facing state onto the First Doctor console block entity for client rendering.
 * Other agents may call {@link #syncVariant} / {@link #syncStabilisers} after model changes
 * initiated outside console click handlers.
 */
public final class FirstDoctorConsoleSync {
    private FirstDoctorConsoleSync() {
    }

    /**
     * Writes {@code variant} onto the interior console BE for {@code tardisId} and pushes a client update.
     */
    public static void syncVariant(
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId,
            @Nullable TardisChameleonVariant variant
    ) {
        FirstDoctorConsoleBlockEntity console = findConsole(server, tardisId);
        if (console != null && variant != null) {
            console.setSyncedVariant(variant);
        }
    }

    /**
     * Writes cloak state onto the interior console and linked exterior BEs.
     */
    public static void syncCloak(
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId,
            boolean cloaked
    ) {
        FirstDoctorConsoleBlockEntity console = findConsole(server, tardisId);
        if (console != null) {
            console.setSyncedCloaked(cloaked);
        }
        TardisBlockEntity exterior = findExterior(server, tardisId);
        if (exterior != null) {
            exterior.setSyncedCloaked(cloaked);
        }
    }

    /**
     * Writes stabilisers enabled state onto the interior console BE and pushes a client update.
     */
    public static void syncStabilisers(
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId,
            boolean enabled
    ) {
        FirstDoctorConsoleBlockEntity console = findConsole(server, tardisId);
        if (console != null) {
            console.setSyncedStabilisersEnabled(enabled);
        }
    }

    private static @Nullable FirstDoctorConsoleBlockEntity findConsole(
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId
    ) {
        if (server == null || tardisId == null) {
            return null;
        }
        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return null;
        }
        BlockPos consolePos = TardisPlotAllocator.plotOrigin(tardisId)
                .offset(FirstDoctorConsoleRoomLayout.LOCAL_CONSOLE);
        if (interior.getBlockEntity(consolePos) instanceof FirstDoctorConsoleBlockEntity console) {
            return console;
        }
        return null;
    }

    private static @Nullable TardisBlockEntity findExterior(
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId
    ) {
        if (server == null || tardisId == null) {
            return null;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null || !model.hasExteriorLocation || model.exteriorDimension == null) {
            return null;
        }
        Identifier identifier = Identifier.tryParse(model.exteriorDimension);
        if (identifier == null) {
            return null;
        }
        ServerLevel exterior = server.getLevel(ResourceKey.create(Registries.DIMENSION, identifier));
        if (exterior == null) {
            return null;
        }
        BlockPos pos = new BlockPos(model.exteriorX, model.exteriorY, model.exteriorZ);
        if (exterior.getBlockEntity(pos) instanceof TardisBlockEntity tardis) {
            return tardis;
        }
        return null;
    }

}
