package com.adamkali.dwm.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class DWMBlockSettings {
    public static final BlockBehaviour.Properties TARDIS_WALL_SETTINGS = BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.METAL);
    public static final BlockBehaviour.Properties CHRONOPLASM_POWDER_SETTINGS = BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND);
    public static final BlockBehaviour.Properties TARDIS_BLOCK = BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noOcclusion();
    public static final BlockBehaviour.Properties TARDIS_INTERIOR_DOOR = BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noOcclusion();
    public static final BlockBehaviour.Properties FIRST_DOCTOR_CONSOLE = BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noOcclusion();
    public static final BlockBehaviour.Properties BUTTON_SETTINGS = BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.STONE).noCollision();

    public static final BlockBehaviour.Properties GALLIFREY_STONE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(1.5F, 6.0F);

    public static final BlockBehaviour.Properties GALLIFREY_SANDSTONE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(0.8F);

    public static final BlockBehaviour.Properties GALLIFREY_SAND = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .instrument(NoteBlockInstrument.SNARE)
            .strength(0.5F)
            .sound(SoundType.SAND);

    public static final BlockBehaviour.Properties GALLIFREY_DIRT = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .strength(0.5F)
            .sound(SoundType.GRAVEL);

    public static final BlockBehaviour.Properties GALLIFREY_GRASS = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(0.6F)
            .sound(SoundType.GRASS);

    public static final BlockBehaviour.Properties ORANGE_SAND = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .instrument(NoteBlockInstrument.SNARE)
            .strength(0.5F)
            .sound(SoundType.SAND);

    public static final BlockBehaviour.Properties ORANGE_SANDSTONE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(0.8F);

    public static final BlockBehaviour.Properties CITADEL = BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(1.5F, 6.0F)
            .sound(SoundType.STONE);

    public static final BlockBehaviour.Properties CITADEL_GLASS = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((state, world, pos, type) -> false)
            .isRedstoneConductor((state, world, pos) -> false)
            .isSuffocating((state, world, pos) -> false)
            .isViewBlocking((state, world, pos) -> false);

    public static final MapColor ASH_PLANKS_COLOR = MapColor.TERRACOTTA_WHITE;
    public static final MapColor ASH_BARK_COLOR = MapColor.TERRACOTTA_GRAY;
    public static final MapColor DARK_ASH_PLANKS_COLOR = MapColor.TERRACOTTA_BROWN;
    public static final MapColor DARK_ASH_BARK_COLOR = MapColor.TERRACOTTA_GRAY;

    public static BlockBehaviour.Properties ashLog(MapColor topMapColor, MapColor sideMapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(state -> state.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)
                        && state.getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS) == Direction.Axis.Y
                        ? topMapColor
                        : sideMapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }
}
