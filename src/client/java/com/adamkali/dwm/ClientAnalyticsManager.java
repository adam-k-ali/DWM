package com.adamkali.dwm;

import com.adamkali.dwm.analytics.AnalyticsManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientAnalyticsManager {
    private static final int DELIVERY_INTERVAL_MS = 10000;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private static ScheduledExecutorService scheduler;

    public static void initialize() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        // Non-daemon pools keep the JVM alive after Minecraft exits and trip ClientShutdownWatchdog.
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "dwm-analytics");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                AnalyticsManager::deliver,
                DELIVERY_INTERVAL_MS,
                DELIVERY_INTERVAL_MS,
                TimeUnit.MILLISECONDS);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> shutdown());
    }

    private static void shutdown() {
        AnalyticsManager.deliver();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}
