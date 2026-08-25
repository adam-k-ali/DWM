package com.adamkali.dwm.render.portal;

import com.adamkali.dwm.tardis.portal.PortalStreamKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalFrameCacheTest {
    @BeforeEach
    @AfterEach
    void reset() {
        PortalFrameCache.resetForTests();
    }

    @Test
    void unknownKey_isDirtyByDefault() {
        PortalKey key = PortalKey.soto(UUID.randomUUID());
        assertTrue(PortalFrameCache.isDirty(key));
        assertFalse(PortalFrameCache.wasLastWriter(key));
        assertNull(PortalFrameCache.lastWriter());
    }

    @Test
    void clearDirty_afterNoteRendered_allowsSkipEligibility() {
        PortalKey key = PortalKey.soto(UUID.randomUUID());
        PortalFrameCache.noteRendered(key);
        PortalFrameCache.clearDirty(key);

        assertFalse(PortalFrameCache.isDirty(key));
        assertTrue(PortalFrameCache.wasLastWriter(key));
    }

    @Test
    void otherKeyRender_dirtiesPreviousWriter() {
        UUID id = UUID.randomUUID();
        PortalKey soto = PortalKey.soto(id);
        PortalKey boti = PortalKey.boti(id);

        PortalFrameCache.noteRendered(soto);
        PortalFrameCache.clearDirty(soto);
        assertFalse(PortalFrameCache.isDirty(soto));

        PortalKey previous = PortalFrameCache.noteRendered(boti);
        PortalFrameCache.clearDirty(boti);

        assertEquals(soto, previous);
        assertTrue(PortalFrameCache.isDirty(soto));
        assertFalse(PortalFrameCache.isDirty(boti));
        assertTrue(PortalFrameCache.wasLastWriter(boti));
        assertFalse(PortalFrameCache.wasLastWriter(soto));
    }

    @Test
    void overwrittenReadyKeys_marksOthersDirty() {
        PortalKey soto = PortalKey.soto(UUID.randomUUID());
        PortalKey boti = PortalKey.boti(UUID.randomUUID());
        Set<PortalKey> ready = new HashSet<>();
        ready.add(soto);
        ready.add(boti);
        PortalFrameCache.clearDirty(soto);
        PortalFrameCache.clearDirty(boti);

        Set<PortalKey> overwritten = PortalFrameCache.overwrittenReadyKeys(soto, ready);

        assertTrue(overwritten.contains(boti));
        assertFalse(overwritten.contains(soto));
        assertTrue(PortalFrameCache.isDirty(boti));
        assertFalse(PortalFrameCache.isDirty(soto));
    }

    @Test
    void markDirty_streamKind_mapsToPortalKey() {
        UUID id = UUID.randomUUID();
        PortalFrameCache.noteRendered(PortalKey.soto(id));
        PortalFrameCache.clearDirty(PortalKey.soto(id));

        PortalFrameCache.markDirty(PortalStreamKind.SOTO, id);

        assertTrue(PortalFrameCache.isDirty(PortalKey.soto(id)));
        assertTrue(PortalFrameCache.isDirty(PortalFrameCache.toPortalKey(PortalStreamKind.SOTO, id)));
    }

    @Test
    void invalidateForResize_clearsCleanAndLastWriter() {
        PortalKey key = PortalKey.boti(UUID.randomUUID());
        PortalFrameCache.noteRendered(key);
        PortalFrameCache.clearDirty(key);

        PortalFrameCache.invalidateForResize();

        assertTrue(PortalFrameCache.isDirty(key));
        assertNull(PortalFrameCache.lastWriter());
        assertFalse(PortalFrameCache.wasLastWriter(key));
    }

    @Test
    void markAllDirty_resetsSkipState() {
        PortalKey a = PortalKey.soto(UUID.randomUUID());
        PortalKey b = PortalKey.boti(UUID.randomUUID());
        PortalFrameCache.noteRendered(a);
        PortalFrameCache.clearDirty(a);
        PortalFrameCache.clearDirty(b);

        PortalFrameCache.markAllDirty();

        assertTrue(PortalFrameCache.isDirty(a));
        assertTrue(PortalFrameCache.isDirty(b));
        assertNull(PortalFrameCache.lastWriter());
    }
}
