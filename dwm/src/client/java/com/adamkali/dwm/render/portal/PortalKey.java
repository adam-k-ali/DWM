package com.adamkali.dwm.render.portal;

import java.util.UUID;

/**
 * Identity for a scheduled / rendered door portal (kind + TARDIS id).
 * One shared full-window FBO: last END_MAIN writer wins when multiple keys share a frame.
 */
public record PortalKey(PortalKind kind, UUID tardisId) {
    public PortalKey {
        if (kind == null) {
            throw new IllegalArgumentException("kind");
        }
        if (tardisId == null) {
            throw new IllegalArgumentException("tardisId");
        }
    }

    public static PortalKey boti(UUID tardisId) {
        return new PortalKey(PortalKind.BOTI, tardisId);
    }

    public static PortalKey soto(UUID tardisId) {
        return new PortalKey(PortalKind.SOTO, tardisId);
    }
}
