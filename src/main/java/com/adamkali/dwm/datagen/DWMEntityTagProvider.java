package com.adamkali.dwm.datagen;

import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import java.util.concurrent.CompletableFuture;

public class DWMEntityTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
    public DWMEntityTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(EntityTypeTags.FALL_DAMAGE_IMMUNE)
                .add(key(DWMEntityTypes.FLUTTERWING));
    }

    private static net.minecraft.resources.ResourceKey<EntityType<?>> key(EntityType<?> type) {
        return type.builtInRegistryHolder().key();
    }
}
