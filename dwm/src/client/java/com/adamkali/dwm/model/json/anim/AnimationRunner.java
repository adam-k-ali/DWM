package com.adamkali.dwm.model.json.anim;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link EntityAnimation} bound to a baked model: part paths and variable names are resolved
 * once at construction, so unknown ones fail when the model is built, not mid-frame.
 */
public final class AnimationRunner<S extends EntityRenderState> {
    private record Bound(ModelPart part, AnimationChannel channel, Expression.Compiled value) {
    }

    private final AnimationVariables<S> variables;
    private final List<Bound> bound;
    private final float[] values;

    public AnimationRunner(ModelPart root, EntityAnimation animation, AnimationVariables<S> variables) {
        this.variables = variables;
        List<String> names = variables.names();
        this.values = new float[names.size()];
        List<Bound> bound = new ArrayList<>();
        for (AnimationBinding binding : animation.bindings()) {
            try {
                bound.add(new Bound(
                        resolve(root, binding.partPath()),
                        binding.channel(),
                        binding.value().compile(names)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("animation binding {part=" + binding.partPath()
                        + ", channel=" + binding.channel().getSerializedName() + "}: " + e.getMessage(), e);
            }
        }
        this.bound = List.copyOf(bound);
    }

    static ModelPart resolve(ModelPart root, String path) {
        ModelPart part = root;
        if (path.isEmpty()) {
            return part;
        }
        for (String name : path.split("/")) {
            if (!part.hasChild(name)) {
                throw new IllegalArgumentException("unknown part '" + name + "' in path '" + path + "'");
            }
            part = part.getChild(name);
        }
        return part;
    }

    /** Adds every binding to the (already reset) pose. */
    public void apply(S state) {
        if (bound.isEmpty()) {
            return;
        }
        variables.read(state, values);
        for (Bound b : bound) {
            b.channel().add(b.part(), b.value().eval(values));
        }
    }
}
