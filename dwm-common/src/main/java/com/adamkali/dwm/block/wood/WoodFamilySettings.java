package com.adamkali.dwm.block.wood;

import com.adamkali.dwm.block.DWMBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public record WoodFamilySettings(
        BlockBehaviour.Properties planks,
        BlockBehaviour.Properties leaves,
        BlockBehaviour.Properties sapling,
        BlockBehaviour.Properties sign,
        BlockBehaviour.Properties hangingSign,
        BlockBehaviour.Properties button,
        BlockBehaviour.Properties pressurePlate,
        BlockBehaviour.Properties flowerPot,
        BlockBehaviour.Properties door,
        BlockBehaviour.Properties trapdoor
) {
    public static WoodFamilySettings of(MapColor planksColor) {
        return new WoodFamilySettings(
                BlockBehaviour.Properties.of()
                        .mapColor(planksColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(2.0F, 3.0F)
                        .sound(SoundType.WOOD)
                        .ignitedByLava(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .strength(0.2F)
                        .randomTicks()
                        .sound(SoundType.GRASS)
                        .noOcclusion()
                        .isValidSpawn(WoodFamilySettings::canSpawnOnLeaves)
                        .isSuffocating(WoodFamilySettings::never)
                        .isViewBlocking(WoodFamilySettings::never)
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY)
                        .isRedstoneConductor(WoodFamilySettings::never),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollision()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .pushReaction(PushReaction.DESTROY),
                BlockBehaviour.Properties.of()
                        .mapColor(planksColor)
                        .forceSolidOn()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollision()
                        .strength(1.0F)
                        .ignitedByLava(),
                BlockBehaviour.Properties.of()
                        .mapColor(planksColor)
                        .forceSolidOn()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollision()
                        .strength(1.0F)
                        .ignitedByLava(),
                BlockBehaviour.Properties.of()
                        .noCollision()
                        .strength(0.5F)
                        .pushReaction(PushReaction.DESTROY),
                BlockBehaviour.Properties.of()
                        .mapColor(planksColor)
                        .forceSolidOn()
                        .instrument(NoteBlockInstrument.BASS)
                        .noCollision()
                        .strength(0.5F)
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY),
                BlockBehaviour.Properties.of()
                        .instabreak()
                        .noOcclusion()
                        .pushReaction(PushReaction.DESTROY),
                BlockBehaviour.Properties.of()
                        .mapColor(planksColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .noOcclusion()
                        .ignitedByLava()
                        .pushReaction(PushReaction.DESTROY),
                BlockBehaviour.Properties.of()
                        .mapColor(planksColor)
                        .instrument(NoteBlockInstrument.BASS)
                        .strength(3.0F)
                        .noOcclusion()
                        .isValidSpawn(WoodFamilySettings::neverSpawn)
                        .ignitedByLava()
        );
    }

    public BlockBehaviour.Properties log(MapColor topColor, MapColor sideColor) {
        return DWMBlockSettings.ashLog(topColor, sideColor);
    }

    public BlockBehaviour.Properties wallSign(MapColor planksColor, net.minecraft.world.level.block.Block standingSign) {
        return BlockBehaviour.Properties.of()
                .mapColor(planksColor)
                .forceSolidOn()
                .instrument(NoteBlockInstrument.BASS)
                .noCollision()
                .strength(1.0F)
                .ignitedByLava()
                .overrideLootTable(java.util.Optional.of(standingSign.getLootTable().orElseThrow()))
                .overrideDescription(standingSign.getDescriptionId());
    }

    private static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    private static Boolean neverSpawn(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    private static Boolean canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return type == EntityTypes.OCELOT || type == EntityTypes.PARROT;
    }
}
