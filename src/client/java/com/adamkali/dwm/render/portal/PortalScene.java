package com.adamkali.dwm.render.portal;

import java.util.Objects;

/**
 * One scheduled portal draw: key + tick delta + pluggable content.
 * Hitch / clear color are resolved by {@link PortalContent} at END_MAIN.
 */
public final class PortalScene {
    private final PortalKey key;
    private final float tickDelta;
    private final PortalContent content;

    public PortalScene(PortalKey key, float tickDelta, PortalContent content) {
        this.key = Objects.requireNonNull(key, "key");
        this.tickDelta = tickDelta;
        this.content = Objects.requireNonNull(content, "content");
    }

    public PortalKey key() {
        return key;
    }

    public float tickDelta() {
        return tickDelta;
    }

    public PortalContent content() {
        return content;
    }
}
