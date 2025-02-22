package com.adamkali.dwm;

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
            builder.setSavingRunnable(() -> {

            });
            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.dwm.category.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.dwm.option.analytics"), false).setDefaultValue(false).build());
            general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.dwm.option.analytics.anonymous"), false).setDefaultValue(false).build());
            return builder.build();
        };
    }
}
