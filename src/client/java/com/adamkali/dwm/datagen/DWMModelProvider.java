package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.world.level.block.Block;

public class DWMModelProvider extends FabricModelProvider {
    public DWMModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_STONE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_STONE_BRICKS);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_COBBLESTONE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_SMOOTH_STONE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_SAND);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_DIRT);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_COARSE_DIRT);

        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_WALL);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_PANEL);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_TILE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_GLASS);

        registerSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_SANDSTONE);
        registerCutSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_CUT_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);
        registerChiseledSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_CHISELED_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);

        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyClientDatagen.generateBlockModels(blockStateModelGenerator, family);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyClientDatagen.generateItemModels(itemModelGenerator, family);
        }
    }

    private static void registerCubeAll(BlockModelGenerators generator, Block block) {
        generator.createTrivialCube(block);
        generator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }

    private static void registerSandstone(BlockModelGenerators generator, Block sandstone) {
        generator.createTrivialBlock(sandstone, TexturedModel.TOP_BOTTOM_WITH_WALL);
        generator.registerSimpleItemModel(sandstone, ModelLocationUtils.getModelLocation(sandstone));
    }

    private static void registerCutSandstone(BlockModelGenerators generator, Block cutSandstone, Block sandstone) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(cutSandstone))
                .put(TextureSlot.END, TextureMapping.getBlockTexture(sandstone, "_top"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(cutSandstone));
        var modelId = ModelTemplates.CUBE_COLUMN.create(cutSandstone, textures, generator.modelOutput);
        generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(cutSandstone, BlockModelGenerators.plainVariant(modelId)));
        generator.registerSimpleItemModel(cutSandstone, modelId);
    }

    private static void registerChiseledSandstone(BlockModelGenerators generator, Block chiseledSandstone, Block sandstone) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(chiseledSandstone))
                .put(TextureSlot.END, TextureMapping.getBlockTexture(sandstone, "_top"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(chiseledSandstone));
        var modelId = ModelTemplates.CUBE_COLUMN.create(chiseledSandstone, textures, generator.modelOutput);
        generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(chiseledSandstone, BlockModelGenerators.plainVariant(modelId)));
        generator.registerSimpleItemModel(chiseledSandstone, modelId);
    }
}
