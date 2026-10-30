package com.adamkali.dwm.entity;

import com.adamkali.dwm.MinecraftTestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DalekJobFxTest {
    private static final float EPSILON = 0.0001F;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void watchingJobsScanOnlyWhileOnDuty() {
        for (DalekJob job : new DalekJob[] {DalekJob.SENTRY, DalekJob.LOOKOUT, DalekJob.PATROL}) {
            assertEquals(1.0F, DalekJobFx.scanTarget(job, true), EPSILON, job.name());
            assertEquals(0.0F, DalekJobFx.scanTarget(job, false), EPSILON, job.name());
            assertEquals(0.0F, DalekJobFx.workTarget(job, true), EPSILON, job.name());
        }
    }

    @Test
    void technicianWorksOnlyWhileOnDuty() {
        assertEquals(1.0F, DalekJobFx.workTarget(DalekJob.TECHNICIAN, true), EPSILON);
        assertEquals(0.0F, DalekJobFx.workTarget(DalekJob.TECHNICIAN, false), EPSILON);
        assertEquals(0.0F, DalekJobFx.scanTarget(DalekJob.TECHNICIAN, true), EPSILON);
    }

    @Test
    void wildDalekNeverAnimatesAJob() {
        assertEquals(0.0F, DalekJobFx.scanTarget(DalekJob.NONE, true), EPSILON);
        assertEquals(0.0F, DalekJobFx.workTarget(DalekJob.NONE, true), EPSILON);
    }

    @Test
    void easeStepsTowardTargetWithoutOvershooting() {
        assertEquals(DalekJobFx.BLEND_PER_TICK, DalekJobFx.ease(0.0F, 1.0F), EPSILON);
        assertEquals(1.0F, DalekJobFx.ease(0.95F, 1.0F), EPSILON);
        assertEquals(0.9F, DalekJobFx.ease(1.0F, 0.0F), EPSILON);
        assertEquals(0.0F, DalekJobFx.ease(0.05F, 0.0F), EPSILON);
    }

    @Test
    void fullBlendTakesAboutTenTicks() {
        float blend = 0.0F;
        int ticks = 0;
        while (blend < 1.0F) {
            blend = DalekJobFx.ease(blend, 1.0F);
            ticks++;
        }
        assertEquals(10, ticks);
    }
}
