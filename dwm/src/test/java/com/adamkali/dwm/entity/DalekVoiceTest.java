package com.adamkali.dwm.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DalekVoiceTest {
    @Test
    void freshLockAfterCooldownShouts() {
        assertTrue(DalekVoice.shouldExterminate(false, true, DalekVoice.EXTERMINATE_COOLDOWN_TICKS));
    }

    @Test
    void freshLockWithinCooldownIsSilent() {
        assertFalse(DalekVoice.shouldExterminate(false, true, DalekVoice.EXTERMINATE_COOLDOWN_TICKS - 1));
    }

    @Test
    void retargetingWhileAlreadyLockedIsSilent() {
        assertFalse(DalekVoice.shouldExterminate(true, true, Integer.MAX_VALUE));
    }

    @Test
    void losingTheTargetIsSilent() {
        assertFalse(DalekVoice.shouldExterminate(true, false, Integer.MAX_VALUE));
        assertFalse(DalekVoice.shouldExterminate(false, false, Integer.MAX_VALUE));
    }
}
