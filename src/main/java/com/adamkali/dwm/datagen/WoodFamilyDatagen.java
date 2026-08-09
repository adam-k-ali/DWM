package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyBlocks;
import com.adamkali.dwm.block.wood.WoodFamilyFeature;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

/**
 * Shared datagen helpers for {@link RegisteredWoodFamily} instances.
 */
public final class WoodFamilyDatagen {
    private WoodFamilyDatagen() {
    }

    public static void generateRecipes(RecipeGenerator generator, RecipeExporter exporter, RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        generator.offerPlanksRecipe(blocks.planks(), family.definition().logItemTag(), 4);
        generator.offerBarkBlockRecipe(blocks.wood(), blocks.log());
        generator.offerBarkBlockRecipe(blocks.strippedWood(), blocks.strippedLog());
        generator.createStairsRecipe(blocks.stairs(), Ingredient.ofItem(blocks.planks()))
                .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                .offerTo(exporter);
        generator.offerSlabRecipe(RecipeCategory.BUILDING_BLOCKS, blocks.slab(), blocks.planks());
        generator.createFenceRecipe(blocks.fence(), Ingredient.ofItem(blocks.planks()))
                .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                .offerTo(exporter);
        generator.createFenceGateRecipe(blocks.fenceGate(), Ingredient.ofItem(blocks.planks()))
                .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                .offerTo(exporter);
        generator.offerPressurePlateRecipe(blocks.pressurePlate(), blocks.planks());
        generator.createButtonRecipe(blocks.button(), Ingredient.ofItem(blocks.planks()))
                .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                .offerTo(exporter);
        if (family.hasDoor()) {
            generator.createDoorRecipe(family.requireDoor(), Ingredient.ofItem(blocks.planks()))
                    .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                    .offerTo(exporter);
        }
        if (family.has(WoodFamilyFeature.TRAPDOOR)) {
            generator.createTrapdoorRecipe(family.requireTrapdoor(), Ingredient.ofItem(blocks.planks()))
                    .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                    .offerTo(exporter);
        }
        generator.createSignRecipe(family.signItem(), Ingredient.ofItem(blocks.planks()))
                .criterion(generator.hasItem(blocks.planks()), generator.conditionsFromItem(blocks.planks()))
                .offerTo(exporter);
        generator.offerHangingSignRecipe(family.hangingSignItem(), blocks.strippedLog());
        generator.offerBoatRecipe(family.boatItem(), blocks.planks());
    }

    public interface LootDropSink {
        void addDrop(net.minecraft.block.Block block);

        void addDrop(net.minecraft.block.Block block, LootTable.Builder builder);

        void addPottedPlantDrops(net.minecraft.block.Block block);

        LootTable.Builder slabDrops(net.minecraft.block.Block block);

        LootTable.Builder leavesDrops(net.minecraft.block.Block leaves, net.minecraft.block.Block sapling, float... chances);

        LootTable.Builder tallDoorDrops(net.minecraft.block.Block door);

        void excludeFromStrictValidation(net.minecraft.block.Block block);
    }

    public static void generateLoot(LootDropSink loot, RegisteredWoodFamily family, float[] saplingDropChance) {
        WoodFamilyBlocks blocks = family.blocks();
        loot.addDrop(blocks.planks());
        loot.addDrop(blocks.log());
        loot.addDrop(blocks.wood());
        loot.addDrop(blocks.strippedLog());
        loot.addDrop(blocks.strippedWood());
        loot.addDrop(blocks.stairs());
        loot.addDrop(blocks.slab(), loot.slabDrops(blocks.slab()));
        loot.addDrop(blocks.fence());
        loot.addDrop(blocks.fenceGate());
        loot.addDrop(blocks.button());
        loot.addDrop(blocks.pressurePlate());
        if (family.has(WoodFamilyFeature.TALL_DOOR)) {
            loot.addDrop(family.requireDoor(), loot.tallDoorDrops(family.requireDoor()));
        } else if (family.has(WoodFamilyFeature.DOOR)) {
            loot.addDrop(family.requireDoor());
        }
        if (family.has(WoodFamilyFeature.TRAPDOOR)) {
            loot.addDrop(family.requireTrapdoor());
        }
        loot.addDrop(blocks.sign());
        loot.addDrop(blocks.hangingSign());
        loot.addDrop(blocks.sapling());
        loot.addPottedPlantDrops(blocks.pottedSapling());
        loot.addDrop(blocks.leaves(), loot.leavesDrops(blocks.leaves(), blocks.sapling(), saplingDropChance));
        loot.excludeFromStrictValidation(blocks.wallSign());
        loot.excludeFromStrictValidation(blocks.wallHangingSign());
    }

    public interface LangSink {
        void addBlockAndItem(net.minecraft.block.Block block, String name);

        void add(net.minecraft.block.Block block, String name);

        void add(net.minecraft.item.Item item, String name);

        void add(net.minecraft.entity.EntityType<?> type, String name);
    }

    public static void addTranslations(LangSink lang, RegisteredWoodFamily family) {
        String name = family.definition().displayName();
        WoodFamilyBlocks blocks = family.blocks();
        lang.addBlockAndItem(blocks.planks(), name + " Planks");
        lang.addBlockAndItem(blocks.log(), name + " Log");
        lang.addBlockAndItem(blocks.wood(), name + " Wood");
        lang.addBlockAndItem(blocks.strippedLog(), "Stripped " + name + " Log");
        lang.addBlockAndItem(blocks.strippedWood(), "Stripped " + name + " Wood");
        lang.addBlockAndItem(blocks.leaves(), name + " Leaves");
        lang.addBlockAndItem(blocks.sapling(), name + " Sapling");
        lang.addBlockAndItem(blocks.stairs(), name + " Stairs");
        lang.addBlockAndItem(blocks.slab(), name + " Slab");
        lang.addBlockAndItem(blocks.fence(), name + " Fence");
        lang.addBlockAndItem(blocks.fenceGate(), name + " Fence Gate");
        lang.addBlockAndItem(blocks.button(), name + " Button");
        lang.addBlockAndItem(blocks.pressurePlate(), name + " Pressure Plate");
        if (family.hasDoor()) {
            lang.addBlockAndItem(family.requireDoor(), name + " Door");
        }
        if (family.has(WoodFamilyFeature.TRAPDOOR)) {
            lang.addBlockAndItem(family.requireTrapdoor(), name + " Trapdoor");
        }
        lang.add(blocks.sign(), name + " Sign");
        lang.add(family.signItem(), name + " Sign");
        lang.add(blocks.hangingSign(), name + " Hanging Sign");
        lang.add(family.hangingSignItem(), name + " Hanging Sign");
        lang.add(blocks.pottedSapling(), "Potted " + name + " Sapling");
        lang.add(family.boatItem(), name + " Boat");
        lang.add(family.boatEntity(), name + " Boat");
    }

    public interface BlockTagSink {
        void addToTag(net.minecraft.registry.tag.TagKey<net.minecraft.block.Block> tag, net.minecraft.block.Block block);

        void addTagToTag(net.minecraft.registry.tag.TagKey<net.minecraft.block.Block> tag, net.minecraft.registry.tag.TagKey<net.minecraft.block.Block> nested);
    }

    public static void generateBlockTags(BlockTagSink tags, RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        for (net.minecraft.block.Block log : family.logs()) {
            tags.addToTag(family.definition().logBlockTag(), log);
        }
        for (net.minecraft.block.Block block : family.axeMineableBlocks()) {
            tags.addToTag(BlockTags.AXE_MINEABLE, block);
        }
        tags.addTagToTag(BlockTags.LOGS_THAT_BURN, family.definition().logBlockTag());
        tags.addTagToTag(BlockTags.LOGS, family.definition().logBlockTag());
        tags.addToTag(BlockTags.OVERWORLD_NATURAL_LOGS, blocks.log());
        tags.addToTag(BlockTags.PLANKS, blocks.planks());
        tags.addToTag(BlockTags.WOODEN_STAIRS, blocks.stairs());
        tags.addToTag(BlockTags.WOODEN_SLABS, blocks.slab());
        tags.addToTag(BlockTags.WOODEN_FENCES, blocks.fence());
        tags.addToTag(BlockTags.FENCE_GATES, blocks.fenceGate());
        tags.addToTag(BlockTags.WOODEN_BUTTONS, blocks.button());
        tags.addToTag(BlockTags.WOODEN_PRESSURE_PLATES, blocks.pressurePlate());
        if (family.hasDoor()) {
            tags.addToTag(BlockTags.WOODEN_DOORS, family.requireDoor());
            tags.addToTag(BlockTags.DOORS, family.requireDoor());
        }
        if (family.has(WoodFamilyFeature.TRAPDOOR)) {
            tags.addToTag(BlockTags.WOODEN_TRAPDOORS, family.requireTrapdoor());
            tags.addToTag(BlockTags.TRAPDOORS, family.requireTrapdoor());
        }
        tags.addToTag(BlockTags.STANDING_SIGNS, blocks.sign());
        tags.addToTag(BlockTags.WALL_SIGNS, blocks.wallSign());
        tags.addToTag(BlockTags.CEILING_HANGING_SIGNS, blocks.hangingSign());
        tags.addToTag(BlockTags.WALL_HANGING_SIGNS, blocks.wallHangingSign());
        tags.addToTag(BlockTags.LEAVES, blocks.leaves());
        tags.addToTag(BlockTags.SAPLINGS, blocks.sapling());
        tags.addToTag(BlockTags.FLOWER_POTS, blocks.pottedSapling());
    }

    public interface ItemTagSink {
        void copy(net.minecraft.registry.tag.TagKey<net.minecraft.block.Block> blockTag, net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> itemTag);

        void addToTag(net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> tag, net.minecraft.item.Item item);
    }

    public static void generateItemTags(ItemTagSink tags, RegisteredWoodFamily family) {
        tags.copy(family.definition().logBlockTag(), family.definition().logItemTag());
        tags.addToTag(ItemTags.SIGNS, family.signItem());
        tags.addToTag(ItemTags.HANGING_SIGNS, family.hangingSignItem());
        tags.addToTag(ItemTags.BOATS, family.boatItem());
    }
}
