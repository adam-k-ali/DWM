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

    private DWMBlockTags() {
    }
}
