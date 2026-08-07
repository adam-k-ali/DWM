package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlockTags;
import com.adamkali.dwm.item.DWMItemTags;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class DWMItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public DWMItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture,
            FabricTagProvider.BlockTagProvider blockTagProvider
    ) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        copy(DWMBlockTags.GALLIFREY_STONE, DWMItemTags.GALLIFREY_STONE);
        copy(DWMBlockTags.ASH_LOGS, DWMItemTags.ASH_LOGS);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        getOrCreateTagBuilder(ItemTags.SIGNS)
                .add(DWMItems.ASH_SIGN);

        getOrCreateTagBuilder(ItemTags.HANGING_SIGNS)
                .add(DWMItems.ASH_HANGING_SIGN);

        getOrCreateTagBuilder(ItemTags.BOATS)
                .add(DWMItems.ASH_BOAT);
    }
}
