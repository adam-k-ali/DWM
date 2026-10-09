package com.adamkali.dwm.model.json.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Adds {@code value} to one pose channel of one part each frame. {@code part} is a slash path from
 * the model root ({@code "neck/head"}); absent targets the root part itself.
 */
public record AnimationBinding(Optional<String> part, AnimationChannel channel, Expression value) {
    public static final Codec<AnimationBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("part").forGetter(AnimationBinding::part),
            AnimationChannel.CODEC.fieldOf("channel").forGetter(AnimationBinding::channel),
            Expression.CODEC.fieldOf("value").forGetter(AnimationBinding::value)
    ).apply(instance, AnimationBinding::new));

    public String partPath() {
        return part.orElse("");
    }

    /** Child bindings replace parent bindings that share this key. */
    String mergeKey() {
        return partPath() + "#" + channel.getSerializedName();
    }
}
