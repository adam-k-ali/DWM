package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.portal.PortalAtmosphere;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Temporarily lights the portal pass as the looked-into world: replaces the viewer's lightmap
 * inputs with the synced {@link PortalAtmosphere} ones and switches the level diffuse
 * {@link Lighting} (ghost entities / block entities) to that world's cardinal light type.
 * Packed sky/block coords are sampled from the other dimension; without this overlay they are
 * interpreted against the viewer's dimension.
 */
public final class PortalLightmap implements AutoCloseable {
    private final Lightmap lightmap;
    private final LightmapRenderState live;
    private final Snapshot previous;
    private final Lighting lighting;
    private final CardinalLighting.Type previousCardinal;
    private boolean closed;

    private PortalLightmap(
            Lightmap lightmap,
            LightmapRenderState live,
            Snapshot previous,
            Lighting lighting,
            CardinalLighting.Type previousCardinal
    ) {
        this.lightmap = lightmap;
        this.live = live;
        this.previous = previous;
        this.lighting = lighting;
        this.previousCardinal = previousCardinal;
    }

    /** @return {@code null} (nothing to restore) when there is no renderer or atmosphere */
    public static PortalLightmap apply(Minecraft client, PortalAtmosphere atmosphere) {
        if (client == null || client.gameRenderer == null || atmosphere == null) {
            return null;
        }
        GameRenderer renderer = client.gameRenderer;
        LightmapRenderState live = renderer.gameRenderState.lightmapRenderState;
        Snapshot previous = Snapshot.of(live);
        overlay(live, atmosphere);
        renderer.lightmap.render(live);

        Lighting lighting = renderer.lighting();
        CardinalLighting.Type viewerCardinal = viewerCardinal(client);
        if (lighting != null && atmosphere.cardinalLighting() != viewerCardinal) {
            lighting.updateLevel(atmosphere.cardinalLighting());
        } else {
            lighting = null;
        }
        return new PortalLightmap(renderer.lightmap, live, previous, lighting, viewerCardinal);
    }

    static void overlay(LightmapRenderState state, PortalAtmosphere atmosphere) {
        state.skyFactor = atmosphere.skyLightFactor();
        state.skyLightColor = ARGB.vector3fFromRGB24(atmosphere.skyLightColor());
        state.ambientColor = ARGB.vector3fFromRGB24(atmosphere.ambientLightColor());
        state.blockLightTint = ARGB.vector3fFromRGB24(atmosphere.blockLightTint());
        state.needsUpdate = true;
    }

    private static CardinalLighting.Type viewerCardinal(Minecraft client) {
        return client.level == null
                ? CardinalLighting.Type.DEFAULT
                : client.level.dimensionType().cardinalLightType();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        previous.restore(live);
        live.needsUpdate = true;
        lightmap.render(live);
        if (lighting != null) {
            lighting.updateLevel(previousCardinal);
        }
    }

    private record Snapshot(
            float skyFactor,
            Vector3fc skyLightColor,
            Vector3fc ambientColor,
            Vector3fc blockLightTint
    ) {
        static Snapshot of(LightmapRenderState state) {
            return new Snapshot(
                    state.skyFactor,
                    new Vector3f(state.skyLightColor),
                    new Vector3f(state.ambientColor),
                    new Vector3f(state.blockLightTint)
            );
        }

        void restore(LightmapRenderState state) {
            state.skyFactor = skyFactor;
            state.skyLightColor = skyLightColor;
            state.ambientColor = ambientColor;
            state.blockLightTint = blockLightTint;
        }
    }
}
