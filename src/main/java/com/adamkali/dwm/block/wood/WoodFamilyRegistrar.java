package com.adamkali.dwm.block.wood;

import com.adamkali.dwm.item.DWMCreativeTabs;

import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.item.DWMItems;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class WoodFamilyRegistrar {
    private WoodFamilyRegistrar() {
    }

    public static RegisteredWoodFamily registerBlocks(WoodFamilyDefinition definition) {
        String id = definition.id();
        WoodFamilySettings settings = WoodFamilySettings.of(definition.planksColor());

        Block planks = DWMBlocks.registerBlock(Block::new, settings.planks(), id + "_planks");
        Block log = DWMBlocks.registerBlock(
                RotatedPillarBlock::new,
                settings.log(definition.planksColor(), definition.barkColor()),
                id + "_log"
        );
        Block wood = DWMBlocks.registerBlock(
                RotatedPillarBlock::new,
                settings.log(definition.barkColor(), definition.barkColor()),
                id + "_wood"
        );
        Block strippedLog = DWMBlocks.registerBlock(
                RotatedPillarBlock::new,
                settings.log(definition.planksColor(), definition.planksColor()),
                "stripped_" + id + "_log"
        );
        Block strippedWood = DWMBlocks.registerBlock(
                RotatedPillarBlock::new,
                settings.log(definition.planksColor(), definition.planksColor()),
                "stripped_" + id + "_wood"
        );
        Block leaves = DWMBlocks.registerBlock(
                props -> new UntintedParticleLeavesBlock(0.01F, ParticleTypes.CHERRY_LEAVES, props),
                settings.leaves(),
                id + "_leaves"
        );
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
                s -> new StairBlock(planks.defaultBlockState(), s),
                BlockBehaviour.Properties.ofLegacyCopy(planks),
                id + "_stairs"
        );
        Block slab = DWMBlocks.registerBlock(SlabBlock::new, BlockBehaviour.Properties.ofLegacyCopy(planks), id + "_slab");
        Block fence = DWMBlocks.registerBlock(FenceBlock::new, BlockBehaviour.Properties.ofLegacyCopy(planks), id + "_fence");
        Block fenceGate = DWMBlocks.registerBlock(
                s -> new FenceGateBlock(definition.woodType(), s),
                BlockBehaviour.Properties.ofLegacyCopy(planks),
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
                    s -> new TrapDoorBlock(definition.blockSetType(), s),
                    settings.trapdoor(),
                    id + "_trapdoor"
            );
        }

        Block sign = DWMBlocks.registerBlockWithoutItem(
                s -> new StandingSignBlock(definition.woodType(), s),
                settings.sign(),
                id + "_sign"
        );
        Block wallSign = DWMBlocks.registerBlockWithoutItem(
                s -> new WallSignBlock(definition.woodType(), s),
                settings.wallSign(definition.planksColor(), sign),
                id + "_wall_sign"
        );
        Block hangingSign = DWMBlocks.registerBlockWithoutItem(
                s -> new CeilingHangingSignBlock(definition.woodType(), s),
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
                new Item.Properties().stacksTo(16),
                id + "_sign"
        );
        Item hangingSign = DWMItems.register(
                settings -> new HangingSignItem(blocks.hangingSign(), blocks.wallHangingSign(), settings),
                new Item.Properties().stacksTo(16),
                id + "_hanging_sign"
        );
        Item boat = DWMItems.register(
                settings -> new BoatItem(family.boatEntity(), settings),
                new Item.Properties().stacksTo(1),
                id + "_boat"
        );
        family.setItems(sign, hangingSign, boat);
    }

    /**
     * Register the boat entity before {@link #registerItems}; the item supplier is only
     * invoked when a boat spawns (after items exist).
     */
    public static EntityType<Boat> registerBoatEntity(RegisteredWoodFamily family) {
        EntityType<Boat> boatEntity = DWMEntityTypes.registerBoat(
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

        BlockEntityTypes.SIGN.addValidBlock(blocks.sign());
        BlockEntityTypes.SIGN.addValidBlock(blocks.wallSign());
        BlockEntityTypes.HANGING_SIGN.addValidBlock(blocks.hangingSign());
        BlockEntityTypes.HANGING_SIGN.addValidBlock(blocks.wallHangingSign());
    }

    public static void addCreativeTabs(RegisteredWoodFamily family) {
        WoodFamilyBlocks blocks = family.blocks();
        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.BUILDING_BLOCKS).register(content -> {
            for (Block block : family.buildingBlocks()) {
                content.accept(block);
            }
        });
        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.NATURAL_BLOCKS).register(content -> {
            content.accept(blocks.log());
            content.accept(blocks.leaves());
            content.accept(blocks.sapling());
        });
        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.REDSTONE_BLOCKS).register(content -> {
            content.accept(blocks.button());
            content.accept(blocks.pressurePlate());
            if (blocks.trapdoor() != null) {
                content.accept(blocks.trapdoor());
            }
            if (blocks.door() != null) {
                content.accept(blocks.door());
            }
        });
        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.TOOLS_AND_UTILITIES).register(content -> content.accept(family.boatItem()));
        CreativeModeTabEvents.modifyOutputEvent(DWMCreativeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.accept(family.signItem());
            content.accept(family.hangingSignItem());
        });
    }
}
