package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyBlocks;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;

public final class WoodFamilyClientDatagen {
    private WoodFamilyClientDatagen() {
    }

    public static void generateBlockModels(BlockModelGenerators generator, RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        generator.woodProvider(blocks.log()).log(blocks.log()).wood(blocks.wood());
        generator.woodProvider(blocks.strippedLog()).log(blocks.strippedLog()).wood(blocks.strippedWood());
        generator.createTrivialBlock(blocks.leaves(), TexturedModel.LEAVES);
        generator.registerSimpleItemModel(blocks.leaves(), ModelLocationUtils.getModelLocation(blocks.leaves()));
        generator.createPlantWithDefaultItem(
                blocks.sapling(),
                blocks.pottedSapling(),
                BlockModelGenerators.PlantType.NOT_TINTED
        );
        generator.family(blocks.planks()).generateFor(family.vanillaModelFamily());
        if (family.has(WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL) && blocks.trapdoor() != null) {
            Identifier bottomModel = Identifier.fromNamespaceAndPath(
                    DWMReference.MOD_ID,
                    "block/" + family.definition().id() + "_trapdoor_bottom"
            );
            generator.registerSimpleItemModel(blocks.trapdoor(), bottomModel);
        }
    }

    public static void generateItemModels(ItemModelGenerators generator, RegisteredWoodFamily family) {
        generator.generateFlatItem(family.boatItem(), ModelTemplates.FLAT_ITEM);
        if (family.has(WoodFamilyFeature.CUSTOM_DOOR_MODEL) && family.doorOrNull() != null) {
            generator.generateFlatItem(family.doorOrNull().asItem(), ModelTemplates.FLAT_ITEM);
        }
    }
}
