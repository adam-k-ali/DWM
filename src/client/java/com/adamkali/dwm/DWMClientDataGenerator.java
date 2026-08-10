package com.adamkali.dwm;

import com.adamkali.dwm.datagen.DWMBlockTagProvider;
import com.adamkali.dwm.datagen.DWMItemTagProvider;
import com.adamkali.dwm.datagen.DWMLanguageProvider;
import com.adamkali.dwm.datagen.DWMLootTableProvider;
import com.adamkali.dwm.datagen.DWMModelProvider;
import com.adamkali.dwm.datagen.DWMRecipeProvider;
import com.adamkali.dwm.datagen.DWMWorldgenProvider;
import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.world.DWMBiomeBootstrap;
import com.adamkali.dwm.world.DWMChunkGeneratorSettingsBootstrap;
import com.adamkali.dwm.world.DWMConfiguredFeatureBootstrap;
import com.adamkali.dwm.world.DWMPlacedFeatureBootstrap;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Single datagen entrypoint (client source set) so model + data providers share one cache
 * and do not delete each other's outputs.
 */
public class DWMClientDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(AdvancementsProvider::new);
        pack.addProvider(DWMRecipeProvider::new);
        pack.addProvider(DWMLanguageProvider::new);
        DWMBlockTagProvider blockTagProvider = pack.addProvider(DWMBlockTagProvider::new);
        pack.addProvider((output, registries) -> new DWMItemTagProvider(output, registries, blockTagProvider));
        pack.addProvider(DWMModelProvider::new);
        pack.addProvider(DWMLootTableProvider::new);
        pack.addProvider(DWMWorldgenProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.CONFIGURED_FEATURE, DWMConfiguredFeatureBootstrap::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, DWMPlacedFeatureBootstrap::bootstrap);
        registryBuilder.add(Registries.BIOME, DWMBiomeBootstrap::bootstrap);
        registryBuilder.add(Registries.NOISE_SETTINGS, DWMChunkGeneratorSettingsBootstrap::bootstrap);
    }

    static class AdvancementsProvider extends FabricAdvancementProvider {
        protected AdvancementsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(output, registryLookup);
        }

        @Override
        public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
            HolderGetter<Item> registryEntryLookup = registries.lookupOrThrow(Registries.ITEM);
            Advancement.Builder.advancement()
                    .display(
                            DWMItems.SONIC_THIRD_DOCTOR,
                            Component.translatable("advancements.dwm.sonic_screwdriver"),
                            Component.translatable("advancements.dwm.sonic_screwdriver.description"),
                            Identifier.parse("textures/gui/advancements/backgrounds/adventure.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("sonic_screwdriver", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(registryEntryLookup, DWMItemTags.SONIC_SCREWDRIVERS)))
                    .save(consumer, DWMReference.MOD_ID + "/sonic_screwdriver");
        }
    }
}
