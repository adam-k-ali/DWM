package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;

public abstract class TardisModel extends EntityModel<TardisRenderState> {

    protected TardisModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setAngles(TardisRenderState state) {
        super.setAngles(state);
        float doorSwingProgress = state.getDoorSwingProgress();
        ModelPart leftDoor = null;

        if (root.hasChild("LeftDoor")) {
            leftDoor = root.getChild("LeftDoor");
        } else if (root.hasChild("Main") && root.getChild("Main").hasChild("LeftDoor")) {
            leftDoor = root.getChild("Main").getChild("LeftDoor");
        }

        if (leftDoor != null) {
            leftDoor.setAngles(0.0F, doorSwingProgress * (float) Math.PI / 3, 0.0F);
        }
    }
}
