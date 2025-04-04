package com.adamkali.dwm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;

public class DWMBlockSettings {
    public static final AbstractBlock.Settings TARDIS_WALL_SETTINGS = AbstractBlock.Settings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.METAL);
    public static final AbstractBlock.Settings CHRONOPLASM_POWDER_SETTINGS = AbstractBlock.Settings.create().strength(0.5F).sounds(BlockSoundGroup.SAND);
}
