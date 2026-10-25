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

    private boolean hasKey(String key) {
        return bindings.stream().anyMatch(binding -> binding.mergeKey().equals(key));
    }

    /**
     * Child bindings replace all parent bindings on the same part+channel (in place, at the first
     * parent occurrence); new ones append. Several child bindings on one key all apply.
     */
    public EntityAnimation overlay(EntityAnimation child) {
        if (child.isEmpty()) {
            return this;
        }
        Map<String, List<AnimationBinding>> overrides = new LinkedHashMap<>();
        child.bindings.forEach(binding ->
                overrides.computeIfAbsent(binding.mergeKey(), key -> new ArrayList<>()).add(binding));
        List<AnimationBinding> merged = new ArrayList<>();
        for (AnimationBinding binding : bindings) {
            String key = binding.mergeKey();
            if (!child.hasKey(key)) {
                merged.add(binding);
                continue;
            }
            List<AnimationBinding> replacement = overrides.remove(key);
            if (replacement != null) {
                merged.addAll(replacement);
            }
        }
        overrides.values().forEach(merged::addAll);
        return new EntityAnimation(merged);
    }
}
