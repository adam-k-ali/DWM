package com.adamkali.dwm.analytics;

import com.adamkali.dwm.DWMReference;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.mojang.logging.LogUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<JSONObject> EVENTS = new ArrayList<>();

    private static MessageBuilder messageBuilder;

    private static final String ANALYTICS_USER = "user";

    public static final String EVENT_LAUNCH = "launch";
    public static final String EVENT_SONIC_SCREWDRIVER_USE = "sonic_screwdriver_use";


    private static void init() {
        messageBuilder = new MessageBuilder("af25c1c6ce93b2f71848ac3d6133f074");
    }

    public static void trackEvent(String event, Object... properties) {
        if (!DWMReference.IS_ANALYTICS_ENABLED) {
            return;
        }
        if (messageBuilder == null) {
            init();
        }

        JSONObject jsonProperties = new JSONObject();
        for (int i = 0; i < properties.length; i += 2) {
            jsonProperties.put(properties[i].toString(), properties[i + 1]);
        }

        JSONObject someEvent = messageBuilder.event(ANALYTICS_USER, event, jsonProperties);
        EVENTS.add(someEvent);
    }

    public static void deliver() {
        if (!DWMReference.IS_ANALYTICS_ENABLED) {
            return;
        }
        if (messageBuilder == null) {
            return;
        }

        ClientDelivery delivery = new ClientDelivery();
        for (JSONObject event : EVENTS) {
            delivery.addMessage(event);
        }

        MixpanelAPI mixpanel = new MixpanelAPI();
        try {
            mixpanel.deliver(delivery);
            EVENTS.clear();
        } catch (IOException e) {
            LOGGER.error("Failed to deliver analytics events", e);
        }
    }

}
