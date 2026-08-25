package com.adamkali.dwm;

import com.adamkali.dwm.datagen.DWMAdvancementProvider;
import com.adamkali.dwm.datagen.DWMBlockTagProvider;
import com.adamkali.dwm.datagen.DWMItemTagProvider;
import com.adamkali.dwm.datagen.DWMEntityLootTableProvider;
import com.adamkali.dwm.datagen.DWMEntityTagProvider;
import com.adamkali.dwm.datagen.DWMLanguageProvider;
import com.adamkali.dwm.datagen.DWMLootTableProvider;
import com.adamkali.dwm.datagen.DWMModelProvider;
import com.adamkali.dwm.datagen.DWMRecipeProvider;
import com.adamkali.dwm.datagen.DWMWorldgenProvider;
import com.adamkali.dwm.world.DWMBiomeBootstrap;
import com.adamkali.dwm.world.DWMChunkGeneratorSettingsBootstrap;
import com.adamkali.dwm.world.DWMConfiguredFeatureBootstrap;
import com.adamkali.dwm.world.DWMPlacedFeatureBootstrap;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

/**
 * Single datagen entrypoint (client source set) so model + data providers share one cache
 * and do not delete each other's outputs.
 */
public class DWMClientDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(DWMAdvancementProvider::new);
        pack.addProvider(DWMRecipeProvider::new);
        pack.addProvider(DWMLanguageProvider::new);
        DWMBlockTagProvider blockTagProvider = pack.addProvider(DWMBlockTagProvider::new);
        pack.addProvider((output, registries) -> new DWMItemTagProvider(output, registries, blockTagProvider));
        pack.addProvider(DWMModelProvider::new);
        pack.addProvider(DWMLootTableProvider::new);
        pack.addProvider(DWMEntityLootTableProvider::new);
        pack.addProvider(DWMEntityTagProvider::new);
        pack.addProvider(DWMWorldgenProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.CONFIGURED_FEATURE, DWMConfiguredFeatureBootstrap::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, DWMPlacedFeatureBootstrap::bootstrap);
        registryBuilder.add(Registries.BIOME, DWMBiomeBootstrap::bootstrap);
        registryBuilder.add(Registries.NOISE_SETTINGS, DWMChunkGeneratorSettingsBootstrap::bootstrap);
    }
}
