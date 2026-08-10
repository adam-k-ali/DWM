package com.adamkali.dwm.block.wood;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class RegisteredWoodFamily {
    private final WoodFamilyDefinition definition;
    private final WoodFamilyBlocks blocks;
    private Item signItem;
    private Item hangingSignItem;
    private Item boatItem;
    private EntityType<Boat> boatEntity;

    public RegisteredWoodFamily(WoodFamilyDefinition definition, WoodFamilyBlocks blocks) {
        this.definition = definition;
        this.blocks = blocks;
    }

    public WoodFamilyDefinition definition() {
        return definition;
    }

    public WoodFamilyBlocks blocks() {
        return blocks;
    }

    public Item signItem() {
        return Objects.requireNonNull(signItem, "sign item not registered for " + definition.id());
    }

    public Item hangingSignItem() {
        return Objects.requireNonNull(hangingSignItem, "hanging sign item not registered for " + definition.id());
    }

    public Item boatItem() {
        return Objects.requireNonNull(boatItem, "boat item not registered for " + definition.id());
    }

    public EntityType<Boat> boatEntity() {
        return Objects.requireNonNull(boatEntity, "boat entity not registered for " + definition.id());
    }

    void setItems(Item signItem, Item hangingSignItem, Item boatItem) {
        this.signItem = signItem;
        this.hangingSignItem = hangingSignItem;
        this.boatItem = boatItem;
    }

    void setBoatEntity(EntityType<Boat> boatEntity) {
        this.boatEntity = boatEntity;
    }

    public boolean has(WoodFamilyFeature feature) {
        return definition.has(feature);
    }

    /** True when either a vanilla or tall door was registered for this family. */
    public boolean hasDoor() {
        return has(WoodFamilyFeature.DOOR) || has(WoodFamilyFeature.TALL_DOOR);
    }

    public Block requireDoor() {
        return Objects.requireNonNull(blocks.door(), "door not registered for " + definition.id());
    }

    public Block requireTrapdoor() {
        return Objects.requireNonNull(blocks.trapdoor(), "trapdoor not registered for " + definition.id());
    }

    @Nullable
    public Block doorOrNull() {
        return blocks.door();
    }

    @Nullable
    public Block trapdoorOrNull() {
        return blocks.trapdoor();
    }

    public List<Block> logs() {
        return List.of(blocks.log(), blocks.wood(), blocks.strippedLog(), blocks.strippedWood());
    }

    public List<Block> buildingBlocks() {
        List<Block> list = new ArrayList<>();
        list.add(blocks.planks());
        list.add(blocks.log());
        list.add(blocks.wood());
        list.add(blocks.strippedLog());
        list.add(blocks.strippedWood());
        list.add(blocks.stairs());
        list.add(blocks.slab());
        list.add(blocks.fence());
        list.add(blocks.fenceGate());
        list.add(blocks.button());
        list.add(blocks.pressurePlate());
        if (blocks.door() != null) {
            list.add(blocks.door());
        }
        if (blocks.trapdoor() != null) {
            list.add(blocks.trapdoor());
        }
        return List.copyOf(list);
    }

    public List<Block> familyBlocks() {
        List<Block> list = new ArrayList<>();
        list.add(blocks.planks());
        list.add(blocks.log());
        list.add(blocks.wood());
        list.add(blocks.strippedLog());
        list.add(blocks.strippedWood());
        list.add(blocks.leaves());
        list.add(blocks.sapling());
        list.add(blocks.pottedSapling());
        list.add(blocks.stairs());
        list.add(blocks.slab());
        list.add(blocks.fence());
        list.add(blocks.fenceGate());
        list.add(blocks.button());
        list.add(blocks.pressurePlate());
        if (blocks.door() != null) {
            list.add(blocks.door());
        }
        if (blocks.trapdoor() != null) {
            list.add(blocks.trapdoor());
        }
        list.add(blocks.sign());
        list.add(blocks.wallSign());
        list.add(blocks.hangingSign());
        list.add(blocks.wallHangingSign());
        return List.copyOf(list);
    }

    public List<Block> axeMineableBlocks() {
        List<Block> list = new ArrayList<>(buildingBlocks());
        list.add(blocks.sign());
        list.add(blocks.wallSign());
        list.add(blocks.hangingSign());
        list.add(blocks.wallHangingSign());
        return List.copyOf(list);
    }

    public BlockFamily vanillaModelFamily() {
        BlockFamily.Builder builder = new BlockFamily.Builder(blocks.planks())
                .stairs(blocks.stairs())
                .slab(blocks.slab())
                .fence(blocks.fence())
                .fenceGate(blocks.fenceGate())
                .button(blocks.button())
                .pressurePlate(blocks.pressurePlate())
                .sign(blocks.sign(), blocks.wallSign());
        if (blocks.door() != null && !has(WoodFamilyFeature.CUSTOM_DOOR_MODEL)) {
            builder.door(blocks.door());
        }
        if (blocks.trapdoor() != null && !has(WoodFamilyFeature.CUSTOM_TRAPDOOR_MODEL)) {
            builder.trapdoor(blocks.trapdoor());
        }
        return builder.recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks").getFamily();
    }
}
