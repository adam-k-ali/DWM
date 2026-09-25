package com.adamkali.dwm.datagen;

import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import java.util.concurrent.CompletableFuture;

public class DWMEntityLootTableProvider extends FabricEntityLootSubProvider {
    public DWMEntityLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate() {
        add(
                DWMEntityTypes.BROAKIR,
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ContextIntProviders.exactly(1))
                                .add(LootItem.lootTableItem(Items.LEATHER)
                                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                this.enchantments,
                                                ContextFloatProviders.between(0.0F, 1.0F)
                                        )))
                )
        );
        add(DWMEntityTypes.FLUTTERWING, LootTable.lootTable());
        add(DWMEntityTypes.TIME_LORD, LootTable.lootTable());
        add(DWMEntityTypes.DALEK, LootTable.lootTable());
        add(
                DWMEntityTypes.MEWING_DOG,
                LootTable.lootTable().withPool(
                        LootPool.lootPool()
                                .setRolls(ContextIntProviders.exactly(1))
                                .add(LootItem.lootTableItem(Items.LEATHER)
                                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                                this.enchantments,
                                                ContextFloatProviders.between(0.0F, 1.0F)
                                        )))
                )
        );
    }
}
