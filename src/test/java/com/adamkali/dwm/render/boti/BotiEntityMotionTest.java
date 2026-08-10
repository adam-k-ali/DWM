package com.adamkali.dwm.render.boti;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.render.boti.BotiEntityMotion.EntityInterpState;
import com.adamkali.dwm.render.boti.BotiEntityMotion.LerpedPose;
import com.adamkali.dwm.tardis.boti.BotiEntitySample;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BotiEntityMotionTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void blendFactor_IsMidpointAtHalfInterval() {
        long receive = 1000L;
        float t = BotiEntityMotion.blendFactor(receive, receive + BotiEntityMotion.EXPECTED_INTERVAL_MS / 2);
        assertEquals(0.5f, t, 1.0e-4f);
    }

    @Test
    void blendFactor_ClampsAboveOne() {
        long receive = 1000L;
        float t = BotiEntityMotion.blendFactor(receive, receive + BotiEntityMotion.EXPECTED_INTERVAL_MS * 3);
        assertEquals(1.0f, t, 0.0f);
    }

    @Test
    void blendFactor_ClampsBelowZero() {
        float t = BotiEntityMotion.blendFactor(1000L, 900L);
        assertEquals(0.0f, t, 0.0f);
    }

    @Test
    void blendFactor_customIntervalUsesProvidedMs() {
        long receive = 1000L;
        float half = BotiEntityMotion.blendFactor(receive, receive + 50L, 100L);
        assertEquals(0.5f, half, 1.0e-4f);
        LerpedPose pose = BotiEntityMotion.lerpPose(
                new EntityInterpState(0, 0, 0, 0, 0, 10, 0, 0, 0, 0, receive),
                receive + 50L,
                100L
        );
        assertEquals(5.0, pose.x(), 1.0e-4);
    }

    @Test
    void lerpPose_MidpointIsHalfway() {
        EntityInterpState state = new EntityInterpState(
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                10.0f, 4.0f, 2.0f, 90.0f, 20.0f,
                0L
        );
        LerpedPose pose = BotiEntityMotion.lerpPose(state, BotiEntityMotion.EXPECTED_INTERVAL_MS / 2);
        assertEquals(5.0, pose.x(), 1.0e-4);
        assertEquals(2.0, pose.y(), 1.0e-4);
        assertEquals(1.0, pose.z(), 1.0e-4);
        assertEquals(45.0f, pose.yaw(), 1.0e-3f);
        assertEquals(10.0f, pose.pitch(), 1.0e-3f);
    }

    @Test
    void lerpAngleDegrees_TakesShortestPathAcrossWrap() {
        // Shortest path from 170 → -170 is +20°; midpoint is ±180.
        float mid = BotiEntityMotion.lerpAngleDegrees(170.0f, -170.0f, 0.5f);
        assertEquals(180.0f, Math.abs(mid), 1.0e-2f);
    }

    @Test
    void limbSpeed_ScalesWithHorizontalDistance() {
        assertEquals(0.0f, BotiEntityMotion.limbSpeed(0.0f, 0.0f, 0.0f, 0.0f), 0.0f);
        assertEquals(1.0f, BotiEntityMotion.limbSpeed(0.0f, 0.0f, 1.0f, 0.0f), 1.0e-4f);
        float slow = BotiEntityMotion.limbSpeed(0.0f, 0.0f, 0.1f, 0.0f);
        assertEquals(0.4f, slow, 1.0e-4f);
    }

    @Test
    void advanceTo_MovesPreviousTargetToFrom() {
        BotiEntitySample first = new BotiEntitySample(1.0f, 2.0f, 3.0f, 10.0f, 0.0f, new CompoundTag());
        BotiEntitySample second = new BotiEntitySample(4.0f, 5.0f, 6.0f, 40.0f, 5.0f, new CompoundTag());
        EntityInterpState identity = EntityInterpState.identity(first, 100L);
        EntityInterpState advanced = identity.advanceTo(second, 150L);
        assertEquals(1.0f, advanced.fromX(), 0.0f);
        assertEquals(2.0f, advanced.fromY(), 0.0f);
        assertEquals(3.0f, advanced.fromZ(), 0.0f);
        assertEquals(10.0f, advanced.fromYaw(), 0.0f);
        assertEquals(4.0f, advanced.toX(), 0.0f);
        assertEquals(5.0f, advanced.toY(), 0.0f);
        assertEquals(6.0f, advanced.toZ(), 0.0f);
        assertEquals(40.0f, advanced.toYaw(), 0.0f);
        assertEquals(150L, advanced.receiveTimeMs());
    }
}
