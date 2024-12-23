package com.adamkali.dwm;

import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.sound.DWMSounds;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class DWMMain implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Doctor Who Mod");
        DWMItems.initialize();
        DWMSounds.initialize();
        LOGGER.info("Doctor Who Mod initialized");
    }
}
