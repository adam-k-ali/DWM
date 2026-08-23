package com.adamkali.dwm.platform;

/**
 * Holds the loader-installed {@link DwmPlatform}. Entrypoints must call {@link #set(DwmPlatform)}
 * before any common initialization.
 */
public final class DwmServices {
    private static DwmPlatform platform;

    private DwmServices() {
    }

    public static void set(DwmPlatform instance) {
        if (instance == null) {
            throw new IllegalArgumentException("DwmPlatform must not be null");
        }
        platform = instance;
    }

    public static DwmPlatform get() {
        if (platform == null) {
            throw new IllegalStateException("DwmPlatform has not been installed by the loader entrypoint");
        }
        return platform;
    }
}
