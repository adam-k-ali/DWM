package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Panel6 bottom-row stabilisers control. Lever pitch reflects on/off state.
 */
public class StabilisersModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "stabilisers"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/stabilisers.png");

    /** Bbmodel rest pose (enabled). */
    public static final float LEVER_PITCH_ON = 1.047198F;
    /** Flattened when stabilisers are off. */
    public static final float LEVER_PITCH_OFF = 0.174533F;

    private final ModelPart lever;

    public StabilisersModel(ModelPart root) {
        super(root);
        this.lever = root.getChild("stable_adjust").getChild("lever");
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        lever.xRot = state.isStabilisersEnabled() ? LEVER_PITCH_ON : LEVER_PITCH_OFF;
    }
}
