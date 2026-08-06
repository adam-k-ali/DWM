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
import com.adamkali.dwm.world.DWMConfiguredFeatureBootstrap;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Item;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, DWMConfiguredFeatureBootstrap::bootstrap);
    }

    static class AdvancementsProvider extends FabricAdvancementProvider {
        protected AdvancementsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(output, registryLookup);
        }

        @Override
        public void generateAdvancement(RegistryWrapper.WrapperLookup registries, Consumer<AdvancementEntry> consumer) {
            RegistryEntryLookup<Item> registryEntryLookup = registries.getOrThrow(RegistryKeys.ITEM);
            Advancement.Builder.create()
                    .display(
                            DWMItems.SONIC_THIRD_DOCTOR,
                            Text.translatable("advancements.dwm.sonic_screwdriver"),
                            Text.translatable("advancements.dwm.sonic_screwdriver.description"),
                            Identifier.of("textures/gui/advancements/backgrounds/adventure.png"),
                            AdvancementFrame.TASK,
                            true,
                            true,
                            false
                    )
                    .criterion("sonic_screwdriver", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(registryEntryLookup, DWMItemTags.SONIC_SCREWDRIVERS)))
                    .build(consumer, DWMReference.MOD_ID + "/sonic_screwdriver");
        }
    }
}
