package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.soto.SotoExteriorSyncService;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TardisLogic {
    public static ActionResult toggleDoor(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return ActionResult.FAIL;
        }

        float doorSwing = tardis.doorState.doorSwing;
        if (doorSwing > 0.0f && doorSwing < 1.0f) {
            return ActionResult.PASS;
        }

        tardis.doorState.isOpen = !tardis.doorState.isOpen;
        tardis.markDirty();
        SotoExteriorSyncService.markDirty(tardisId);
        return ActionResult.SUCCESS;
    }

    public static TardisDoorState getDoorState(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.doorState;
    }

    public static void updateDoorState(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }
        float doorSwing = tardis.doorState.doorSwing;
        if (tardis.doorState.isOpen) {
            doorSwing = Math.min(doorSwing + 0.05f, 1f);
        } else {
            doorSwing = Math.max(doorSwing - 0.05f, 0f);
        }
        tardis.doorState.doorSwing = doorSwing;
        tardis.markDirty();
    }

    public static void setVariant(UUID tardisId, TardisChameleonVariant variant) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return;
        }

        tardis.variant = variant;
        tardis.markDirty();
        SotoExteriorSyncService.markDirty(tardisId);
    }

    public static TardisChameleonVariant getVariant(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.variant;
    }

    /**
     * Cycles {@link TardisDataModel#selectedBiome} through biomes tagged for the TARDIS exterior
     * dimension. Returns the newly selected biome id, or empty if none are available.
     */
    public static Optional<Identifier> cycleSelectedBiome(UUID tardisId, MinecraftServer server) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null || server == null) {
            return Optional.empty();
        }
        Registry<Biome> biomes = server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        List<RegistryKey<Biome>> list =
                BiomeSelectorLogic.biomesForDimension(biomes, tardis.exteriorDimension);
        Optional<Identifier> next = BiomeSelectorLogic.nextBiome(tardis.selectedBiome, list);
        if (next.isEmpty()) {
            return Optional.empty();
        }
        tardis.selectedBiome = next.get().toString();
        tardis.markDirty();
        return next;
    }

    public static @Nullable String getSelectedBiome(UUID tardisId) {
        TardisDataModel tardis = TardisDataLoader.get(tardisId);
        if (tardis == null) {
            return null;
        }
        return tardis.selectedBiome;
    }
}
