package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import java.util.concurrent.CompletableFuture;

public class DWMWorldgenProvider extends FabricDynamicRegistryProvider {
    public DWMWorldgenProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        addModEntries(registries, entries, Registries.CONFIGURED_FEATURE);
        addModEntries(registries, entries, Registries.PLACED_FEATURE);
        addModEntries(registries, entries, Registries.BIOME);
        addModEntries(registries, entries, Registries.NOISE_SETTINGS);
        addModEntries(registries, entries, Registries.PROCESSOR_LIST);
        addModEntries(registries, entries, Registries.TEMPLATE_POOL);
        addModEntries(registries, entries, Registries.STRUCTURE);
        addModEntries(registries, entries, Registries.STRUCTURE_SET);
    }

    private static <T> void addModEntries(
            HolderLookup.Provider registries,
            Entries entries,
            ResourceKey<Registry<T>> registryKey
    ) {
        HolderLookup.RegistryLookup<T> registry = registries.lookupOrThrow(registryKey);
        for (Holder.Reference<T> entry : registry.listElements().toList()) {
            if (entry.key().identifier().getNamespace().equals(DWMReference.MOD_ID)) {
                entries.add(entry.key(), entry.value());
            }
        }
    }

    @Override
    public String getName() {
        return DWMReference.MOD_ID + " Worldgen";
    }
}
