package com.adamkali.dwm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.Direction;

public class DWMBlockSettings {
    public static final AbstractBlock.Settings TARDIS_WALL_SETTINGS = AbstractBlock.Settings.create().strength(2.0F, 3.0F).sounds(BlockSoundGroup.METAL);
    public static final AbstractBlock.Settings CHRONOPLASM_POWDER_SETTINGS = AbstractBlock.Settings.create().strength(0.5F).sounds(BlockSoundGroup.SAND);
    public static final AbstractBlock.Settings TARDIS_BLOCK = AbstractBlock.Settings.create().strength(-1.0F, 3600000.8F).nonOpaque();
    public static final AbstractBlock.Settings TARDIS_INTERIOR_DOOR = AbstractBlock.Settings.create().strength(-1.0F, 3600000.8F).nonOpaque();
    public static final AbstractBlock.Settings FIRST_DOCTOR_CONSOLE = AbstractBlock.Settings.create().strength(-1.0F, 3600000.8F).nonOpaque();
    public static final AbstractBlock.Settings BUTTON_SETTINGS = AbstractBlock.Settings.create().strength(0.5F).sounds(BlockSoundGroup.STONE).noCollision();

    public static final AbstractBlock.Settings GALLIFREY_STONE = AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresTool()
            .strength(1.5F, 6.0F);

    public static final AbstractBlock.Settings GALLIFREY_SANDSTONE = AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresTool()
            .strength(0.8F);

    public static final AbstractBlock.Settings GALLIFREY_SAND = AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.SNARE)
            .strength(0.5F)
            .sounds(BlockSoundGroup.SAND);

    public static final AbstractBlock.Settings GALLIFREY_DIRT = AbstractBlock.Settings.create()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .strength(0.5F)
            .sounds(BlockSoundGroup.GRAVEL);

    public static final MapColor ASH_PLANKS_COLOR = MapColor.TERRACOTTA_WHITE;
    public static final MapColor ASH_BARK_COLOR = MapColor.TERRACOTTA_GRAY;
    public static final MapColor DARK_ASH_PLANKS_COLOR = MapColor.TERRACOTTA_BROWN;
    public static final MapColor DARK_ASH_BARK_COLOR = MapColor.TERRACOTTA_GRAY;

    public static AbstractBlock.Settings ashLog(MapColor topMapColor, MapColor sideMapColor) {
        return AbstractBlock.Settings.create()
                .mapColor(state -> state.contains(net.minecraft.block.PillarBlock.AXIS)
                        && state.get(net.minecraft.block.PillarBlock.AXIS) == Direction.Axis.Y
                        ? topMapColor
                        : sideMapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sounds(BlockSoundGroup.WOOD)
                .burnable();
    }
}
