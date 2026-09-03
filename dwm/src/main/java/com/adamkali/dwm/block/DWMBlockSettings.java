package com.adamkali.dwm.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class DWMBlockSettings {
    public static final BlockBehaviour.Properties TARDIS_WALL_SETTINGS = BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.METAL);

    /** Fresh properties for roundels (avoids shared setId / noOcclusion mutation; emits light). */
    public static BlockBehaviour.Properties tardisRoundel() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F)
                .sound(SoundType.METAL)
                .lightLevel(state -> 10);
    }

    /** Roundel B variants: same as {@link #tardisRoundel()} with no occlusion. */
    public static BlockBehaviour.Properties tardisRoundelNoOcclusion() {
        return tardisRoundel().noOcclusion();
    }

    public static final BlockBehaviour.Properties CHRONOPLASM_POWDER_SETTINGS = BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND);
    public static final BlockBehaviour.Properties TARDIS_BLOCK = BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noOcclusion();
    public static final BlockBehaviour.Properties TARDIS_INTERIOR_DOOR = BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noOcclusion();
    public static final BlockBehaviour.Properties FIRST_DOCTOR_CONSOLE = BlockBehaviour.Properties.of()
            .strength(-1.0F, 3600000.8F)
            .noOcclusion()
            .lightLevel(state -> 15);
    public static final BlockBehaviour.Properties BUTTON_SETTINGS = BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.STONE).noCollision();

    /** Breakable interior decor props (chairs, column, globe, scanners). */
    public static final BlockBehaviour.Properties TARDIS_DECOR_SETTINGS =
            BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.METAL).noOcclusion();

    /** Fresh properties for ceiling vent (avoids shared setId mutation; emits light). */
    public static BlockBehaviour.Properties tardisCeilingVent() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel(state -> 12);
    }

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

    /** Fresh properties for each Gallifrey cross flower (avoids shared setId mutation). */
    public static BlockBehaviour.Properties gallifreyCrossPlant() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY);
    }

    public static BlockBehaviour.Properties gallifreyPottedPlant() {
        return BlockBehaviour.Properties.of()
                .instabreak()
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    public static BlockBehaviour.Properties saccharineCane() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY);
    }

    /** Fresh properties for azbantium ore/block (harder than obsidian; avoids shared setId mutation). */
    public static BlockBehaviour.Properties azbantium() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(55.0F, 1200.0F)
                .sound(SoundType.METAL);
    }

    /** Fresh properties for Gallifrey-stone vanilla ores (vanilla ore hardness; avoids shared setId mutation). */
    public static BlockBehaviour.Properties gallifreyVanillaOre() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F)
                .sound(SoundType.STONE);
    }

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

    public static final MapColor PETRIFIED_PLANKS_COLOR = MapColor.COLOR_GRAY;
    public static final MapColor PETRIFIED_BARK_COLOR = MapColor.TERRACOTTA_GRAY;

    /** Mineralized Skaro petrified wood — pickaxe, nonflammable (no {@code ignitedByLava}). */
    public static BlockBehaviour.Properties petrified() {
        return BlockBehaviour.Properties.of()
                .mapColor(PETRIFIED_PLANKS_COLOR)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(2.0F, 6.0F)
                .sound(SoundType.BASALT);
    }

    /** Axis-aware petrified log/wood properties (nonflammable, pickaxe). */
    public static BlockBehaviour.Properties petrifiedLog(MapColor topMapColor, MapColor sideMapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(state -> state.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)
                        && state.getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS) == Direction.Axis.Y
                        ? topMapColor
                        : sideMapColor)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(2.0F, 6.0F)
                .sound(SoundType.BASALT);
    }
}
