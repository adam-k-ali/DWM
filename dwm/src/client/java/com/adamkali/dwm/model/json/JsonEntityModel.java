package com.adamkali.dwm.model.json;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * {@link EntityModel} that only holds a baked JSON mesh. Animation stays on subclasses.
 */
public class JsonEntityModel<S extends EntityRenderState> extends EntityModel<S> {
    public JsonEntityModel(ModelPart root) {
        super(root);
    }
}
