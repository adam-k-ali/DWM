package com.adamkali.dwm.block.wood;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SignItem;

public final class WoodFamilyRegistrar {
    private WoodFamilyRegistrar() {
    }

    public static RegisteredWoodFamily registerBlocks(WoodFamilyDefinition definition) {
        String id = definition.id();
        WoodFamilySettings settings = WoodFamilySettings.of(definition.planksColor());

        Block planks = DWMBlocks.registerBlock(Block::new, settings.planks(), id + "_planks");
        Block log = DWMBlocks.registerBlock(
                PillarBlock::new,
                settings.log(definition.planksColor(), definition.barkColor()),
                id + "_log"
        );
        Block wood = DWMBlocks.registerBlock(
                PillarBlock::new,
                settings.log(definition.barkColor(), definition.barkColor()),
                id + "_wood"
        );
        Block strippedLog = DWMBlocks.registerBlock(
                PillarBlock::new,
                settings.log(definition.planksColor(), definition.planksColor()),
                "stripped_" + id + "_log"
        );
        Block strippedWood = DWMBlocks.registerBlock(
                PillarBlock::new,
                settings.log(definition.planksColor(), definition.planksColor()),
                "stripped_" + id + "_wood"
        );
        Block leaves = DWMBlocks.registerBlock(LeavesBlock::new, settings.leaves(), id + "_leaves");
        Block sapling = DWMBlocks.registerBlock(
                s -> new SaplingBlock(definition.saplingGenerator(), s),
                settings.sapling(),
                id + "_sapling"
        );
        Block pottedSapling = DWMBlocks.registerBlockWithoutItem(
                s -> new FlowerPotBlock(sapling, s),
                settings.flowerPot(),
                "potted_" + id + "_sapling"
        );
        Block stairs = DWMBlocks.registerBlock(
                s -> new StairsBlock(planks.getDefaultState(), s),
                AbstractBlock.Settings.copyShallow(planks),
                id + "_stairs"
        );
        Block slab = DWMBlocks.registerBlock(SlabBlock::new, AbstractBlock.Settings.copyShallow(planks), id + "_slab");
        Block fence = DWMBlocks.registerBlock(FenceBlock::new, AbstractBlock.Settings.copyShallow(planks), id + "_fence");
        Block fenceGate = DWMBlocks.registerBlock(
                s -> new FenceGateBlock(definition.woodType(), s),
                AbstractBlock.Settings.copyShallow(planks),
                id + "_fence_gate"
        );
        Block button = DWMBlocks.registerBlock(
                s -> new ButtonBlock(definition.blockSetType(), 30, s),
                settings.button(),
                id + "_button"
        );
        Block pressurePlate = DWMBlocks.registerBlock(
                s -> new PressurePlateBlock(definition.blockSetType(), s),
                settings.pressurePlate(),
                id + "_pressure_plate"
        );

        Block door = null;
        if (definition.has(WoodFamilyFeature.TALL_DOOR)) {
            door = DWMBlocks.registerBlock(
                    s -> new TallDoorBlock(definition.blockSetType(), s),
                    settings.door(),
                    id + "_door"
            );
        } else if (definition.has(WoodFamilyFeature.DOOR)) {
            door = DWMBlocks.registerBlock(
                    s -> new DoorBlock(definition.blockSetType(), s),
                    settings.door(),
                    id + "_door"
            );
        }

        Block trapdoor = null;
        if (definition.has(WoodFamilyFeature.TRAPDOOR)) {
            trapdoor = DWMBlocks.registerBlock(
                    s -> new TrapdoorBlock(definition.blockSetType(), s),
                    settings.trapdoor(),
                    id + "_trapdoor"
            );
        }

        Block sign = DWMBlocks.registerBlockWithoutItem(
                s -> new SignBlock(definition.woodType(), s),
                settings.sign(),
                id + "_sign"
        );
        Block wallSign = DWMBlocks.registerBlockWithoutItem(
                s -> new WallSignBlock(definition.woodType(), s),
                settings.wallSign(definition.planksColor(), sign),
                id + "_wall_sign"
        );
        Block hangingSign = DWMBlocks.registerBlockWithoutItem(
                s -> new HangingSignBlock(definition.woodType(), s),
                settings.hangingSign(),
                id + "_hanging_sign"
        );
        Block wallHangingSign = DWMBlocks.registerBlockWithoutItem(
                s -> new WallHangingSignBlock(definition.woodType(), s),
                settings.wallSign(definition.planksColor(), hangingSign),
                id + "_wall_hanging_sign"
        );

        return new RegisteredWoodFamily(
                definition,
                new WoodFamilyBlocks(
                        planks, log, wood, strippedLog, strippedWood,
                        leaves, sapling, pottedSapling,
                        stairs, slab, fence, fenceGate, button, pressurePlate,
                        sign, wallSign, hangingSign, wallHangingSign,
                        door, trapdoor
                )
        );
    }

    public static void registerItems(RegisteredWoodFamily family) {
        String id = family.definition().id();
        WoodFamilyBlocks blocks = family.blocks();
        Item sign = DWMItems.register(
                settings -> new SignItem(blocks.sign(), blocks.wallSign(), settings),
                new Item.Settings().maxCount(16),
                id + "_sign"
        );
        Item hangingSign = DWMItems.register(
                settings -> new HangingSignItem(blocks.hangingSign(), blocks.wallHangingSign(), settings),
                new Item.Settings().maxCount(16),
                id + "_hanging_sign"
        );
        Item boat = DWMItems.register(
                settings -> new BoatItem(family.boatEntity(), settings),
                new Item.Settings().maxCount(1),
                id + "_boat"
        );
        family.setItems(sign, hangingSign, boat);
    }

    /**
     * Register the boat entity before {@link #registerItems}; the item supplier is only
     * invoked when a boat spawns (after items exist).
     */
    public static EntityType<BoatEntity> registerBoatEntity(RegisteredWoodFamily family) {
        EntityType<BoatEntity> boatEntity = DWMEntityTypes.registerBoat(
                family.definition().id() + "_boat",
                family::boatItem
        );
        family.setBoatEntity(boatEntity);
        return boatEntity;
    }

    public static void wireRuntime(RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();

        StrippableBlockRegistry.register(blocks.log(), blocks.strippedLog());
        StrippableBlockRegistry.register(blocks.wood(), blocks.strippedWood());

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        flammable.add(blocks.planks(), 5, 20);
        flammable.add(blocks.slab(), 5, 20);
        flammable.add(blocks.fenceGate(), 5, 20);
        flammable.add(blocks.fence(), 5, 20);
        flammable.add(blocks.stairs(), 5, 20);
        flammable.add(blocks.log(), 5, 5);
        flammable.add(blocks.strippedLog(), 5, 5);
        flammable.add(blocks.wood(), 5, 5);
        flammable.add(blocks.strippedWood(), 5, 5);
        flammable.add(blocks.leaves(), 30, 60);
        if (blocks.door() != null) {
            flammable.add(blocks.door(), 5, 20);
        }
        if (blocks.trapdoor() != null) {
            flammable.add(blocks.trapdoor(), 5, 20);
        }

        BlockEntityType.SIGN.addSupportedBlock(blocks.sign());
        BlockEntityType.SIGN.addSupportedBlock(blocks.wallSign());
        BlockEntityType.HANGING_SIGN.addSupportedBlock(blocks.hangingSign());
        BlockEntityType.HANGING_SIGN.addSupportedBlock(blocks.wallHangingSign());
    }

    public static void addCreativeTabs(RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            for (Block block : family.buildingBlocks()) {
                content.add(block);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> {
            content.add(blocks.log());
            content.add(blocks.leaves());
            content.add(blocks.sapling());
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            content.add(blocks.button());
            content.add(blocks.pressurePlate());
            if (blocks.trapdoor() != null) {
                content.add(blocks.trapdoor());
            }
            if (blocks.door() != null) {
                content.add(blocks.door());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> content.add(family.boatItem()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(family.signItem());
            content.add(family.hangingSignItem());
        });
    }
}
