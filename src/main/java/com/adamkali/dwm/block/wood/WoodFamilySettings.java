package com.adamkali.dwm.block.wood;

import com.adamkali.dwm.block.DWMBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public record WoodFamilySettings(
        AbstractBlock.Settings planks,
        AbstractBlock.Settings leaves,
        AbstractBlock.Settings sapling,
        AbstractBlock.Settings sign,
        AbstractBlock.Settings hangingSign,
        AbstractBlock.Settings button,
        AbstractBlock.Settings pressurePlate,
        AbstractBlock.Settings flowerPot,
        AbstractBlock.Settings door,
        AbstractBlock.Settings trapdoor
) {
    public static WoodFamilySettings of(MapColor planksColor) {
        return new WoodFamilySettings(
                AbstractBlock.Settings.create()
                        .mapColor(planksColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F)
                        .sounds(BlockSoundGroup.WOOD)
                        .burnable(),
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.DARK_GREEN)
                        .strength(0.2F)
                        .ticksRandomly()
                        .sounds(BlockSoundGroup.GRASS)
                        .nonOpaque()
                        .allowsSpawning(WoodFamilySettings::canSpawnOnLeaves)
                        .suffocates(WoodFamilySettings::never)
                        .blockVision(WoodFamilySettings::never)
                        .burnable()
                        .pistonBehavior(PistonBehavior.DESTROY)
                        .solidBlock(WoodFamilySettings::never),
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.DARK_GREEN)
                        .noCollision()
                        .ticksRandomly()
                        .breakInstantly()
                        .sounds(BlockSoundGroup.GRASS)
                        .pistonBehavior(PistonBehavior.DESTROY),
                AbstractBlock.Settings.create()
                        .mapColor(planksColor)
                        .solid()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollision()
                        .strength(1.0F)
                        .burnable(),
                AbstractBlock.Settings.create()
                        .mapColor(planksColor)
                        .solid()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollision()
                        .strength(1.0F)
                        .burnable(),
                AbstractBlock.Settings.create()
                        .noCollision()
                        .strength(0.5F)
                        .pistonBehavior(PistonBehavior.DESTROY),
                AbstractBlock.Settings.create()
                        .mapColor(planksColor)
                        .solid()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollision()
                        .strength(0.5F)
                        .burnable()
                        .pistonBehavior(PistonBehavior.DESTROY),
                AbstractBlock.Settings.create()
                        .breakInstantly()
                        .nonOpaque()
                        .pistonBehavior(PistonBehavior.DESTROY),
                AbstractBlock.Settings.create()
                        .mapColor(planksColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .nonOpaque()
                        .burnable()
                        .pistonBehavior(PistonBehavior.DESTROY),
                AbstractBlock.Settings.create()
                        .mapColor(planksColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .nonOpaque()
                        .allowsSpawning(WoodFamilySettings::neverSpawn)
                        .burnable()
        );
    }

    public AbstractBlock.Settings log(MapColor topColor, MapColor sideColor) {
        return DWMBlockSettings.ashLog(topColor, sideColor);
    }

    public AbstractBlock.Settings wallSign(MapColor planksColor, net.minecraft.block.Block standingSign) {
        return AbstractBlock.Settings.create()
                .mapColor(planksColor)
                .solid()
                .instrument(NoteBlockInstrument.BASS)
                .noCollision()
                .strength(1.0F)
                .burnable()
                .lootTable(java.util.Optional.of(standingSign.getLootTableKey().orElseThrow()))
                .overrideTranslationKey(standingSign.getTranslationKey());
    }

    private static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    private static Boolean neverSpawn(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    private static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
