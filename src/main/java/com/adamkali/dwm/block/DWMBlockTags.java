package com.adamkali.dwm.block;

import com.adamkali.dwm.DWMReference;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class DWMBlockTags {
    public static final TagKey<Block> GALLIFREY_STONE = TagKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(DWMReference.MOD_ID, "gallifrey_stone")
    );

    public static final TagKey<Block> CITADEL = TagKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(DWMReference.MOD_ID, "citadel")
    );

    public static final TagKey<Block> ASH_LOGS = TagKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(DWMReference.MOD_ID, "ash_logs")
    );

    public static final TagKey<Block> DARK_ASH_LOGS = TagKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(DWMReference.MOD_ID, "dark_ash_logs")
    );

    public static final TagKey<Block> CARDINAL_LOGS = TagKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(DWMReference.MOD_ID, "cardinal_logs")
    );

    private DWMBlockTags() {
    }
}
