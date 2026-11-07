package com.adamkali.dwm.tardis.soto;

import com.adamkali.dwm.tardis.soto.SotoAtmosphereColors.EffectsKind;
import net.minecraft.util.ARGB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Pure cloud parameters for the SOTO portal, derived from a synced {@link SotoAtmosphere}.
 * <p>
 * Mirrors the overworld {@code CLOUD_COLOR} / {@code CLOUD_HEIGHT} environment attributes: a
 * translucent white base, tinted dark over the night keyframes of the day timeline and blended
 * toward grey by rain and thunder.
 */
public record SotoCloudState(int color, float height) {
    /** Vanilla overworld cloud base height ({@code DimensionDefaults.OVERWORLD_CLOUD_HEIGHT}). */
    public static final float OVERWORLD_CLOUD_HEIGHT = 192.33F;

    private static final int BASE_COLOR = ARGB.white(0.8F);
    private static final int NIGHT_MULTIPLIER = ARGB.colorFromFloat(1.0F, 0.1F, 0.1F, 0.15F);
    private static final int DAY_TICKS = 24000;

    /** Ticks between portal re-renders while clouds are visible; drift is 0.03 blocks/tick, so 20 is ~0.6 blocks. */
    public static final int REFRESH_INTERVAL_TICKS = 20;

    /** Camera Y used only before the exterior's footprint origin is known. */
    public static final double FALLBACK_CAMERA_Y = 70.0;

    /**
     * Cloud camera in world space. The portal camera is footprint-local, so adding the synced footprint
     * origin gives the real exterior position (cloud field, drift origin and height all match the outside).
     * Without an origin only the local X/Z and {@link #FALLBACK_CAMERA_Y} are available.
     */
    public static Vec3 cloudCamera(BlockPos footprintOrigin, Vec3 portalLocalCamera) {
        if (footprintOrigin == null) {
            return new Vec3(portalLocalCamera.x, FALLBACK_CAMERA_Y, portalLocalCamera.z);
        }
        return new Vec3(
                footprintOrigin.getX() + portalLocalCamera.x,
                footprintOrigin.getY() + portalLocalCamera.y,
                footprintOrigin.getZ() + portalLocalCamera.z);
    }

    /** True when the cached portal frame is old enough that the clouds should be redrawn. */
    public static boolean refreshDue(long gameTime, long lastRefreshTick) {
        return lastRefreshTick == Long.MIN_VALUE
                || gameTime < lastRefreshTick
                || gameTime - lastRefreshTick >= REFRESH_INTERVAL_TICKS;
    }

    /** Only the overworld has clouds. */
    public boolean hasClouds() {
        return ARGB.alpha(color) > 0;
    }

    public static SotoCloudState from(SotoAtmosphere atmosphere) {
        SotoAtmosphere resolved = atmosphere == null ? SotoAtmosphere.DEFAULT : atmosphere;
        if (SotoAtmosphereColors.effectsKind(resolved.dimensionEffectsId()) != EffectsKind.OVERWORLD) {
            return new SotoCloudState(0, OVERWORLD_CLOUD_HEIGHT);
        }
        return new SotoCloudState(
                color(resolved.timeOfDay(), resolved.rainGradient(), resolved.thunderGradient()),
                OVERWORLD_CLOUD_HEIGHT);
    }

    static int color(long timeOfDay, float rainGradient, float thunderGradient) {
        int color = ARGB.multiply(BASE_COLOR, nightMultiplier(timeOfDay));
        if (rainGradient > 0.0F) {
            color = blendToGray(color, 0.24F, 0.5F * rainGradient);
        }
        if (thunderGradient > 0.0F) {
            color = blendToGray(color, 0.095F, 0.94F * thunderGradient);
        }
        return color;
    }

    /** Day timeline keyframes: white 133..11867, night 13670..22330, linear in between. */
    static int nightMultiplier(long timeOfDay) {
        float t = Math.floorMod(timeOfDay, (long) DAY_TICKS);
        if (t >= 13670.0F && t <= 22330.0F) {
            return NIGHT_MULTIPLIER;
        }
        if (t > 11867.0F && t < 13670.0F) {
            return ARGB.srgbLerp((t - 11867.0F) / (13670.0F - 11867.0F), -1, NIGHT_MULTIPLIER);
        }
        if (t > 22330.0F || t < 133.0F) {
            float since = t > 22330.0F ? t - 22330.0F : t + DAY_TICKS - 22330.0F;
            float span = DAY_TICKS + 133.0F - 22330.0F;
            return ARGB.srgbLerp(Mth.clamp(since / span, 0.0F, 1.0F), NIGHT_MULTIPLIER, -1);
        }
        return -1;
    }

    private static int blendToGray(int color, float brightness, float factor) {
        int gray = ARGB.scaleRGB(ARGB.greyscale(color), brightness);
        return ARGB.srgbLerp(factor, color, gray);
    }
}
