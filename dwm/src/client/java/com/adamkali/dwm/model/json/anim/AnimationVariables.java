package com.adamkali.dwm.model.json.anim;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * The named inputs a model type exposes to its animation expressions, read from its render state.
 */
public final class AnimationVariables<S extends EntityRenderState> {
    private final Map<String, ToDoubleFunction<S>> inputs;

    private AnimationVariables(Map<String, ToDoubleFunction<S>> inputs) {
        this.inputs = inputs;
    }

    public static <S extends EntityRenderState> Builder<S> builder() {
        return new Builder<>();
    }

    /**
     * {@code age}, {@code walk_pos}, {@code walk_speed}, {@code head_pitch} and {@code head_yaw}
     * (degrees, as on the render state).
     */
    public static <S extends LivingEntityRenderState> Builder<S> living() {
        Builder<S> builder = builder();
        return builder
                .add("age", state -> state.ageInTicks)
                .add("walk_pos", state -> state.walkAnimationPos)
                .add("walk_speed", state -> state.walkAnimationSpeed)
                .add("head_pitch", state -> state.xRot)
                .add("head_yaw", state -> state.yRot);
    }

    public List<String> names() {
        return List.copyOf(inputs.keySet());
    }

    /** Reads every input from {@code state} into {@code out}, in {@link #names()} order. */
    void read(S state, float[] out) {
        int i = 0;
        for (ToDoubleFunction<S> input : inputs.values()) {
            out[i++] = (float) input.applyAsDouble(state);
        }
    }

    public static final class Builder<S extends EntityRenderState> {
        private final Map<String, ToDoubleFunction<S>> inputs = new LinkedHashMap<>();

        public Builder<S> add(String name, ToDoubleFunction<S> input) {
            if (inputs.put(name, input) != null) {
                throw new IllegalArgumentException("duplicate animation variable: " + name);
            }
            return this;
        }

        public AnimationVariables<S> build() {
            return new AnimationVariables<>(new LinkedHashMap<>(inputs));
        }
    }
}
