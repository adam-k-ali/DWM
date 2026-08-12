package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.network.RequestPortalStreamC2SPayload;
import com.adamkali.dwm.network.SyncPortalChunkS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntityRemoveS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntitySpawnS2CPayload;
import com.adamkali.dwm.network.SyncPortalEntityUpdateS2CPayload;
import com.adamkali.dwm.network.SyncPortalMetaS2CPayload;
import com.adamkali.dwm.network.UnloadPortalChunkS2CPayload;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.render.soto.ghost.SotoGhostMeshCache;
import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.adamkali.dwm.tardis.portal.PortalShellState;
import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side facade keyed by (PortalStreamKind, UUID) that owns portal meta and
 * delegates terrain/entities to kind-keyed SotoGhostExterior / SotoGhostMeshCache.
 */
public final class PortalSceneStore {
    private static final long REQUEST_COOLDOWN_MS = 2000L;

    public record SceneKey(PortalStreamKind kind, UUID tardisId) {
    }

    private record MetaEntry(int revision, PortalShellState shell, PortalAtmosphere atmosphere) {
    }

    private static final Map<SceneKey, MetaEntry> META = new ConcurrentHashMap<>();
    private static final Map<SceneKey, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();

    private PortalSceneStore() {
    }

    public static void applyMeta(SyncPortalMetaS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null) {
            return;
        }
        SceneKey key = new SceneKey(payload.kind(), payload.tardisId());
        MetaEntry existing = META.get(key);
        if (existing != null && payload.revision() < existing.revision()) {
            return;
        }
        PortalShellState shell = payload.shellState();
        PortalAtmosphere atmosphere = payload.atmosphere() == null ? PortalAtmosphere.DEFAULT : payload.atmosphere();
        META.put(key, new MetaEntry(payload.revision(), shell, atmosphere));
        LAST_REQUEST_MS.remove(key);
        PortalFrameCache.markDirty(payload.kind(), payload.tardisId());
    }

    public static PortalShellState getShell(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return null;
        }
        MetaEntry entry = META.get(new SceneKey(kind, tardisId));
        return entry == null ? null : entry.shell();
    }

    public static PortalAtmosphere getAtmosphere(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return null;
        }
        MetaEntry entry = META.get(new SceneKey(kind, tardisId));
        return entry == null ? null : entry.atmosphere();
    }

    public static void applyChunk(SyncPortalChunkS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null) {
            return;
        }
        SotoGhostExterior.applyChunk(payload.kind(), payload);
        PortalFrameCache.markDirty(payload.kind(), payload.tardisId());
    }

    public static void unloadChunk(UnloadPortalChunkS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null) {
            return;
        }
        SotoGhostExterior.unloadChunk(payload.kind(), payload.tardisId(), payload.chunkX(), payload.chunkZ());
        PortalFrameCache.markDirty(payload.kind(), payload.tardisId());
    }

    public static void applyEntitySpawn(SyncPortalEntitySpawnS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null) {
            return;
        }
        SotoGhostExterior.applyEntitySpawn(payload.kind(), payload);
        PortalPerfStats.noteEntitySpawn();
        PortalFrameCache.markDirty(payload.kind(), payload.tardisId());
    }

    public static void applyEntityUpdate(SyncPortalEntityUpdateS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null) {
            return;
        }
        SotoGhostExterior.applyEntityUpdate(payload.kind(), payload);
        PortalPerfStats.noteEntityUpdate();
        PortalFrameCache.markDirty(payload.kind(), payload.tardisId());
    }

    public static void removeEntity(SyncPortalEntityRemoveS2CPayload payload) {
        if (payload == null || payload.tardisId() == null || payload.kind() == null) {
            return;
        }
        SotoGhostExterior.removeEntity(payload.kind(), payload.tardisId(), payload.entityUuid());
        PortalPerfStats.noteEntityRemove();
        PortalFrameCache.markDirty(payload.kind(), payload.tardisId());
    }

    public static void requestIfNeeded(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        SceneKey key = new SceneKey(kind, tardisId);
        long now = System.currentTimeMillis();
        Long last = LAST_REQUEST_MS.get(key);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getConnection() == null) {
            return;
        }
        LAST_REQUEST_MS.put(key, now);
        ClientPlayNetworking.send(new RequestPortalStreamC2SPayload(kind, tardisId));
    }

    public static void invalidate(PortalStreamKind kind, UUID tardisId) {
        if (kind == null || tardisId == null) {
            return;
        }
        SceneKey key = new SceneKey(kind, tardisId);
        META.remove(key);
        LAST_REQUEST_MS.remove(key);
        SotoGhostExterior.invalidate(kind, tardisId);
        PortalFrameCache.markDirty(kind, tardisId);
    }

    public static void invalidateAll() {
        META.clear();
        LAST_REQUEST_MS.clear();
        SotoGhostExterior.invalidateAll();
        PortalFrameCache.markAllDirty();
    }

    public static void clientTick() {
        SotoGhostExterior.clientTick();
    }
}
