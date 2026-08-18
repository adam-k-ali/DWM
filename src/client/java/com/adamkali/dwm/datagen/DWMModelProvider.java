package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.item.DWMItems;
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
        registerGallifreyGrass(blockStateModelGenerator);

        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_WALL);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_PANEL);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_TILE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.CITADEL_GLASS);

        registerSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_SANDSTONE);
        registerCutSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_CUT_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);
        registerChiseledSandstone(blockStateModelGenerator, DWMBlocks.GALLIFREY_CHISELED_SANDSTONE, DWMBlocks.GALLIFREY_SANDSTONE);

        registerOrangeSandFamily(blockStateModelGenerator);

        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyClientDatagen.generateBlockModels(blockStateModelGenerator, family);
        }

        registerGallifreyPlants(blockStateModelGenerator);

        registerCubeAll(blockStateModelGenerator, DWMBlocks.AZBANTIUM_ORE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.AZBANTIUM_BLOCK);

        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_COAL_ORE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_IRON_ORE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_GOLD_ORE);
        registerCubeAll(blockStateModelGenerator, DWMBlocks.GALLIFREY_DIAMOND_ORE);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyClientDatagen.generateItemModels(itemModelGenerator, family);
        }

        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.AZBANTIUM_BOOTS, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(DWMItems.STATTENHEIM_REMOTE, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    private static void registerGallifreyPlants(BlockModelGenerators generator) {
        generator.createPlantWithDefaultItem(
                DWMBlocks.FLOWER_OF_REMEMBRANCE,
                DWMBlocks.POTTED_FLOWER_OF_REMEMBRANCE,
                BlockModelGenerators.PlantType.NOT_TINTED
        );
        generator.createPlantWithDefaultItem(
                DWMBlocks.MOONLIGHT_BLOOM,
                DWMBlocks.POTTED_MOONLIGHT_BLOOM,
                BlockModelGenerators.PlantType.NOT_TINTED
        );
        // Cross block model from block texture; flat item uses textures/item/saccharine_cane.png
        generator.createCrossBlock(DWMBlocks.SACCHARINE_CANE, BlockModelGenerators.PlantType.NOT_TINTED);
        generator.registerSimpleFlatItemModel(DWMBlocks.SACCHARINE_CANE.asItem());
    }

    private static void registerCubeAll(BlockModelGenerators generator, Block block) {
        generator.createTrivialCube(block);
        generator.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
    }

    private static void registerGallifreyGrass(BlockModelGenerators generator) {
        Block grass = DWMBlocks.GALLIFREY_GRASS_BLOCK;
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(grass, "_top"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(DWMBlocks.GALLIFREY_DIRT))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(grass, "_side"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(grass, "_side"));
        var modelId = ModelTemplates.CUBE_BOTTOM_TOP.create(grass, textures, generator.modelOutput);
        generator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(grass, BlockModelGenerators.plainVariant(modelId)));
        generator.registerSimpleItemModel(grass, modelId);
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

    /**
     * Vanilla red-sandstone parity: sand + sandstone stairs/slab/wall + cut slab + smooth stairs/slab.
     * Smooth reuses {@code orange_sandstone_top} (no dedicated smooth texture).
     */
    private static void registerOrangeSandFamily(BlockModelGenerators generator) {
        registerCubeAll(generator, DWMBlocks.ORANGE_SAND);

        TexturedModel sandstone = TexturedModel.TOP_BOTTOM_WITH_WALL.get(DWMBlocks.ORANGE_SANDSTONE);
        generator.new BlockFamilyProvider(sandstone.getMapping())
                .fullBlock(DWMBlocks.ORANGE_SANDSTONE, sandstone.getTemplate())
                .stairs(DWMBlocks.ORANGE_SANDSTONE_STAIRS)
                .slab(DWMBlocks.ORANGE_SANDSTONE_SLAB)
                .wall(DWMBlocks.ORANGE_SANDSTONE_WALL);
        generator.registerSimpleItemModel(
                DWMBlocks.ORANGE_SANDSTONE,
                ModelLocationUtils.getModelLocation(DWMBlocks.ORANGE_SANDSTONE)
        );

        TexturedModel cut = TexturedModel.COLUMN.get(DWMBlocks.ORANGE_SANDSTONE)
                .updateTextures(m -> m.put(
                        TextureSlot.SIDE,
                        TextureMapping.getBlockTexture(DWMBlocks.CUT_ORANGE_SANDSTONE)
                ));
        generator.new BlockFamilyProvider(cut.getMapping())
                .fullBlock(DWMBlocks.CUT_ORANGE_SANDSTONE, cut.getTemplate())
                .slab(DWMBlocks.CUT_ORANGE_SANDSTONE_SLAB);
        generator.registerSimpleItemModel(
                DWMBlocks.CUT_ORANGE_SANDSTONE,
                ModelLocationUtils.getModelLocation(DWMBlocks.CUT_ORANGE_SANDSTONE)
        );

        registerChiseledSandstone(
                generator,
                DWMBlocks.CHISELED_ORANGE_SANDSTONE,
                DWMBlocks.ORANGE_SANDSTONE
        );

        TexturedModel smooth = TexturedModel.createAllSame(
                TextureMapping.getBlockTexture(DWMBlocks.ORANGE_SANDSTONE, "_top")
        );
        generator.new BlockFamilyProvider(smooth.getMapping())
                .fullBlock(DWMBlocks.SMOOTH_ORANGE_SANDSTONE, smooth.getTemplate())
                .stairs(DWMBlocks.SMOOTH_ORANGE_SANDSTONE_STAIRS)
                .slab(DWMBlocks.SMOOTH_ORANGE_SANDSTONE_SLAB);
        generator.registerSimpleItemModel(
                DWMBlocks.SMOOTH_ORANGE_SANDSTONE,
                ModelLocationUtils.getModelLocation(DWMBlocks.SMOOTH_ORANGE_SANDSTONE)
        );
    }
}
