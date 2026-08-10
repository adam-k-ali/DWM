package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.concurrent.CompletableFuture;

public class DWMWorldgenProvider extends FabricDynamicRegistryProvider {
    public DWMWorldgenProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        addModEntries(registries, entries, RegistryKeys.CONFIGURED_FEATURE);
        addModEntries(registries, entries, RegistryKeys.PLACED_FEATURE);
        addModEntries(registries, entries, RegistryKeys.BIOME);
        addModEntries(registries, entries, RegistryKeys.CHUNK_GENERATOR_SETTINGS);
    }

    private static <T> void addModEntries(
            RegistryWrapper.WrapperLookup registries,
            Entries entries,
            RegistryKey<Registry<T>> registryKey
    ) {
        RegistryWrapper.Impl<T> registry = registries.getOrThrow(registryKey);
        for (RegistryEntry.Reference<T> entry : registry.streamEntries().toList()) {
            if (entry.registryKey().getValue().getNamespace().equals(DWMReference.MOD_ID)) {
                entries.add(entry.registryKey(), entry.value());
            }
        }
    }

    @Override
    public String getName() {
        return DWMReference.MOD_ID + " Worldgen";
    }
}
