package com.adamkali.dwm;

import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.DWMItems;
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
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DWMDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(AdvancementsProvider::new);
    }

    static class AdvancementsProvider extends FabricAdvancementProvider {
        protected AdvancementsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(output, registryLookup);
        }

        @Override
        public void generateAdvancement(RegistryWrapper.WrapperLookup registries, Consumer<AdvancementEntry> consumer) {
            RegistryEntryLookup<Item> registryEntryLookup = registries.getOrThrow(RegistryKeys.ITEM);
            AdvancementEntry rootAdvancement = Advancement.Builder.create()
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
                    // The first string used in criterion is the name referenced by other advancements when they want to have 'requirements'
                    .criterion("sonic_screwdriver", InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(registryEntryLookup, DWMItemTags.SONIC_SCREWDRIVERS)))
                    .build(consumer, DWMReference.MOD_ID + "/sonic_screwdriver");

        }
    }

//    static InventoryChangedListCriterion extends AbstractCriterion<InventoryChangedCriterion.Conditions> {
//        public InventoryChangedListCriterion(Identifier id) {
//            super(id);
//        }
//
//        public InventoryChangedListCriterion.Conditions conditions() {
//            return new InventoryChangedListCriterion.Conditions(this);
//        }
//
//        public static class Conditions extends AbstractCriterion.Conditions {
//            public Conditions(InventoryChangedListCriterion criterion) {
//                super(criterion);
//            }
//
//            public static InventoryChangedListCriterion.Conditions items(ItemConvertible... items) {
//                return new InventoryChangedListCriterion.Conditions(criterion, Arrays.stream(items).map(ItemConvertible::asItem).collect(Collectors.toList()));
//            }
//        }
//    }


}
