package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.network.RequestSotoExteriorC2SPayload;
import com.adamkali.dwm.network.RequestSotoGhostC2SPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS SOTO exterior cache for shell metadata and atmosphere (Phase 0).
 * Terrain and entities come from {@link SotoGhostExterior}.
 * <p>
 * Ghost entity / block-entity draws are stubs on 26.2 until extract+submit against
 * {@link SubmitNodeCollector} and {@code CameraRenderState} is ported (old
 * {@code MultiBufferSource} BER/entity {@code render} APIs are gone).
 */
public final class SotoExteriorMeshCache {
    private static final long REQUEST_COOLDOWN_MS = 2000L;

    private static final Map<UUID, CachedSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new ConcurrentHashMap<>();

    private SotoExteriorMeshCache() {
    }

    public record ShellState(
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation
    ) {
    }

    public static boolean hasSnapshot(UUID tardisId) {
        return tardisId != null && SNAPSHOTS.containsKey(tardisId);
    }

    public static ShellState getShellState(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            requestIfNeeded(tardisId);
            return null;
        }
        return cached.shell();
    }

    public static SotoAtmosphere getAtmosphere(UUID tardisId) {
        if (tardisId == null) {
            return null;
        }
        CachedSnapshot cached = SNAPSHOTS.get(tardisId);
        if (cached == null) {
            requestIfNeeded(tardisId);
            return null;
        }
        return cached.atmosphere();
    }

    public static void applySnapshot(
            UUID tardisId,
            int revision,
            TardisChameleonVariant variant,
            float doorSwing,
            boolean isOpen,
            int exteriorRotation,
            SotoAtmosphere atmosphere
    ) {
        if (tardisId == null) {
            return;
        }
        CachedSnapshot existing = SNAPSHOTS.get(tardisId);
        if (existing != null && revision < existing.revision()) {
            return;
        }
        ShellState shell = new ShellState(
                variant == null ? TardisChameleonVariant.TT_CAPSULE : variant,
                doorSwing,
                isOpen,
                exteriorRotation
        );
        SotoAtmosphere atm = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        SNAPSHOTS.put(tardisId, new CachedSnapshot(revision, shell, atm));
        LAST_REQUEST_MS.remove(tardisId);
    }

    public static void invalidate(UUID tardisId) {
        if (tardisId != null) {
            SNAPSHOTS.remove(tardisId);
            LAST_REQUEST_MS.remove(tardisId);
            SotoGhostExterior.invalidate(tardisId);
        }
    }

    public static void invalidateAll() {
        SNAPSHOTS.clear();
        LAST_REQUEST_MS.clear();
        SotoGhostExterior.invalidateAll();
    }

    public static void renderGhostBlockEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            int light,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        Minecraft client = Minecraft.getInstance();
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        if (client == null || ghost == null) {
            return;
        }
        BlockEntityRenderDispatcher beDispatcher = client.getBlockEntityRenderDispatcher();
        if (camera != null) {
            beDispatcher.prepare(camera.position());
        }
        // TODO(soto-submit): extract+submit each BlockEntity via SubmitNodeCollector / CameraRenderState.
        for (BlockEntity blockEntity : ghost.buildRenderedBlockEntities()) {
            if (beDispatcher.getRenderer(blockEntity) == null) {
                continue;
            }
            // Matrices / light / tickDelta retained for the upcoming submit port.
            if (matrices == null || submitNodeCollector == null && light < 0 && tickDelta < 0) {
                break;
            }
        }
    }

    public static void renderGhostEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            int light,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !SotoGhostExterior.hasEntities(tardisId)) {
            return;
        }
        EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
        if (camera != null) {
            entityDispatcher.prepare(camera, client.player);
        }
        // TODO(soto-submit): extractEntity + submit against SubmitNodeCollector.
        for (SotoGhostExterior.RenderableGhostEntity ghost : SotoGhostExterior.getRenderableEntities(tardisId)) {
            Entity entity = ghost.entity();
            LerpedPose pose = ghost.pose();
            entity.setYRot(pose.yaw());
            entity.setXRot(pose.pitch());
            if (entity instanceof LivingEntity living) {
                snapLivingYaw(living, pose.yaw());
            }
            if (matrices == null || submitNodeCollector == null && light < 0 && tickDelta < 0) {
                break;
            }
        }
    }

    private static void snapLivingYaw(LivingEntity living, float yaw) {
        living.setYBodyRot(yaw);
        living.setYHeadRot(yaw);
        living.yBodyRotO = yaw;
        living.yHeadRotO = yaw;
    }

    private static void requestIfNeeded(UUID tardisId) {
        long now = System.currentTimeMillis();
        Long last = LAST_REQUEST_MS.get(tardisId);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getConnection() == null) {
            return;
        }
        LAST_REQUEST_MS.put(tardisId, now);
        ClientPlayNetworking.send(new RequestSotoExteriorC2SPayload(tardisId));
        ClientPlayNetworking.send(new RequestSotoGhostC2SPayload(tardisId));
    }

    private record CachedSnapshot(
            int revision,
            ShellState shell,
            SotoAtmosphere atmosphere
    ) {
        private CachedSnapshot {
            if (atmosphere == null) {
                atmosphere = SotoAtmosphere.DEFAULT;
            }
        }
    }
}
