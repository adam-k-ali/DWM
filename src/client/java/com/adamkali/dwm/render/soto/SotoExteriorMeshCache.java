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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-TARDIS SOTO exterior cache for shell metadata and atmosphere (Phase 0).
 * Terrain and entities come from {@link SotoGhostExterior}.
 * <p>
 * Ghost entity / block-entity draws use extract+submit against {@link SubmitNodeCollector}
 * and {@link CameraRenderState}, matching {@link com.adamkali.dwm.render.boti.BotiInteriorMeshCache}.
 */
public final class SotoExteriorMeshCache {
    private static final long REQUEST_COOLDOWN_MS = 2000L;
    private static final int FULLBRIGHT = LightCoordsUtil.FULL_BRIGHT;
    /** Fallback ids for ghosts created before spawn-time setId (or if id was never assigned). */
    private static final AtomicInteger NEXT_FALLBACK_ENTITY_ID = new AtomicInteger(2_000_000);

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

    /**
     * Submits ghost block entities into {@code submitNodeCollector}. Caller must flush
     * (e.g. {@code FeatureRenderDispatcher#renderAllFeatures}) while the portal target is bound.
     *
     * @return number of BEs successfully submitted
     */
    public static int renderGhostBlockEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            int light,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        Minecraft client = Minecraft.getInstance();
        SotoGhostExterior ghost = SotoGhostExterior.get(tardisId);
        if (client == null || ghost == null || matrices == null || submitNodeCollector == null || cameraState == null) {
            return 0;
        }
        BlockEntityRenderDispatcher beDispatcher = client.getBlockEntityRenderDispatcher();
        if (camera != null) {
            beDispatcher.prepare(camera.position());
        }
        List<BlockEntity> bes = ghost.buildRenderedBlockEntities();
        int submitted = 0;
        int lightCoords = light >= 0 ? light : FULLBRIGHT;
        Vec3 cameraPos = cameraState.pos;
        for (BlockEntity blockEntity : bes) {
            @SuppressWarnings("unchecked")
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer =
                    (BlockEntityRenderer<BlockEntity, BlockEntityRenderState>) beDispatcher.getRenderer(blockEntity);
            if (renderer == null) {
                continue;
            }
            BlockEntityRenderState state = beDispatcher.tryExtractRenderState(blockEntity, tickDelta, null, false);
            if (state == null) {
                continue;
            }
            state.lightCoords = lightCoords;
            BlockPos pos = blockEntity.getBlockPos();
            matrices.pushPose();
            matrices.translate(
                    pos.getX() - cameraPos.x,
                    pos.getY() - cameraPos.y,
                    pos.getZ() - cameraPos.z
            );
            beDispatcher.submit(state, matrices, submitNodeCollector, cameraState);
            matrices.popPose();
            submitted++;
        }
        return submitted;
    }

    /**
     * Submits ghost entities into {@code submitNodeCollector}. Caller must flush while the
     * portal target is bound.
     *
     * @return number of entities successfully submitted
     */
    public static int renderGhostEntities(
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraState,
            float tickDelta,
            UUID tardisId,
            Camera camera
    ) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || matrices == null || submitNodeCollector == null || cameraState == null
                || !SotoGhostExterior.hasEntities(tardisId)) {
            return 0;
        }
        EntityRenderDispatcher entityDispatcher = client.getEntityRenderDispatcher();
        if (camera != null) {
            entityDispatcher.prepare(camera, client.player);
        }
        List<SotoGhostExterior.RenderableGhostEntity> ghosts =
                SotoGhostExterior.getRenderableEntities(tardisId);
        int submitted = 0;
        Vec3 cameraPos = cameraState.pos;
        for (SotoGhostExterior.RenderableGhostEntity ghost : ghosts) {
            Entity entity = ghost.entity();
            LerpedPose pose = ghost.pose();
            entity.setYRot(pose.yaw());
            entity.setXRot(pose.pitch());
            if (entity instanceof LivingEntity living) {
                snapLivingYaw(living, pose.yaw());
            }
            try {
                // Client-created ghosts may still lack an id if spawned before setId was wired.
                ensureGhostEntityId(entity);
                EntityRenderState entityState = entityDispatcher.extractEntity(entity, tickDelta);
                if (entityState == null) {
                    continue;
                }
                // Ghost entities sit at footprint-relative coords in the interior ClientLevel;
                // packed light from that sample is usually 0 → solid black models.
                entityState.lightCoords = FULLBRIGHT;
                entityDispatcher.submit(
                        entityState,
                        cameraState,
                        pose.x() - cameraPos.x,
                        pose.y() - cameraPos.y,
                        pose.z() - cameraPos.z,
                        matrices,
                        submitNodeCollector
                );
                submitted++;
            } catch (RuntimeException ignored) {
                // Skip one bad ghost rather than aborting the portal feature flush.
            }
        }
        return submitted;
    }

    private static void ensureGhostEntityId(Entity entity) {
        try {
            entity.getId();
        } catch (IllegalStateException missingId) {
            entity.setId(NEXT_FALLBACK_ENTITY_ID.getAndIncrement());
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
