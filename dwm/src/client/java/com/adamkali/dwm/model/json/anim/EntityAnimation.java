package com.adamkali.dwm.model.json.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code animation} block of an entity model file: bindings applied after the pose reset.
 * Several bindings on the same part and channel sum.
 */
public record EntityAnimation(List<AnimationBinding> bindings) {
    public static final EntityAnimation EMPTY = new EntityAnimation(List.of());

    public static final Codec<EntityAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AnimationBinding.CODEC.listOf().optionalFieldOf("bindings", List.of())
                    .forGetter(EntityAnimation::bindings)
    ).apply(instance, EntityAnimation::new));

    public EntityAnimation {
        bindings = List.copyOf(bindings);
    }

    public boolean isEmpty() {
        return bindings.isEmpty();
    }

    /** Child bindings replace same part+channel bindings of the parent in place; new ones append. */
    public EntityAnimation overlay(EntityAnimation child) {
        if (child.isEmpty()) {
            return this;
        }
        Map<String, AnimationBinding> overrides = new LinkedHashMap<>();
        child.bindings.forEach(binding -> overrides.put(binding.mergeKey(), binding));
        List<AnimationBinding> merged = new ArrayList<>();
        for (AnimationBinding binding : bindings) {
            AnimationBinding override = overrides.remove(binding.mergeKey());
            merged.add(override != null ? override : binding);
        }
        merged.addAll(overrides.values());
        return new EntityAnimation(merged);
    }
}
