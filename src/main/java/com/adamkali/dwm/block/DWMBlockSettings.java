package com.adamkali.dwm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

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

    public static final AbstractBlock.Settings ASH_PLANKS = AbstractBlock.Settings.create()
            .mapColor(ASH_PLANKS_COLOR)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 3.0F)
            .sounds(BlockSoundGroup.WOOD)
            .burnable();

    public static final AbstractBlock.Settings ASH_LEAVES = AbstractBlock.Settings.create()
            .mapColor(MapColor.DARK_GREEN)
            .strength(0.2F)
            .ticksRandomly()
            .sounds(BlockSoundGroup.GRASS)
            .nonOpaque()
            .allowsSpawning(DWMBlockSettings::canSpawnOnLeaves)
            .suffocates(DWMBlockSettings::never)
            .blockVision(DWMBlockSettings::never)
            .burnable()
            .pistonBehavior(PistonBehavior.DESTROY)
            .solidBlock(DWMBlockSettings::never);

    public static final AbstractBlock.Settings ASH_SAPLING = AbstractBlock.Settings.create()
            .mapColor(MapColor.DARK_GREEN)
            .noCollision()
            .ticksRandomly()
            .breakInstantly()
            .sounds(BlockSoundGroup.GRASS)
            .pistonBehavior(PistonBehavior.DESTROY);

    public static final AbstractBlock.Settings ASH_SIGN = AbstractBlock.Settings.create()
            .mapColor(ASH_PLANKS_COLOR)
            .solid()
            .instrument(NoteBlockInstrument.BASS)
            .noCollision()
            .strength(1.0F)
            .burnable();

    public static final AbstractBlock.Settings ASH_HANGING_SIGN = AbstractBlock.Settings.create()
            .mapColor(ASH_PLANKS_COLOR)
            .solid()
            .instrument(NoteBlockInstrument.BASS)
            .noCollision()
            .strength(1.0F)
            .burnable();

    public static final AbstractBlock.Settings ASH_BUTTON = AbstractBlock.Settings.create()
            .noCollision()
            .strength(0.5F)
            .pistonBehavior(PistonBehavior.DESTROY);

    public static final AbstractBlock.Settings ASH_PRESSURE_PLATE = AbstractBlock.Settings.create()
            .mapColor(ASH_PLANKS_COLOR)
            .solid()
            .instrument(NoteBlockInstrument.BASS)
            .noCollision()
            .strength(0.5F)
            .burnable()
            .pistonBehavior(PistonBehavior.DESTROY);

    public static final AbstractBlock.Settings ASH_FLOWER_POT = AbstractBlock.Settings.create()
            .breakInstantly()
            .nonOpaque()
            .pistonBehavior(PistonBehavior.DESTROY);

    private static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    private static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
