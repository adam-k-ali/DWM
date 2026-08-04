package com.adamkali.dwm.render.soto;

import com.adamkali.dwm.network.RequestSotoExteriorC2SPayload;
import com.adamkali.dwm.network.RequestSotoGhostC2SPayload;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.render.soto.ghost.SotoGhostExterior;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-TARDIS SOTO exterior cache for shell metadata and atmosphere (Phase 0).
 * Terrain and entities come from {@link SotoGhostExterior}.
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
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        if (client == null || ghost == null) {
            return;
        }
        BlockEntityRenderDispatcher beDispatcher = client.getBlockEntityRenderDispatcher();
        if (client.world != null && camera != null) {
            beDispatcher.configure(client.world, camera, client.crosshairTarget);
        }
        for (BlockEntity blockEntity : ghost.buildRenderedBlockEntities()) {
            BlockEntityRenderer<BlockEntity> renderer = beDispatcher.get(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockPos pos = blockEntity.getPos();
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }

    public static void renderGhostEntities(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !SotoGhostExterior.hasEntities(tardisId)) {
            return;
        }
        EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
        if (client.world != null && camera != null) {
            entityDispatcher.configure(client.world, camera, client.player);
        }
        for (SotoGhostExterior.RenderableGhostEntity ghost : SotoGhostExterior.getRenderableEntities(tardisId)) {
            Entity entity = ghost.entity();
            LerpedPose pose = ghost.pose();
            entity.setYaw(pose.yaw());
            entity.setPitch(pose.pitch());
            if (entity instanceof LivingEntity living) {
                snapLivingYaw(living, pose.yaw());
            }
            entityDispatcher.render(
                    entity,
                    pose.x(),
                    pose.y(),
                    pose.z(),
                    tickDelta,
                    matrices,
                    vertexConsumers,
                    light
            );
        }
    }

    private static void snapLivingYaw(LivingEntity living, float yaw) {
        living.setBodyYaw(yaw);
        living.setHeadYaw(yaw);
        living.prevBodyYaw = yaw;
        living.prevHeadYaw = yaw;
    }

    private static void requestIfNeeded(UUID tardisId) {
        long now = System.currentTimeMillis();
        Long last = LAST_REQUEST_MS.get(tardisId);
        if (last != null && now - last < REQUEST_COOLDOWN_MS) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) {
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
