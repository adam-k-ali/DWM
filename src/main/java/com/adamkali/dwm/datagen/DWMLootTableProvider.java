package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import java.util.concurrent.CompletableFuture;

public class DWMLootTableProvider extends FabricBlockLootSubProvider {
    public DWMLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (Block block : DWMBlocks.GALLIFREY_STONE_FAMILY) {
            if (block == DWMBlocks.GALLIFREY_GRASS_BLOCK) {
                continue;
            }
            dropSelf(block);
        }
        add(DWMBlocks.GALLIFREY_GRASS_BLOCK, createSingleItemTableWithSilkTouch(
                DWMBlocks.GALLIFREY_GRASS_BLOCK,
                DWMBlocks.GALLIFREY_DIRT
        ));

        for (Block block : DWMBlocks.CITADEL_BUILDING_BLOCKS) {
            dropSelf(block);
        }
        add(DWMBlocks.CITADEL_GLASS, createSilkTouchOnlyTable(DWMBlocks.CITADEL_GLASS));

        for (var family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.generateLoot(new WoodFamilyDatagen.LootDropSink() {
                @Override
                public void addDrop(Block block) {
                    DWMLootTableProvider.this.dropSelf(block);
                }

                @Override
                public void addDrop(Block block, net.minecraft.world.level.storage.loot.LootTable.Builder builder) {
                    DWMLootTableProvider.this.add(block, builder);
                }

                @Override
                public void addPottedPlantDrops(Block block) {
                    DWMLootTableProvider.this.dropPottedContents(block);
                }

                @Override
                public net.minecraft.world.level.storage.loot.LootTable.Builder slabDrops(Block block) {
                    return DWMLootTableProvider.this.createSlabItemTable(block);
                }

                @Override
                public net.minecraft.world.level.storage.loot.LootTable.Builder leavesDrops(Block leaves, Block sapling, float... chances) {
                    return DWMLootTableProvider.this.createLeavesDrops(leaves, sapling, chances);
                }

                @Override
                public net.minecraft.world.level.storage.loot.LootTable.Builder tallDoorDrops(Block door) {
                    return DWMLootTableProvider.this.createSinglePropConditionTable(
                            door,
                            com.adamkali.dwm.block.wood.TallDoorBlock.SEGMENT,
                            com.adamkali.dwm.block.wood.TallDoorSegment.BOTTOM
                    );
                }

                @Override
                public void excludeFromStrictValidation(Block block) {
                    DWMLootTableProvider.this.excludeFromStrictValidation(block);
                }
            }, family, NORMAL_LEAVES_SAPLING_CHANCES);
        }

        // Existing building blocks that previously had no loot tables.
        dropSelf(DWMBlocks.BLACK_ROUNDEL_A);
        dropSelf(DWMBlocks.BLUE_ROUNDEL_A);
        dropSelf(DWMBlocks.BROWN_ROUNDEL_A);
        dropSelf(DWMBlocks.CYAN_ROUNDEL_A);
        dropSelf(DWMBlocks.GREEN_ROUNDEL_A);
        dropSelf(DWMBlocks.LIGHT_BLUE_ROUNDEL_A);
        dropSelf(DWMBlocks.LIGHT_GRAY_ROUNDEL_A);
        dropSelf(DWMBlocks.LIME_ROUNDEL_A);
        dropSelf(DWMBlocks.MAGENTA_ROUNDEL_A);
        dropSelf(DWMBlocks.ORANGE_ROUNDEL_A);
        dropSelf(DWMBlocks.PINK_ROUNDEL_A);
        dropSelf(DWMBlocks.RED_ROUNDEL_A);
        dropSelf(DWMBlocks.WHITE_ROUNDEL_A);
        dropSelf(DWMBlocks.YELLOW_ROUNDEL_A);
        dropSelf(DWMBlocks.GRAY_ROUNDEL_A);
        dropSelf(DWMBlocks.PURPLE_ROUNDEL_A);
        dropSelf(DWMBlocks.TEAL_ROUNDEL_A);

        dropSelf(DWMBlocks.BLACK_ROUNDEL_B);
        dropSelf(DWMBlocks.BLUE_ROUNDEL_B);
        dropSelf(DWMBlocks.BROWN_ROUNDEL_B);
        dropSelf(DWMBlocks.CYAN_ROUNDEL_B);
        dropSelf(DWMBlocks.GREEN_ROUNDEL_B);
        dropSelf(DWMBlocks.LIGHT_BLUE_ROUNDEL_B);
        dropSelf(DWMBlocks.LIGHT_GRAY_ROUNDEL_B);
        dropSelf(DWMBlocks.LIME_ROUNDEL_B);
        dropSelf(DWMBlocks.MAGENTA_ROUNDEL_B);
        dropSelf(DWMBlocks.ORANGE_ROUNDEL_B);
        dropSelf(DWMBlocks.PINK_ROUNDEL_B);
        dropSelf(DWMBlocks.RED_ROUNDEL_B);
        dropSelf(DWMBlocks.WHITE_ROUNDEL_B);
        dropSelf(DWMBlocks.YELLOW_ROUNDEL_B);
        dropSelf(DWMBlocks.GRAY_ROUNDEL_B);
        dropSelf(DWMBlocks.PURPLE_ROUNDEL_B);
        dropSelf(DWMBlocks.TEAL_ROUNDEL_B);

        dropSelf(DWMBlocks.BLACK_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.BLUE_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.BROWN_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.CYAN_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.GREEN_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.LIGHT_GRAY_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.LIME_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.MAGENTA_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.ORANGE_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.PINK_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.RED_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.WHITE_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.YELLOW_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.GRAY_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.PURPLE_BIG_ROUNDEL_A);
        dropSelf(DWMBlocks.TEAL_BIG_ROUNDEL_A);

        dropSelf(DWMBlocks.BLACK_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.BLUE_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.BROWN_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.CYAN_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.GREEN_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.LIGHT_GRAY_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.LIME_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.MAGENTA_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.ORANGE_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.PINK_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.RED_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.WHITE_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.YELLOW_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.GRAY_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.PURPLE_BIG_ROUNDEL_B);
        dropSelf(DWMBlocks.TEAL_BIG_ROUNDEL_B);

        dropSelf(DWMBlocks.BLACK_TARDIS_WALL);
        dropSelf(DWMBlocks.BLUE_TARDIS_WALL);
        dropSelf(DWMBlocks.BROWN_TARDIS_WALL);
        dropSelf(DWMBlocks.CYAN_TARDIS_WALL);
        dropSelf(DWMBlocks.GREEN_TARDIS_WALL);
        dropSelf(DWMBlocks.LIGHT_BLUE_TARDIS_WALL);
        dropSelf(DWMBlocks.LIGHT_GRAY_TARDIS_WALL);
        dropSelf(DWMBlocks.LIME_TARDIS_WALL);
        dropSelf(DWMBlocks.MAGENTA_TARDIS_WALL);
        dropSelf(DWMBlocks.ORANGE_TARDIS_WALL);
        dropSelf(DWMBlocks.PINK_TARDIS_WALL);
        dropSelf(DWMBlocks.RED_TARDIS_WALL);
        dropSelf(DWMBlocks.WHITE_TARDIS_WALL);
        dropSelf(DWMBlocks.YELLOW_TARDIS_WALL);
        dropSelf(DWMBlocks.GRAY_TARDIS_WALL);
        dropSelf(DWMBlocks.PURPLE_TARDIS_WALL);
        dropSelf(DWMBlocks.TEAL_TARDIS_WALL);

        dropSelf(DWMBlocks.BLACK_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.BLUE_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.BROWN_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.CYAN_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.GREEN_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.LIGHT_BLUE_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.LIGHT_GRAY_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.LIME_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.MAGENTA_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.ORANGE_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.PINK_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.RED_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.WHITE_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.YELLOW_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.GRAY_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.PURPLE_CHRONOPLASM_POWDER);
        dropSelf(DWMBlocks.TEAL_CHRONOPLASM_POWDER);

        dropSelf(DWMBlocks.TARDIS_DOOR_BUTTON);

        // Unbreakable / special blocks: empty drops, excluded from strict validation if needed.
        excludeFromStrictValidation(DWMBlocks.TARDIS_BLOCK);
        excludeFromStrictValidation(DWMBlocks.TARDIS_INTERIOR_DOOR);
        excludeFromStrictValidation(DWMBlocks.FIRST_DOCTOR_CONSOLE);
    }
}
