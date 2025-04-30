package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;

public abstract class TardisModel extends EntityModel<TardisRenderState> {

    protected TardisModel(ModelPart root) {
        super(root);
    }
}
