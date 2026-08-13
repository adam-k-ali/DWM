package com.adamkali.dwm.block;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class DWMBlockTags {
    public static final TagKey<Block> GALLIFREY_STONE = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_stone")
    );

    public static final TagKey<Block> ORANGE_SAND = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "orange_sand")
    );

    public static final TagKey<Block> CITADEL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "citadel")
    );

    public static final TagKey<Block> GALLIFREY_PLANTS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_plants")
    );

    /** Gallifrey stone only — ore replacement target (excludes dirt/sand/grass family members). */
    public static final TagKey<Block> GALLIFREY_ORE_REPLACEABLES = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "gallifrey_ore_replaceables")
    );

    public static final TagKey<Block> AZBANTIUM_ORES = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "azbantium_ores")
    );

    /**
     * Vanilla still ships {@code data/minecraft/tags/block/coal_ores.json}, but
     * {@link net.minecraft.tags.BlockTags} no longer exposes a constant in 26.2.
     */
    public static final TagKey<Block> COAL_ORES = TagKey.create(
            Registries.BLOCK,
            Identifier.withDefaultNamespace("coal_ores")
    );

    /**
     * Vanilla still ships {@code data/minecraft/tags/block/diamond_ores.json}, but
     * {@link net.minecraft.tags.BlockTags} no longer exposes a constant in 26.2.
     */
    public static final TagKey<Block> DIAMOND_ORES = TagKey.create(
            Registries.BLOCK,
            Identifier.withDefaultNamespace("diamond_ores")
    );

    public static final TagKey<Block> ASH_LOGS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash_logs")
    );

    public static final TagKey<Block> DARK_ASH_LOGS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash_logs")
    );

    public static final TagKey<Block> CARDINAL_LOGS = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal_logs")
    );

    /**
     * Vanilla still ships {@code data/minecraft/tags/block/logs_that_burn.json}, but
     * {@link net.minecraft.tags.BlockTags} no longer exposes a constant in 26.2.
     */
    public static final TagKey<Block> LOGS_THAT_BURN = TagKey.create(
            Registries.BLOCK,
            Identifier.withDefaultNamespace("logs_that_burn")
    );

    /**
     * Vanilla still ships {@code data/minecraft/tags/block/saplings.json}, but
     * {@link net.minecraft.tags.BlockTags} no longer exposes a constant in 26.2.
     */
    public static final TagKey<Block> SAPLINGS = TagKey.create(
            Registries.BLOCK,
            Identifier.withDefaultNamespace("saplings")
    );

    private DWMBlockTags() {
    }
}
