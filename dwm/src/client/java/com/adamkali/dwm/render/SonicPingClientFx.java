package com.adamkali.dwm.render;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import com.adamkali.dwm.item.SonicPingLogic;
import com.adamkali.dwm.sound.DWMSounds;

/**
 * Owner-only cloaked-shell silhouette + particles after a successful Ping.
 */
public final class SonicPingClientFx {
    private static @Nullable UUID activeTardisId;
    private static @Nullable BlockPos activePos;
    private static long expiresAtGameTime;

    private SonicPingClientFx() {
    }

    public static void begin(UUID tardisId, BlockPos pos, long gameTime) {
        activeTardisId = tardisId;
        activePos = pos;
        expiresAtGameTime = gameTime + SonicPingLogic.REVEAL_TICKS;
    }

    public static void clear() {
        activeTardisId = null;
        activePos = null;
        expiresAtGameTime = 0L;
    }

    public static boolean isActive(@Nullable UUID tardisId) {
        return tardisId != null && tardisId.equals(activeTardisId);
    }

    public static void clientTick(@Nullable Level level) {
        if (activeTardisId == null || level == null) {
            return;
        }
        if (level.getGameTime() >= expiresAtGameTime) {
            clear();
            return;
        }
        if (activePos == null) {
            return;
        }
        spawnParticles(level, activePos);
    }

    public static void playCue(Level level, BlockPos pos) {
        level.playLocalSound(
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                DWMSounds.SONIC_SCREWDRIVER,
                SoundSource.PLAYERS,
                1.0F,
                1.0F,
                false
        );
        spawnParticles(level, pos);
    }

    private static void spawnParticles(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;
        double cz = pos.getZ() + 0.5;
        for (int i = 0; i < 8; i++) {
            double ox = (random.nextDouble() - 0.5) * 1.4;
            double oy = random.nextDouble() * 2.0;
            double oz = (random.nextDouble() - 0.5) * 1.4;
            level.addParticle(ParticleTypes.END_ROD, cx + ox, cy + oy, cz + oz, 0.0, 0.02, 0.0);
            level.addParticle(ParticleTypes.GLOW, cx + ox * 0.6, cy + oy * 0.5, cz + oz * 0.6, 0.0, 0.01, 0.0);
        }
    }
}
