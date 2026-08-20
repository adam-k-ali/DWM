package com.adamkali.dwm.datagen;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import java.util.concurrent.CompletableFuture;

public class DWMLanguageProvider extends FabricLanguageProvider {
    public DWMLanguageProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        addItems(translationBuilder);
        addBuildingBlocks(translationBuilder);
        addGallifreyStoneFamily(translationBuilder);
        addOrangeSandFamily(translationBuilder);
        addCitadelFamily(translationBuilder);
        addGallifreyPlants(translationBuilder);
        addAzbantiumSet(translationBuilder);
        addGallifreyVanillaOres(translationBuilder);
        for (var family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyDatagen.addTranslations(new WoodFamilyDatagen.LangSink() {
                @Override
                public void addBlockAndItem(net.minecraft.world.level.block.Block block, String name) {
                    DWMLanguageProvider.addBlockAndItem(translationBuilder, block, name);
                }

                @Override
                public void add(net.minecraft.world.level.block.Block block, String name) {
                    translationBuilder.add(block, name);
                }

                @Override
                public void add(net.minecraft.world.item.Item item, String name) {
                    translationBuilder.add(item, name);
                }

                @Override
                public void add(net.minecraft.world.entity.EntityType<?> type, String name) {
                    translationBuilder.add(type, name);
                }
            }, family);
        }
        addMisc(translationBuilder);
    }

    private static void addItems(TranslationBuilder t) {
        addItem(t, DWMItems.SONIC_SECOND_DOCTOR, "Sonic Screwdriver (Second Doctor)");
        addItem(t, DWMItems.SONIC_THIRD_DOCTOR, "Sonic Screwdriver (Third Doctor)");
        addItem(t, DWMItems.SONIC_FOURTH_DOCTOR, "Sonic Screwdriver (Fourth Doctor)");
        addItem(t, DWMItems.SONIC_FIFTH_DOCTOR, "Sonic Screwdriver (Fifth Doctor)");
        addItem(t, DWMItems.TARDIS_KEY, "TARDIS Key");
        t.add("dwm.key.bound", "Key bound to this TARDIS");
        t.add("dwm.key.bind_not_owner", "Only the TARDIS owner can bind a key");
        t.add("dwm.key.wrong_tardis", "This key is bound to another TARDIS");
        t.add("dwm.key.tooltip.unbound", "Unbound");
        t.add("dwm.key.tooltip.bound", "Bound to a TARDIS");
        addItem(t, DWMItems.STATTENHEIM_REMOTE, "Stattenheim Remote");
        t.add("dwm.stattenheim.summoned", "TARDIS incoming");
        t.add("dwm.stattenheim.no_tardis", "You do not own a TARDIS");
        t.add("dwm.stattenheim.in_progress", "TARDIS is already travelling");
        t.add("dwm.stattenheim.invalid_landing", "Cannot land here");
        t.add("dwm.stattenheim.unavailable", "Cannot summon the TARDIS");
    }

    private static void addBuildingBlocks(TranslationBuilder t) {
        addBlockItem(t, DWMBlocks.BLACK_CHRONOPLASM_POWDER, "Black Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.BLUE_CHRONOPLASM_POWDER, "Blue Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.BROWN_CHRONOPLASM_POWDER, "Brown Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.CYAN_CHRONOPLASM_POWDER, "Cyan Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.GREEN_CHRONOPLASM_POWDER, "Green Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.LIGHT_BLUE_CHRONOPLASM_POWDER, "Light Blue Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.LIGHT_GRAY_CHRONOPLASM_POWDER, "Light Gray Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.LIME_CHRONOPLASM_POWDER, "Lime Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.MAGENTA_CHRONOPLASM_POWDER, "Magenta Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.ORANGE_CHRONOPLASM_POWDER, "Orange Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.PINK_CHRONOPLASM_POWDER, "Pink Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.RED_CHRONOPLASM_POWDER, "Red Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.WHITE_CHRONOPLASM_POWDER, "White Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.YELLOW_CHRONOPLASM_POWDER, "Yellow Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.GRAY_CHRONOPLASM_POWDER, "Gray Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.PURPLE_CHRONOPLASM_POWDER, "Purple Chronoplasm Powder");
        addBlockItem(t, DWMBlocks.TEAL_CHRONOPLASM_POWDER, "Teal Chronoplasm Powder");

        addBlockItem(t, DWMBlocks.BLACK_TARDIS_WALL, "Black TARDIS Wall");
        addBlockItem(t, DWMBlocks.BLUE_TARDIS_WALL, "Blue TARDIS Wall");
        addBlockItem(t, DWMBlocks.BROWN_TARDIS_WALL, "Brown TARDIS Wall");
        addBlockItem(t, DWMBlocks.CYAN_TARDIS_WALL, "Cyan TARDIS Wall");
        addBlockItem(t, DWMBlocks.GREEN_TARDIS_WALL, "Green TARDIS Wall");
        addBlockItem(t, DWMBlocks.LIGHT_BLUE_TARDIS_WALL, "Light Blue TARDIS Wall");
        addBlockItem(t, DWMBlocks.LIGHT_GRAY_TARDIS_WALL, "Light Gray TARDIS Wall");
        addBlockItem(t, DWMBlocks.LIME_TARDIS_WALL, "Lime TARDIS Wall");
        addBlockItem(t, DWMBlocks.MAGENTA_TARDIS_WALL, "Magenta TARDIS Wall");
        addBlockItem(t, DWMBlocks.ORANGE_TARDIS_WALL, "Orange TARDIS Wall");
        addBlockItem(t, DWMBlocks.PINK_TARDIS_WALL, "Pink TARDIS Wall");
        addBlockItem(t, DWMBlocks.RED_TARDIS_WALL, "Red TARDIS Wall");
        addBlockItem(t, DWMBlocks.WHITE_TARDIS_WALL, "White TARDIS Wall");
        addBlockItem(t, DWMBlocks.YELLOW_TARDIS_WALL, "Yellow TARDIS Wall");
        addBlockItem(t, DWMBlocks.GRAY_TARDIS_WALL, "Gray TARDIS Wall");
        addBlockItem(t, DWMBlocks.PURPLE_TARDIS_WALL, "Purple TARDIS Wall");
        addBlockItem(t, DWMBlocks.TEAL_TARDIS_WALL, "Teal TARDIS Wall");

        addBlockItem(t, DWMBlocks.BLACK_ROUNDEL_A, "Black Roundel A");
        addBlockItem(t, DWMBlocks.BLUE_ROUNDEL_A, "Blue Roundel A");
        addBlockItem(t, DWMBlocks.BROWN_ROUNDEL_A, "Brown Roundel A");
        addBlockItem(t, DWMBlocks.CYAN_ROUNDEL_A, "Cyan Roundel A");
        addBlockItem(t, DWMBlocks.GREEN_ROUNDEL_A, "Green Roundel A");
        addBlockItem(t, DWMBlocks.LIGHT_BLUE_ROUNDEL_A, "Light Blue Roundel A");
        addBlockItem(t, DWMBlocks.LIGHT_GRAY_ROUNDEL_A, "Light Gray Roundel A");
        addBlockItem(t, DWMBlocks.LIME_ROUNDEL_A, "Lime Roundel A");
        addBlockItem(t, DWMBlocks.MAGENTA_ROUNDEL_A, "Magenta Roundel A");
        addBlockItem(t, DWMBlocks.ORANGE_ROUNDEL_A, "Orange Roundel A");
        addBlockItem(t, DWMBlocks.PINK_ROUNDEL_A, "Pink Roundel A");
        addBlockItem(t, DWMBlocks.RED_ROUNDEL_A, "Red Roundel A");
        addBlockItem(t, DWMBlocks.WHITE_ROUNDEL_A, "White Roundel A");
        addBlockItem(t, DWMBlocks.YELLOW_ROUNDEL_A, "Yellow Roundel A");
        addBlockItem(t, DWMBlocks.GRAY_ROUNDEL_A, "Gray Roundel A");
        addBlockItem(t, DWMBlocks.PURPLE_ROUNDEL_A, "Purple Roundel A");
        addBlockItem(t, DWMBlocks.TEAL_ROUNDEL_A, "Teal Roundel A");

        addBlockItem(t, DWMBlocks.BLACK_ROUNDEL_B, "Black Roundel B");
        addBlockItem(t, DWMBlocks.BLUE_ROUNDEL_B, "Blue Roundel B");
        addBlockItem(t, DWMBlocks.BROWN_ROUNDEL_B, "Brown Roundel B");
        addBlockItem(t, DWMBlocks.CYAN_ROUNDEL_B, "Cyan Roundel B");
        addBlockItem(t, DWMBlocks.GREEN_ROUNDEL_B, "Green Roundel B");
        addBlockItem(t, DWMBlocks.LIGHT_BLUE_ROUNDEL_B, "Light Blue Roundel B");
        addBlockItem(t, DWMBlocks.LIGHT_GRAY_ROUNDEL_B, "Light Gray Roundel B");
        addBlockItem(t, DWMBlocks.LIME_ROUNDEL_B, "Lime Roundel B");
        addBlockItem(t, DWMBlocks.MAGENTA_ROUNDEL_B, "Magenta Roundel B");
        addBlockItem(t, DWMBlocks.ORANGE_ROUNDEL_B, "Orange Roundel B");
        addBlockItem(t, DWMBlocks.PINK_ROUNDEL_B, "Pink Roundel B");
        addBlockItem(t, DWMBlocks.RED_ROUNDEL_B, "Red Roundel B");
        addBlockItem(t, DWMBlocks.WHITE_ROUNDEL_B, "White Roundel B");
        addBlockItem(t, DWMBlocks.YELLOW_ROUNDEL_B, "Yellow Roundel B");
        addBlockItem(t, DWMBlocks.GRAY_ROUNDEL_B, "Gray Roundel B");
        addBlockItem(t, DWMBlocks.PURPLE_ROUNDEL_B, "Purple Roundel B");
        addBlockItem(t, DWMBlocks.TEAL_ROUNDEL_B, "Teal Roundel B");

        addBlockItem(t, DWMBlocks.BLACK_BIG_ROUNDEL_A, "Black Big Roundel A");
        addBlockItem(t, DWMBlocks.BLUE_BIG_ROUNDEL_A, "Blue Big Roundel A");
        addBlockItem(t, DWMBlocks.BROWN_BIG_ROUNDEL_A, "Brown Big Roundel A");
        addBlockItem(t, DWMBlocks.CYAN_BIG_ROUNDEL_A, "Cyan Big Roundel A");
        addBlockItem(t, DWMBlocks.GREEN_BIG_ROUNDEL_A, "Green Big Roundel A");
        addBlockItem(t, DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_A, "Light Blue Big Roundel A");
        addBlockItem(t, DWMBlocks.LIGHT_GRAY_BIG_ROUNDEL_A, "Light Gray Big Roundel A");
        addBlockItem(t, DWMBlocks.LIME_BIG_ROUNDEL_A, "Lime Big Roundel A");
        addBlockItem(t, DWMBlocks.MAGENTA_BIG_ROUNDEL_A, "Magenta Big Roundel A");
        addBlockItem(t, DWMBlocks.ORANGE_BIG_ROUNDEL_A, "Orange Big Roundel A");
        addBlockItem(t, DWMBlocks.PINK_BIG_ROUNDEL_A, "Pink Big Roundel A");
        addBlockItem(t, DWMBlocks.RED_BIG_ROUNDEL_A, "Red Big Roundel A");
        addBlockItem(t, DWMBlocks.WHITE_BIG_ROUNDEL_A, "White Big Roundel A");
        addBlockItem(t, DWMBlocks.YELLOW_BIG_ROUNDEL_A, "Yellow Big Roundel A");
        addBlockItem(t, DWMBlocks.GRAY_BIG_ROUNDEL_A, "Gray Big Roundel A");
        addBlockItem(t, DWMBlocks.PURPLE_BIG_ROUNDEL_A, "Purple Big Roundel A");
        addBlockItem(t, DWMBlocks.TEAL_BIG_ROUNDEL_A, "Teal Big Roundel A");

        addBlockItem(t, DWMBlocks.BLACK_BIG_ROUNDEL_B, "Black Big Roundel B");
        addBlockItem(t, DWMBlocks.BLUE_BIG_ROUNDEL_B, "Blue Big Roundel B");
        addBlockItem(t, DWMBlocks.BROWN_BIG_ROUNDEL_B, "Brown Big Roundel B");
        addBlockItem(t, DWMBlocks.CYAN_BIG_ROUNDEL_B, "Cyan Big Roundel B");
        addBlockItem(t, DWMBlocks.GREEN_BIG_ROUNDEL_B, "Green Big Roundel B");
        addBlockItem(t, DWMBlocks.LIGHT_BLUE_BIG_ROUNDEL_B, "Light Blue Big Roundel B");
        addBlockItem(t, DWMBlocks.LIGHT_GRAY_BIG_ROUNDEL_B, "Light Gray Big Roundel B");
        addBlockItem(t, DWMBlocks.LIME_BIG_ROUNDEL_B, "Lime Big Roundel B");
        addBlockItem(t, DWMBlocks.MAGENTA_BIG_ROUNDEL_B, "Magenta Big Roundel B");
        addBlockItem(t, DWMBlocks.ORANGE_BIG_ROUNDEL_B, "Orange Big Roundel B");
        addBlockItem(t, DWMBlocks.PINK_BIG_ROUNDEL_B, "Pink Big Roundel B");
        addBlockItem(t, DWMBlocks.RED_BIG_ROUNDEL_B, "Red Big Roundel B");
        addBlockItem(t, DWMBlocks.WHITE_BIG_ROUNDEL_B, "White Big Roundel B");
        addBlockItem(t, DWMBlocks.YELLOW_BIG_ROUNDEL_B, "Yellow Big Roundel B");
        addBlockItem(t, DWMBlocks.GRAY_BIG_ROUNDEL_B, "Gray Big Roundel B");
        addBlockItem(t, DWMBlocks.PURPLE_BIG_ROUNDEL_B, "Purple Big Roundel B");
        addBlockItem(t, DWMBlocks.TEAL_BIG_ROUNDEL_B, "Teal Big Roundel B");

        addBlockItem(t, DWMBlocks.TARDIS_BLOCK, "TARDIS");
        addBlockItem(t, DWMBlocks.TARDIS_INTERIOR_DOOR, "TARDIS Interior Door");
        addBlockItem(t, DWMBlocks.FIRST_DOCTOR_CONSOLE, "First Doctor Console");
        addBlockItem(t, DWMBlocks.TARDIS_DOOR_BUTTON, "TARDIS Door Button");

        addBlockAndItem(t, DWMBlocks.TARDIS_CHAIR_SMALL, "Small TARDIS Chair");
        addBlockAndItem(t, DWMBlocks.TARDIS_CHAIR_LARGE, "Large TARDIS Chair");
        addBlockAndItem(t, DWMBlocks.DECORATIONAL_COLUMN, "Decorational Column");
        addBlockAndItem(t, DWMBlocks.TARDIS_CEILING_VENT, "TARDIS Ceiling Vent");
        addBlockAndItem(t, DWMBlocks.TARDIS_GLOBE, "TARDIS Globe");
        addBlockAndItem(t, DWMBlocks.TARDIS_COMPACT_SCANNER, "Compact TARDIS Scanner");
        addBlockAndItem(t, DWMBlocks.TARDIS_FULL_SCANNER, "Full TARDIS Scanner");
    }

    private static void addGallifreyStoneFamily(TranslationBuilder t) {
        addBlockAndItem(t, DWMBlocks.GALLIFREY_STONE, "Gallifrey Stone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_STONE_BRICKS, "Gallifrey Stone Bricks");
        addBlockAndItem(t, DWMBlocks.CHISELED_GALLIFREY_STONE_BRICKS, "Chiseled Gallifrey Stone Bricks");
        addBlockAndItem(t, DWMBlocks.CRACKED_GALLIFREY_STONE_BRICKS, "Cracked Gallifrey Stone Bricks");
        addBlockAndItem(t, DWMBlocks.MOSSY_GALLIFREY_STONE_BRICKS, "Mossy Gallifrey Stone Bricks");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_COBBLESTONE, "Gallifrey Cobblestone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_MOSSY_COBBLESTONE, "Mossy Gallifrey Cobblestone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_SMOOTH_STONE, "Gallifrey Smooth Stone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_SANDSTONE, "Gallifrey Sandstone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_CUT_SANDSTONE, "Gallifrey Cut Sandstone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_CHISELED_SANDSTONE, "Chiseled Gallifrey Sandstone");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_SAND, "Gallifrey Sand");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_DIRT, "Gallifrey Dirt");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_COARSE_DIRT, "Gallifrey Coarse Dirt");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_GRASS_BLOCK, "Gallifrey Grass");
    }

    private static void addOrangeSandFamily(TranslationBuilder t) {
        addBlockAndItem(t, DWMBlocks.ORANGE_SAND, "Orange Sand");
        addBlockAndItem(t, DWMBlocks.ORANGE_SANDSTONE, "Orange Sandstone");
        addBlockAndItem(t, DWMBlocks.ORANGE_SANDSTONE_STAIRS, "Orange Sandstone Stairs");
        addBlockAndItem(t, DWMBlocks.ORANGE_SANDSTONE_SLAB, "Orange Sandstone Slab");
        addBlockAndItem(t, DWMBlocks.ORANGE_SANDSTONE_WALL, "Orange Sandstone Wall");
        addBlockAndItem(t, DWMBlocks.CUT_ORANGE_SANDSTONE, "Cut Orange Sandstone");
        addBlockAndItem(t, DWMBlocks.CUT_ORANGE_SANDSTONE_SLAB, "Cut Orange Sandstone Slab");
        addBlockAndItem(t, DWMBlocks.CHISELED_ORANGE_SANDSTONE, "Chiseled Orange Sandstone");
        addBlockAndItem(t, DWMBlocks.SMOOTH_ORANGE_SANDSTONE, "Smooth Orange Sandstone");
        addBlockAndItem(t, DWMBlocks.SMOOTH_ORANGE_SANDSTONE_STAIRS, "Smooth Orange Sandstone Stairs");
        addBlockAndItem(t, DWMBlocks.SMOOTH_ORANGE_SANDSTONE_SLAB, "Smooth Orange Sandstone Slab");
    }

    private static void addCitadelFamily(TranslationBuilder t) {
        addBlockAndItem(t, DWMBlocks.CITADEL_WALL, "Citadel Wall");
        addBlockAndItem(t, DWMBlocks.CITADEL_PANEL, "Citadel Panel");
        addBlockAndItem(t, DWMBlocks.CITADEL_TILE, "Citadel Tile");
        addBlockAndItem(t, DWMBlocks.CITADEL_GLASS, "Citadel Glass");
    }

    private static void addGallifreyPlants(TranslationBuilder t) {
        addBlockAndItem(t, DWMBlocks.FLOWER_OF_REMEMBRANCE, "Flower of Remembrance");
        t.add(DWMBlocks.POTTED_FLOWER_OF_REMEMBRANCE, "Potted Flower of Remembrance");
        addBlockAndItem(t, DWMBlocks.MOONLIGHT_BLOOM, "Moonlight Bloom");
        t.add(DWMBlocks.POTTED_MOONLIGHT_BLOOM, "Potted Moonlight Bloom");
        addBlockAndItem(t, DWMBlocks.SACCHARINE_CANE, "Saccharine Cane");
    }

    private static void addAzbantiumSet(TranslationBuilder t) {
        addBlockAndItem(t, DWMBlocks.AZBANTIUM_ORE, "Azbantium Ore");
        addBlockAndItem(t, DWMBlocks.AZBANTIUM_BLOCK, "Azbantium Block");
        addItem(t, DWMItems.AZBANTIUM, "Azbantium");
        addItem(t, DWMItems.AZBANTIUM_SWORD, "Azbantium Sword");
        addItem(t, DWMItems.AZBANTIUM_SHOVEL, "Azbantium Shovel");
        addItem(t, DWMItems.AZBANTIUM_PICKAXE, "Azbantium Pickaxe");
        addItem(t, DWMItems.AZBANTIUM_AXE, "Azbantium Axe");
        addItem(t, DWMItems.AZBANTIUM_HOE, "Azbantium Hoe");
        addItem(t, DWMItems.AZBANTIUM_HELMET, "Azbantium Helmet");
        addItem(t, DWMItems.AZBANTIUM_CHESTPLATE, "Azbantium Chestplate");
        addItem(t, DWMItems.AZBANTIUM_LEGGINGS, "Azbantium Leggings");
        addItem(t, DWMItems.AZBANTIUM_BOOTS, "Azbantium Boots");
    }

    private static void addGallifreyVanillaOres(TranslationBuilder t) {
        addBlockAndItem(t, DWMBlocks.GALLIFREY_COAL_ORE, "Gallifrey Coal Ore");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_IRON_ORE, "Gallifrey Iron Ore");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_GOLD_ORE, "Gallifrey Gold Ore");
        addBlockAndItem(t, DWMBlocks.GALLIFREY_DIAMOND_ORE, "Gallifrey Diamond Ore");
    }

    private static void addMisc(TranslationBuilder t) {
        t.add(DWMEntityTypes.TARDIS_SEAT, "TARDIS Seat");
        t.add(DWMEntityTypes.CONSOLE_CONTROL, "Console Control");
        t.add(DWMEntityTypes.BROAKIR, "Broakir");
        addItem(t, DWMItems.BROAKIR_SPAWN_EGG, "Broakir Spawn Egg");
        t.add(DWMEntityTypes.FLUTTERWING, "Flutterwing");
        addItem(t, DWMItems.FLUTTERWING_SPAWN_EGG, "Flutterwing Spawn Egg");
        t.add("dimension.dwm.gallifrey", "Gallifrey");
        t.add("biome.dwm.gallifrey_plains", "Gallifrey Plains");
        t.add("biome.dwm.gallifrey_forest", "Gallifrey Forest");
        t.add("tag.item.dwm.gallifrey_plants", "Gallifrey Plants");
        t.add("tag.item.dwm.azbantium_ores", "Azbantium Ores");
        t.add("tag.item.dwm.repairs_azbantium_equipment", "Azbantium Equipment Repair Materials");
        t.add("biome.dwm.gallifrey_wastes", "Gallifrey Wastes");
        t.add("biome.dwm.gallifrey_badlands", "Gallifrey Badlands");
        t.add("dwm.console.biome_selector", "Biome selector");
        t.add("dwm.console.biome_selected", "Biome: %s");
        t.add("dwm.console.biome_unavailable", "No biomes available for this dimension");
        t.add("dwm.console.waypoint_selector", "Waypoint selector");
        t.add("dwm.console.player_locator", "Player locator");
        t.add("dwm.console.planet_locator", "Planet locator");
        t.add("dwm.console.dimension_selected", "Dimension: %s");
        t.add("dwm.console.dimension_unavailable", "No dimensions available");
        t.add("dwm.console.chameleon_circuit", "Chameleon circuit");
        t.add("dwm.console.materialisation_lever", "Materialisation lever");
        t.add("dwm.console.fast_return", "Fast return");
        t.add("dwm.console.fast_return_selected", "Fast return (%s/%s): %s %s, %s, %s");
        t.add("dwm.console.fast_return_empty", "No previous location");
        t.add("dwm.console.fast_return_unavailable", "Fast return unavailable");
        t.add("dwm.console.stabilisers", "Stabilisers");
        t.add("dwm.console.stabilisers_on", "Stabilisers: On");
        t.add("dwm.console.stabilisers_off", "Stabilisers: Off");
        t.add("dwm.console.stabilisers_unavailable", "Stabilisers unavailable");
        t.add("dwm.console.materialisation_lever_pulled", "Materialisation lever engaged");
        t.add("dwm.console.travel_dematerialising", "Dematerialising...");
        t.add("dwm.console.travel_materialising", "Materialising...");
        t.add("dwm.console.travel_unavailable", "Unable to travel — set a destination and ensure the exterior is linked");
        t.add("dwm.console.travel_in_flight", "TARDIS is already in flight");
        t.add("dwm.console.travel_in_progress", "Travel sequence already in progress");
        t.add("dwm.console.travel_player_offline", "Selected player is offline — cannot materialise");
        t.add("dwm.console.travel_invalid_landing", "No valid landing site nearby — stay in flight");
        t.add("dwm.console.waypoint_unavailable", "Waypoint selector unavailable");
        t.add("dwm.console.waypoint_saved", "Waypoint saved: %s");
        t.add("dwm.console.waypoint_save_failed", "Unable to save waypoint");
        t.add("dwm.console.waypoint_deleted", "Waypoint deleted");
        t.add("dwm.console.waypoint_delete_failed", "Unable to delete waypoint");
        t.add("dwm.console.waypoint_selected", "Waypoint set as destination");
        t.add("dwm.console.waypoint_cleared", "Destination cleared");
        t.add("dwm.console.waypoint_select_failed", "Unable to select waypoint");
        t.add("dwm.console.waypoint_renamed", "Waypoint renamed: %s");
        t.add("dwm.console.waypoint_rename_failed", "Unable to rename waypoint");
        t.add("dwm.console.player_locator_unavailable", "Player locator unavailable");
        t.add("dwm.console.player_locator_selected", "Player set as destination");
        t.add("dwm.console.player_locator_select_failed", "Unable to select player");
        t.add("dwm.console.player_locator_offline", "That player is no longer online");
        t.add("dwm.console.chameleon_unavailable", "Chameleon circuit unavailable");
        t.add("dwm.console.chameleon_selected", "Chameleon: %s");
        t.add("dwm.console.oxygen", "Oxygen: %s%%");
        t.add("dwm.console.pressure", "Pressure: %s%%");
        t.add("dwm.console.temperature", "Temperature: %s%%");
        t.add("dwm.console.radiation", "Radiation: %s%%");
        t.add("dwm.console.reader_no_signal", "No exterior signal");
        t.add("dwm.console.reader_unavailable", "Reader unavailable");
        t.add("dwm.console.refueler_stable", "Artron reserves: stable");
        t.add("dwm.console.refueler_unavailable", "Refueler unavailable");
        t.add("dwm.console.telepathic_circuit", "Telepathic circuit");
        t.add("dwm.console.telepathic_home", "Telepathic circuit locked onto your home");
        t.add("dwm.console.telepathic_spawn", "Telepathic circuit locked onto world spawn");
        t.add("dwm.console.telepathic_unavailable", "Telepathic circuit unavailable");
        t.add("dwm.console.cloak_on", "Cloak engaged");
        t.add("dwm.console.cloak_off", "Cloak disengaged");
        t.add("dwm.console.cloak_unavailable", "Cloak unavailable");
        t.add("dwm.console.doors_locked", "Doors locked");
        t.add("dwm.console.doors_unlocked", "Doors unlocked");
        t.add("dwm.console.doors_are_locked", "Doors are locked");
        t.add("dwm.console.doors_must_be_closed", "Doors must be closed");
        t.add("dwm.console.door_lock_unavailable", "Door lock unavailable");
        t.add("dwm.console.lock_x_on", "X axis locked");
        t.add("dwm.console.lock_x_off", "X axis unlocked");
        t.add("dwm.console.lock_y_on", "Y axis locked");
        t.add("dwm.console.lock_y_off", "Y axis unlocked");
        t.add("dwm.console.lock_z_on", "Z axis locked");
        t.add("dwm.console.lock_z_off", "Z axis unlocked");
        t.add("dwm.console.coordinate_lock_unavailable", "Coordinate lock unavailable");
        t.add("dwm.command.tardis.rebuild.success", "TARDIS interior rebuilt (%s)");
        t.add("dwm.command.tardis.rebuild.no_owned", "You do not own a TARDIS — enter an unowned TARDIS to claim one");
        t.add("dwm.command.tardis.rebuild.unknown", "Unknown TARDIS: %s");
        t.add("dwm.command.tardis.rebuild.in_flight", "Cannot rebuild interior while the TARDIS is in flight");
        t.add("dwm.command.tardis.rebuild.failed", "Failed to rebuild TARDIS interior");
        t.add("dwm.command.tardis.claim.success", "Claimed TARDIS (%s)");
        t.add("dwm.command.tardis.claim.already_owner", "You already own this TARDIS (%s)");
        t.add("dwm.command.tardis.claim.already_owns_another", "You already own a TARDIS");
        t.add("dwm.command.tardis.claim.unknown", "Unknown TARDIS: %s");
        t.add("dwm.command.tardis.claim.not_inside", "Stand inside a TARDIS interior to claim it, or pass a UUID");
        t.add("dwm.gui.waypoint.title", "Waypoint Selector");
        t.add("dwm.gui.waypoint.save", "Save");
        t.add("dwm.gui.waypoint.rename", "Rename");
        t.add("dwm.gui.waypoint.rename.title", "Rename waypoint");
        t.add("dwm.gui.waypoint.new", "New waypoint");
        t.add("dwm.gui.waypoint.new.ghost", "+ New…");
        t.add("dwm.gui.waypoint.select", "Select");
        t.add("dwm.gui.waypoint.clear", "Clear destination");
        t.add("dwm.gui.waypoint.delete", "Delete");
        t.add("dwm.gui.waypoint.delete.confirm.title", "Delete waypoint?");
        t.add("dwm.gui.waypoint.delete.confirm.message", "Are you sure you want to delete \"%s\"?");
        t.add("dwm.gui.waypoint.edit", "Edit");
        t.add("dwm.gui.waypoint.name", "Waypoint name");
        t.add("dwm.gui.waypoint.confirm", "Confirm");
        t.add("dwm.gui.waypoint.detail.name", "Name");
        t.add("dwm.gui.waypoint.detail.location", "Location");
        t.add("dwm.gui.waypoint.empty", "No waypoints saved");
        t.add("dwm.gui.waypoint.tooltip.destination", "Selected destination");
        t.add("dwm.gui.waypoint.tooltip.at_location", "Current location");
        t.add("dwm.gui.waypoint.tooltip.new", "Save current location as a waypoint");
        t.add("dwm.gui.waypoint.tooltip.new_exists", "A waypoint already exists at this location");
        t.add("dwm.gui.waypoint.tooltip.edit", "Rename waypoint");
        t.add("dwm.gui.waypoint.tooltip.delete", "Delete waypoint");
        t.add("dwm.gui.waypoint.tooltip.select", "Set as destination");
        t.add("dwm.gui.waypoint.tooltip.clear", "Clear destination");
        t.add("dwm.gui.player_locator.title", "Player Locator");
        t.add("dwm.gui.player_locator.select", "Select");
        t.add("dwm.gui.player_locator.clear", "Clear destination");
        t.add("dwm.gui.player_locator.empty", "No other players online");
        t.add("dwm.gui.player_locator.detail.name", "Name");
        t.add("dwm.gui.player_locator.detail.location", "Location");
        t.add("dwm.gui.player_locator.tooltip.select", "Set as destination");
        t.add("dwm.gui.player_locator.tooltip.clear", "Clear destination");
        t.add("dwm.gui.player_locator.tooltip.destination", "Selected destination");
        t.add("dwm.sonic_screwdriver.subtitle", "Whirring");
        t.add("dwm.tardis_door_close.subtitle", "TARDIS Door Closing");
        t.add("dwm.tardis_door_open.subtitle", "TARDIS Door Opening");
        t.add("dwm.tardis_hum.subtitle", "TARDIS humming");
        t.add("dwm.tardis_dematerialise_loop.subtitle", "TARDIS dematerialising");
        t.add("dwm.tardis_materialise_loop.subtitle", "TARDIS materialising");
        t.add("dwm.tardis_flight_loop.subtitle", "TARDIS in flight");
        t.add("dwm.tardis_materialise_thud.subtitle", "TARDIS landing");
        t.add("dwm.entity.broakir.ambient.subtitle", "Broakir squeals");
        t.add("dwm.entity.broakir.hurt.subtitle", "Broakir hurts");
        t.add("dwm.entity.broakir.death.subtitle", "Broakir dies");
        t.add("dwm.entity.flutterwing.ambient.subtitle", "Flutterwing flutters");
        t.add("dwm.entity.flutterwing.hurt.subtitle", "Flutterwing hurts");
        t.add("dwm.entity.flutterwing.death.subtitle", "Flutterwing dies");
        t.add("config.dwm.title", "Doctor Who Mod");
        t.add("stat.dwm.sonic_screwdriver_use", "Uses of Sonic Screwdriver");
        t.add("advancements.dwm.sonic_screwdriver", "Sonic Screwdriver");
        t.add("advancements.dwm.sonic_screwdriver.description", "Obtain a Sonic Screwdriver");
        t.add("dwm.tt_capsule", "TT Capsule");
        t.add("dwm.first_doctor_box", "First Doctor Box");
        t.add("dwm.second_doctor_box", "Second Doctor Box");
        t.add("dwm.third_doctor_box", "Third Doctor Box");
        t.add("dwm.fourth_doctor_box", "Fourth Doctor Box");
        t.add("dwm.fifth_doctor_box", "Fifth Doctor Box");
        t.add("dwm.sixth_doctor_box", "Sixth Doctor Box");
        t.add("dwm.seventh_doctor_box", "Seventh Doctor Box");
        t.add("dwm.gui.no_more_variants", "No more variants");
        t.add("dwm.config.option.chameleon_gui", "Enable Chameleon GUI");
        t.add("dwm.config.option.enable_door_portals", "Door portals (BOTI / SOTO)");
        t.add("dwm.config.option.enable_door_portals.tooltip", "Show doorway previews through open exterior and interior doors using the shared portal renderer. Disabled on Fabulous graphics or when order-independent transparency is on.");
        t.add("dwm.config.option.show_portal_perf_debug", "Portal performance debug HUD");
        t.add("dwm.config.option.show_portal_perf_debug.tooltip", "Show an F3-style on-screen overlay with shared portal pipeline stage timings (BOTI and SOTO). Enable only while profiling.");
        t.add("dwm.config.category.experimental", "Experimental");
    }

    private static void addItem(TranslationBuilder t, Item item, String name) {
        t.add(item, name);
    }

    private static void addBlockItem(TranslationBuilder t, Block block, String name) {
        t.add(block.asItem(), name);
    }

    private static void addBlockAndItem(TranslationBuilder t, Block block, String name) {
        t.add(block, name);
        t.add(block.asItem(), name);
    }
}
