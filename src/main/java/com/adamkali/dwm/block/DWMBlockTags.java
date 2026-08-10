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

    public static final TagKey<Block> CITADEL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "citadel")
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

    private DWMBlockTags() {
    }
}
