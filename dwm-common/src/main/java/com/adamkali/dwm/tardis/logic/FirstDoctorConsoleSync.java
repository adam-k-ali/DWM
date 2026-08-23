package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.block.entities.TardisBlockEntity;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
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
 * Call {@link #syncFromModel} after model changes initiated outside the console tick refresh.
 */
public final class FirstDoctorConsoleSync {
    private FirstDoctorConsoleSync() {
    }

    /**
     * Rebuilds the console display snapshot from {@code tardisId}'s model (keeping the current
     * environment reading when present) and pushes cloak onto the linked exterior BE.
     */
    public static void syncFromModel(
            @Nullable MinecraftServer server,
            @Nullable UUID tardisId
    ) {
        if (server == null || tardisId == null) {
            return;
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        if (model == null) {
            return;
        }
        FirstDoctorConsoleBlockEntity console = findConsole(server, tardisId);
        if (console != null) {
            ExteriorEnvironmentReadout.Reading reading = console.syncedDisplay().reading();
            console.setSyncedDisplay(ConsoleDisplayState.from(model, reading));
        }
        TardisBlockEntity exterior = findExterior(server, tardisId);
        if (exterior != null) {
            exterior.setSyncedCloaked(model.cloaked);
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
