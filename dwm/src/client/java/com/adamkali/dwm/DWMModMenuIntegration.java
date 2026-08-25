package com.adamkali.dwm;

import com.adamkali.dwm.config.DWMConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class DWMModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("config.dwm.title"));
            builder.setSavingRunnable(DWMConfig::save);

            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("dwm.config.category.experimental"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("dwm.config.option.chameleon_gui"), DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).setDefaultValue(DWMConfig.ENABLE_CHAMELEON_GUI.getDefaultValue()).setSaveConsumer((newValue) -> DWMConfig.setBoolean(DWMConfig.ENABLE_CHAMELEON_GUI, newValue)).build());
            general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("dwm.config.option.enable_door_portals"), DWMConfig.getBoolean(DWMConfig.ENABLE_DOOR_PORTALS)).setDefaultValue(DWMConfig.ENABLE_DOOR_PORTALS.getDefaultValue()).setTooltip(Component.translatable("dwm.config.option.enable_door_portals.tooltip")).setSaveConsumer((newValue) -> DWMConfig.setBoolean(DWMConfig.ENABLE_DOOR_PORTALS, newValue)).build());
            general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("dwm.config.option.show_portal_perf_debug"), DWMConfig.getBoolean(DWMConfig.SHOW_PORTAL_PERF_DEBUG)).setDefaultValue(DWMConfig.SHOW_PORTAL_PERF_DEBUG.getDefaultValue()).setTooltip(Component.translatable("dwm.config.option.show_portal_perf_debug.tooltip")).setSaveConsumer((newValue) -> DWMConfig.setBoolean(DWMConfig.SHOW_PORTAL_PERF_DEBUG, newValue)).build());
            return builder.alwaysShowTabs().build();
        };
    }
}