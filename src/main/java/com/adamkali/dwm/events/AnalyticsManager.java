package com.adamkali.dwm.events;

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

    public static final String EVENT_SIGNUP = "signup";


    private static void init() {
        messageBuilder = new MessageBuilder("af25c1c6ce93b2f71848ac3d6133f074");
    }

    public static void trackEvent(String event) {
        if (messageBuilder == null) {
            init();
        }

        JSONObject someEvent = messageBuilder.event(ANALYTICS_USER, event, null);
        EVENTS.add(someEvent);
    }

    public static void deliver() {
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
