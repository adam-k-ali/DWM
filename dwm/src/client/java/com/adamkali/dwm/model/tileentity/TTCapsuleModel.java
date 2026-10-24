// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

public class TTCapsuleModel extends TardisModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tt_capsule"), "tt_capsule");


    private final ModelPart door;

    public TTCapsuleModel(ModelPart root) {
        super(root);
        ModelPart bone = root.getChild("bone");
        this.door = bone.getChild("door");
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        float doorSwingProgress = state.getDoorSwingProgress();
        this.door.setRotation(0.0F, doorSwingProgress * (float) Math.PI / 2, 0.0F);
    }
}
