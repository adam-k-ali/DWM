package com.adamkali.dwm;

import com.adamkali.dwm.analytics.AnalyticsManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientAnalyticsManager {
    private static final int DELIVERY_INTERVAL_MS = 10000;

    public static void initialize() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(AnalyticsManager::deliver, DELIVERY_INTERVAL_MS, DELIVERY_INTERVAL_MS, TimeUnit.MILLISECONDS);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            AnalyticsManager.deliver();
        });

    }
}
