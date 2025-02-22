package com.adamkali.dwm;

import com.adamkali.dwm.analytics.AnalyticsManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class DWMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            AnalyticsManager.deliver();
        });


    }


}