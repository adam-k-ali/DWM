package com.adamkali.dwm;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class DWMModMenuIntegration implements ModMenuApi {
    private static void addBooleanEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String configKey, String translationKey, boolean defaultValue) {
        category.addEntry(entryBuilder
                .startBooleanToggle(Text.translatable(translationKey), DWMConfig.getBoolean(configKey))
                .setDefaultValue(defaultValue)
                .setSaveConsumer(newValue -> DWMConfig.setBoolean(configKey, newValue)).build());
    }

    private static void buildGeneralCategory(ConfigBuilder builder) {
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.dwm.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        DWMModMenuIntegration.addBooleanEntry(general,
                entryBuilder,
                DWMConfig.ALL_ANALYTICS_KEY,
                "config.dwm.option.analytics",
                false);
        DWMModMenuIntegration.addBooleanEntry(general,
                entryBuilder,
                DWMConfig.ANONYMOUS_ANALYTICS_KEY,
                "config.dwm.option.analytics.anonymous",
                false);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.dwm.title"));
            builder.setSavingRunnable(DWMConfig::save);
            buildGeneralCategory(builder);
            return builder.build();
        };
    }
}
