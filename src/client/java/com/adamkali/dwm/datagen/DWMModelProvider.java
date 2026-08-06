package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.data.family.BlockFamily;

public class DWMModelProvider extends FabricModelProvider {
    private static final BlockFamily ASH_FAMILY = new BlockFamily.Builder(DWMBlocks.ASH_PLANKS)
            .stairs(DWMBlocks.ASH_STAIRS)
            .slab(DWMBlocks.ASH_SLAB)
            .fence(DWMBlocks.ASH_FENCE)
            .fenceGate(DWMBlocks.ASH_FENCE_GATE)
            .button(DWMBlocks.ASH_BUTTON)
            .pressurePlate(DWMBlocks.ASH_PRESSURE_PLATE)
            .sign(DWMBlocks.ASH_SIGN, DWMBlocks.ASH_WALL_SIGN)
            .group("wooden")
            .unlockCriterionName("has_planks")
            .build();

    public DWMModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
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

        registerSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_SANDSTONE);
        registerCutSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_CUT_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);
        registerChiseledSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_CHISELED_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);

        blockStateModelGenerator.registerLog(DWMBlocks.ASH_LOG).log(DWMBlocks.ASH_LOG).wood(DWMBlocks.ASH_WOOD);
        blockStateModelGenerator.registerLog(DWMBlocks.STRIPPED_ASH_LOG)
                .log(DWMBlocks.STRIPPED_ASH_LOG)
                .wood(DWMBlocks.STRIPPED_ASH_WOOD);
        blockStateModelGenerator.registerSingleton(DWMBlocks.ASH_LEAVES, TexturedModel.LEAVES);
        blockStateModelGenerator.registerParentedItemModel(DWMBlocks.ASH_LEAVES, ModelIds.getBlockModelId(DWMBlocks.ASH_LEAVES));
        blockStateModelGenerator.registerFlowerPotPlantAndItem(
                DWMBlocks.ASH_SAPLING,
                DWMBlocks.POTTED_ASH_SAPLING,
                BlockStateModelGenerator.CrossType.NOT_TINTED
        );
        blockStateModelGenerator.registerCubeAllModelTexturePool(DWMBlocks.ASH_PLANKS).family(ASH_FAMILY);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(DWMItems.ASH_BOAT, Models.GENERATED);
    }

    private static void registerCubeAll(BlockStateModelGenerator generator, Block block) {
        generator.registerSimpleCubeAll(block);
        generator.registerParentedItemModel(block, ModelIds.getBlockModelId(block));
    }

    private static void registerSandstone(BlockStateModelGenerator generator, Block sandstone) {
        generator.registerSingleton(sandstone, TexturedModel.SIDE_TOP_BOTTOM_WALL);
        generator.registerParentedItemModel(sandstone, ModelIds.getBlockModelId(sandstone));
    }

    private static void registerCutSandstone(BlockStateModelGenerator generator, Block cutSandstone, Block sandstone) {
        TextureMap textures = new TextureMap()
                .put(TextureKey.SIDE, TextureMap.getId(cutSandstone))
                .put(TextureKey.END, TextureMap.getSubId(sandstone, "_top"))
                .put(TextureKey.PARTICLE, TextureMap.getId(cutSandstone));
        var modelId = Models.CUBE_COLUMN.upload(cutSandstone, textures, generator.modelCollector);
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(cutSandstone, modelId));
        generator.registerParentedItemModel(cutSandstone, modelId);
    }

    private static void registerChiseledSandstone(BlockStateModelGenerator generator, Block chiseledSandstone, Block sandstone) {
        TextureMap textures = new TextureMap()
                .put(TextureKey.SIDE, TextureMap.getId(chiseledSandstone))
                .put(TextureKey.END, TextureMap.getSubId(sandstone, "_top"))
                .put(TextureKey.PARTICLE, TextureMap.getId(chiseledSandstone));
        var modelId = Models.CUBE_COLUMN.upload(chiseledSandstone, textures, generator.modelCollector);
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(chiseledSandstone, modelId));
        generator.registerParentedItemModel(chiseledSandstone, modelId);
    }
}
