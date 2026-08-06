package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class DWMWorldgenProvider extends FabricDynamicRegistryProvider {
    public DWMWorldgenProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.addAll(registries.getOrThrow(RegistryKeys.CONFIGURED_FEATURE));
    }

    @Override
    public String getName() {
        return DWMReference.MOD_ID + " Worldgen";
    }
}
