package com.adamkali.dwm.platform;

/**
 * Holds the loader-installed {@link DwmClientPlatform}. Client entrypoints must call
 * {@link #set(DwmClientPlatform)} before any client common initialization.
 */
public final class DwmClientServices {
    private static DwmClientPlatform platform;

    private DwmClientServices() {
    }

    public static void set(DwmClientPlatform instance) {
        if (instance == null) {
            throw new IllegalArgumentException("DwmClientPlatform must not be null");
        }
        platform = instance;
    }

    public static DwmClientPlatform get() {
        if (platform == null) {
            throw new IllegalStateException("DwmClientPlatform has not been installed by the client entrypoint");
        }
        return platform;
    }
}
