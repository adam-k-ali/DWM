package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Syncs chameleon variant onto the First Doctor console block entity for hologram rendering.
 * Other agents may call {@link #syncVariant} after variant changes initiated outside
 * {@link TardisLogic#setVariant(UUID, TardisChameleonVariant, MinecraftServer)}.
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
        if (server == null || tardisId == null || variant == null) {
            return;
        }
        ServerLevel interior = server.getLevel(TardisDimensions.TARDIS_WORLD_KEY);
        if (interior == null) {
            return;
        }
        BlockPos consolePos = TardisPlotAllocator.plotOrigin(tardisId).offset(5, 1, 5);
        if (interior.getBlockEntity(consolePos) instanceof FirstDoctorConsoleBlockEntity console) {
            console.setSyncedVariant(variant);
        }
    }
}
