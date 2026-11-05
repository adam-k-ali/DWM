package com.adamkali.dwm.block.definition;

import com.adamkali.dwm.data.definition.NamedValues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Optional;

/**
 * JSON shape of {@link BlockBehaviour.Properties}. Every field is optional; absent fields leave the vanilla default
 * (or, for stairs/slab/wall, the copied base block's value) untouched.
 */
public record BlockProperties(
        Optional<MapColor> mapColor,
        Optional<NoteBlockInstrument> instrument,
        Optional<Strength> strength,
        Optional<SoundType> sound,
        Optional<Boolean> requiresCorrectTool,
        Optional<Integer> lightLevel,
        Optional<Boolean> occlude,
        Optional<Boolean> collision,
        Optional<Boolean> instabreak,
        Optional<Boolean> randomTicks,
        Optional<Boolean> ignitedByLava,
        Optional<PushReaction> pushReaction,
        Optional<BlockBehaviour.OffsetType> offsetType
) {
    public static final BlockProperties EMPTY = new BlockProperties(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty());

    public static final Codec<MapColor> MAP_COLOR = NamedValues.codec("map_color", NamedValues.constants(MapColor.class, MapColor.class));
    public static final Codec<SoundType> SOUND = NamedValues.codec("sound", NamedValues.constants(SoundType.class, SoundType.class));

    public static final Codec<BlockProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_COLOR.optionalFieldOf("map_color").forGetter(BlockProperties::mapColor),
            NamedValues.enumCodec("instrument", NoteBlockInstrument.class).optionalFieldOf("instrument").forGetter(BlockProperties::instrument),
            Strength.CODEC.optionalFieldOf("strength").forGetter(BlockProperties::strength),
            SOUND.optionalFieldOf("sound").forGetter(BlockProperties::sound),
            Codec.BOOL.optionalFieldOf("requires_correct_tool").forGetter(BlockProperties::requiresCorrectTool),
            Codec.intRange(0, 15).optionalFieldOf("light_level").forGetter(BlockProperties::lightLevel),
            Codec.BOOL.optionalFieldOf("occlude").forGetter(BlockProperties::occlude),
            Codec.BOOL.optionalFieldOf("collision").forGetter(BlockProperties::collision),
            Codec.BOOL.optionalFieldOf("instabreak").forGetter(BlockProperties::instabreak),
            Codec.BOOL.optionalFieldOf("random_ticks").forGetter(BlockProperties::randomTicks),
            Codec.BOOL.optionalFieldOf("ignited_by_lava").forGetter(BlockProperties::ignitedByLava),
            NamedValues.enumCodec("push_reaction", PushReaction.class).optionalFieldOf("push_reaction").forGetter(BlockProperties::pushReaction),
            NamedValues.enumCodec("offset_type", BlockBehaviour.OffsetType.class).optionalFieldOf("offset_type").forGetter(BlockProperties::offsetType)
    ).apply(instance, BlockProperties::new));

    /** {@code explosion_resistance} defaults to {@code destroy_time}, matching {@code Properties.strength(float)}. */
    public record Strength(float destroyTime, float explosionResistance) {
        public static final Codec<Strength> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("destroy_time").forGetter(Strength::destroyTime),
                Codec.FLOAT.optionalFieldOf("explosion_resistance").forGetter(strength -> Optional.of(strength.explosionResistance()))
        ).apply(instance, (destroy, resistance) -> new Strength(destroy, resistance.orElse(destroy))));
    }

    /** Applies the declared fields to {@code target} in a fixed, order-independent-of-JSON sequence. */
    public BlockBehaviour.Properties apply(BlockBehaviour.Properties target) {
        mapColor.ifPresent(target::mapColor);
        instrument.ifPresent(target::instrument);
        strength.ifPresent(value -> target.strength(value.destroyTime(), value.explosionResistance()));
        sound.ifPresent(target::sound);
        if (requiresCorrectTool.orElse(false)) {
            target.requiresCorrectToolForDrops();
        }
        lightLevel.ifPresent(level -> target.lightLevel(state -> level));
        if (occlude.isPresent() && !occlude.get()) {
            target.noOcclusion();
        }
        if (collision.isPresent() && !collision.get()) {
            target.noCollision();
        }
        if (instabreak.orElse(false)) {
            target.instabreak();
        }
        if (randomTicks.orElse(false)) {
            target.randomTicks();
        }
        if (ignitedByLava.orElse(false)) {
            target.ignitedByLava();
        }
        pushReaction.ifPresent(target::pushReaction);
        offsetType.ifPresent(target::offsetType);
        return target;
    }
}
