package com.adamkali.dwm.model.json;

import com.adamkali.dwm.model.json.anim.AnimationRunner;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import com.adamkali.dwm.model.json.anim.EntityAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * {@link EntityModel} for a baked JSON mesh. When built with an animation, {@code setupAnim}
 * resets the pose (vanilla) and then applies the file's {@code animation} bindings.
 */
public class JsonEntityModel<S extends EntityRenderState> extends EntityModel<S> {
    private final AnimationRunner<S> animation;

    public JsonEntityModel(ModelPart root) {
        super(root);
        this.animation = null;
    }

    public JsonEntityModel(ModelPart root, EntityAnimation animation, AnimationVariables<S> variables) {
        super(root);
        this.animation = new AnimationRunner<>(root, animation, variables);
    }

    /** Animation as recorded for {@code layer} when its JSON loaded. */
    public JsonEntityModel(ModelPart root, ModelLayerLocation layer, AnimationVariables<S> variables) {
        this(root, EntityModelAnimations.get(layer), variables);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        if (animation != null) {
            animation.apply(state);
        }
    }
}
