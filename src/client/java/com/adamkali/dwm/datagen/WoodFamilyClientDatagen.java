package com.adamkali.dwm.datagen;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyBlocks;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.util.Identifier;

public final class WoodFamilyClientDatagen {
    private WoodFamilyClientDatagen() {
    }

    public static void generateBlockModels(BlockStateModelGenerator generator, RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        generator.registerLog(blocks.log()).log(blocks.log()).wood(blocks.wood());
        generator.registerLog(blocks.strippedLog()).log(blocks.strippedLog()).wood(blocks.strippedWood());
        generator.registerSingleton(blocks.leaves(), TexturedModel.LEAVES);
        generator.registerParentedItemModel(blocks.leaves(), ModelIds.getBlockModelId(blocks.leaves()));
        generator.registerFlowerPotPlantAndItem(
                blocks.sapling(),
                blocks.pottedSapling(),
                BlockStateModelGenerator.CrossType.NOT_TINTED
        );
        generator.registerCubeAllModelTexturePool(blocks.planks()).family(family.vanillaModelFamily());
        generator.registerHangingSign(blocks.strippedLog(), blocks.hangingSign(), blocks.wallHangingSign());
        if (family.has(WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL) && blocks.trapdoor() != null) {
            Identifier bottomModel = Identifier.of(
                    DWMReference.MOD_ID,
                    "block/" + family.definition().id() + "_trapdoor_bottom"
            );
            generator.registerParentedItemModel(blocks.trapdoor(), bottomModel);
        }
    }

    public static void generateItemModels(ItemModelGenerator generator, RegisteredWoodFamily family) {
        generator.register(family.boatItem(), Models.GENERATED);
        if (family.has(WoodFamilyFeature.CUSTOM_DOOR_MODEL) && family.doorOrNull() != null) {
            generator.register(family.doorOrNull().asItem(), Models.GENERATED);
        }
    }
}
