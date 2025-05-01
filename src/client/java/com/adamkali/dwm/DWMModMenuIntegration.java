package com.adamkali.dwm;

import com.adamkali.dwm.config.DWMConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class DWMModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.dwm.title"));
            builder.setSavingRunnable(DWMConfig::save);

            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("dwm.config.category.experimental"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("dwm.config.option.chameleon_gui"), DWMConfig.getBoolean(DWMConfig.ENABLE_CHAMELEON_GUI)).setDefaultValue(DWMConfig.ENABLE_CHAMELEON_GUI.getDefaultValue()).setSaveConsumer((newValue) -> DWMConfig.setBoolean(DWMConfig.ENABLE_CHAMELEON_GUI, newValue)).build());
//            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.dwm.option.analytics.anonymous"), false).setDefaultValue(false).build());
            return builder.alwaysShowTabs().build();
        };
    }
}