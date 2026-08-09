package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class DWMLootTableProvider extends FabricBlockLootTableProvider {
    public DWMLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (Block block : DWMBlocks.GALLIFREY_STONE_FAMILY) {
            addDrop(block);
        }

        for (Block block : DWMBlocks.CITADEL_BUILDING_BLOCKS) {
            addDrop(block);
        }
        addDrop(DWMBlocks.CITADEL_GLASS, dropsWithSilkTouch(DWMBlocks.CITADEL_GLASS));

        for (var family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.generateLoot(new WoodFamilyDatagen.LootDropSink() {
                @Override
                public void addDrop(Block block) {
                    DWMLootTableProvider.this.addDrop(block);
                }

                @Override
                public void addDrop(Block block, net.minecraft.loot.LootTable.Builder builder) {
                    DWMLootTableProvider.this.addDrop(block, builder);
                }

                @Override
                public void addPottedPlantDrops(Block block) {
                    DWMLootTableProvider.this.addPottedPlantDrops(block);
                }

                @Override
                public net.minecraft.loot.LootTable.Builder slabDrops(Block block) {
                    return DWMLootTableProvider.this.slabDrops(block);
                }

                @Override
                public net.minecraft.loot.LootTable.Builder leavesDrops(Block leaves, Block sapling, float... chances) {
                    return DWMLootTableProvider.this.leavesDrops(leaves, sapling, chances);
                }

                @Override
                public net.minecraft.loot.LootTable.Builder tallDoorDrops(Block door) {
                    return DWMLootTableProvider.this.dropsWithProperty(
                            door,
                            com.adamkali.dwm.block.wood.TallDoorBlock.SEGMENT,
                            com.adamkali.dwm.block.wood.TallDoorSegment.BOTTOM
                    );
                }

                @Override
                public void excludeFromStrictValidation(Block block) {
                    DWMLootTableProvider.this.excludeFromStrictValidation(block);
                }
            }, family, SAPLING_DROP_CHANCE);
        }

        // Existing building blocks that previously had no loot tables.
        addDrop(DWMBlocks.BLACK_ROUNDEL_A);
        addDrop(DWMBlocks.BLUE_ROUNDEL_A);
        addDrop(DWMBlocks.BROWN_ROUNDEL_A);
        addDrop(DWMBlocks.CYAN_ROUNDEL_A);
        addDrop(DWMBlocks.GREEN_ROUNDEL_A);
        addDrop(DWMBlocks.LIGHT_BLUE_ROUNDEL_A);
        addDrop(DWMBlocks.LIGHT_GRAY_ROUNDEL_A);
        addDrop(DWMBlocks.LIME_ROUNDEL_A);
        addDrop(DWMBlocks.MAGENTA_ROUNDEL_A);
        addDrop(DWMBlocks.ORANGE_ROUNDEL_A);
        addDrop(DWMBlocks.PINK_ROUNDEL_A);
        addDrop(DWMBlocks.RED_ROUNDEL_A);
        addDrop(DWMBlocks.WHITE_ROUNDEL_A);
        addDrop(DWMBlocks.YELLOW_ROUNDEL_A);
        addDrop(DWMBlocks.GRAY_ROUNDEL_A);
        addDrop(DWMBlocks.PURPLE_ROUNDEL_A);
        addDrop(DWMBlocks.TEAL_ROUNDEL_A);

        addDrop(DWMBlocks.BLACK_ROUNDEL_B);
        addDrop(DWMBlocks.BLUE_ROUNDEL_B);
        addDrop(DWMBlocks.BROWN_ROUNDEL_B);
        addDrop(DWMBlocks.CYAN_ROUNDEL_B);
        addDrop(DWMBlocks.GREEN_ROUNDEL_B);
        addDrop(DWMBlocks.LIGHT_BLUE_ROUNDEL_B);
        addDrop(DWMBlocks.LIGHT_GRAY_ROUNDEL_B);
        addDrop(DWMBlocks.LIME_ROUNDEL_B);
        addDrop(DWMBlocks.MAGENTA_ROUNDEL_B);
        addDrop(DWMBlocks.ORANGE_ROUNDEL_B);
        addDrop(DWMBlocks.PINK_ROUNDEL_B);
        addDrop(DWMBlocks.RED_ROUNDEL_B);
        addDrop(DWMBlocks.WHITE_ROUNDEL_B);
        addDrop(DWMBlocks.YELLOW_ROUNDEL_B);
        addDrop(DWMBlocks.GRAY_ROUNDEL_B);
        addDrop(DWMBlocks.PURPLE_ROUNDEL_B);
        addDrop(DWMBlocks.TEAL_ROUNDEL_B);

        addDrop(DWMBlocks.BLACK_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.BLUE_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.BROWN_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.CYAN_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.GREEN_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.LIGHT_GRAY_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.LIME_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.MAGENTA_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.ORANGE_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.PINK_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.RED_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.WHITE_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.YELLOW_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.GRAY_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.PURPLE_BIG_ROUNDEL_A);
        addDrop(DWMBlocks.TEAL_BIG_ROUNDEL_A);

        addDrop(DWMBlocks.BLACK_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.BLUE_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.BROWN_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.CYAN_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.GREEN_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.LIGHT_GRAY_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.LIME_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.MAGENTA_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.ORANGE_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.PINK_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.RED_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.WHITE_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.YELLOW_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.GRAY_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.PURPLE_BIG_ROUNDEL_B);
        addDrop(DWMBlocks.TEAL_BIG_ROUNDEL_B);

        addDrop(DWMBlocks.BLACK_TARDIS_WALL);
        addDrop(DWMBlocks.BLUE_TARDIS_WALL);
        addDrop(DWMBlocks.BROWN_TARDIS_WALL);
        addDrop(DWMBlocks.CYAN_TARDIS_WALL);
        addDrop(DWMBlocks.GREEN_TARDIS_WALL);
        addDrop(DWMBlocks.LIGHT_BLUE_TARDIS_WALL);
        addDrop(DWMBlocks.LIGHT_GRAY_TARDIS_WALL);
        addDrop(DWMBlocks.LIME_TARDIS_WALL);
        addDrop(DWMBlocks.MAGENTA_TARDIS_WALL);
        addDrop(DWMBlocks.ORANGE_TARDIS_WALL);
        addDrop(DWMBlocks.PINK_TARDIS_WALL);
        addDrop(DWMBlocks.RED_TARDIS_WALL);
        addDrop(DWMBlocks.WHITE_TARDIS_WALL);
        addDrop(DWMBlocks.YELLOW_TARDIS_WALL);
        addDrop(DWMBlocks.GRAY_TARDIS_WALL);
        addDrop(DWMBlocks.PURPLE_TARDIS_WALL);
        addDrop(DWMBlocks.TEAL_TARDIS_WALL);

        addDrop(DWMBlocks.BLACK_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.BLUE_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.BROWN_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.CYAN_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.GREEN_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.LIGHT_BLUE_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.LIGHT_GRAY_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.LIME_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.MAGENTA_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.ORANGE_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.PINK_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.RED_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.WHITE_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.YELLOW_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.GRAY_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.PURPLE_CHRONOPLASM_POWDER);
        addDrop(DWMBlocks.TEAL_CHRONOPLASM_POWDER);

        addDrop(DWMBlocks.TARDIS_DOOR_BUTTON);

        // Unbreakable / special blocks: empty drops, excluded from strict validation if needed.
        excludeFromStrictValidation(DWMBlocks.TARDIS_BLOCK);
        excludeFromStrictValidation(DWMBlocks.TARDIS_INTERIOR_DOOR);
        excludeFromStrictValidation(DWMBlocks.FIRST_DOCTOR_CONSOLE);
    }
}
