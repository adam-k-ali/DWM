package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.soto.SotoAtmosphere;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors;
import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Temporarily replaces the viewer's lightmap with one matching the looked-into portal atmosphere.
 * Packed sky/block coords are sampled from the other dimension; without this overlay they are
 * interpreted against {@code dwm:tardis} ({@code sky_light_factor=0}, black ambient).
 */
public final class PortalLightmap implements AutoCloseable {
    private final Lightmap lightmap;
    private final LightmapRenderState live;
    private final Snapshot previous;
    private boolean closed;

    private PortalLightmap(Lightmap lightmap, LightmapRenderState live, Snapshot previous) {
        this.lightmap = lightmap;
        this.live = live;
        this.previous = previous;
    }

    public static PortalLightmap apply(Minecraft client, SotoAtmosphere atmosphere) {
        if (client == null || client.gameRenderer == null || atmosphere == null) {
            return null;
        }
        GameRenderer renderer = client.gameRenderer;
        LightmapRenderState live = renderer.gameRenderState.lightmapRenderState;
        Snapshot previous = Snapshot.of(live);
        overlay(live, atmosphere);
        renderer.lightmap.render(live);
        return new PortalLightmap(renderer.lightmap, live, previous);
    }

    static void overlay(LightmapRenderState state, SotoAtmosphere atmosphere) {
        EffectsKind kind = SotoAtmosphereColors.effectsKind(atmosphere.dimensionEffectsId());
        float skyAngle = SotoAtmosphereColors.skyAngle(atmosphere.timeOfDay());
        state.skyFactor = SotoAtmosphereColors.skyLightFactor(
                kind, skyAngle, atmosphere.rainGradient(), atmosphere.thunderGradient());
        state.skyLightColor = ARGB.vector3fFromRGB24(SotoAtmosphereColors.skyLightColor(kind));
        state.ambientColor = ARGB.vector3fFromRGB24(SotoAtmosphereColors.ambientLightColor(kind));
        state.needsUpdate = true;
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
    }

    private record Snapshot(float skyFactor, Vector3fc skyLightColor, Vector3fc ambientColor) {
        static Snapshot of(LightmapRenderState state) {
            return new Snapshot(
                    state.skyFactor,
                    new Vector3f(state.skyLightColor),
                    new Vector3f(state.ambientColor)
            );
        }

        void restore(LightmapRenderState state) {
            state.skyFactor = skyFactor;
            state.skyLightColor = skyLightColor;
            state.ambientColor = ambientColor;
        }
    }
}
