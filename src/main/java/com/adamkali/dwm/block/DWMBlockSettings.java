package com.adamkali.dwm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;

public class DWMBlockSettings {
    public static final AbstractBlock.Settings TARDIS_WALL_SETTINGS = AbstractBlock.Settings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.METAL);
    public static final AbstractBlock.Settings CHRONOPLASM_POWDER_SETTINGS = AbstractBlock.Settings.create().strength(0.5F).sounds(BlockSoundGroup.SAND);
    public static final AbstractBlock.Settings TARDIS_BLOCK = AbstractBlock.Settings.create().strength(-1.0F, 3600000.8F).nonOpaque();
    public static final AbstractBlock.Settings BUTTON_SETTINGS = AbstractBlock.Settings.create().strength(0.5F).sounds(BlockSoundGroup.STONE).noCollision();
}
