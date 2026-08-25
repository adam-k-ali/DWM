// Made with Blockbench (converted from Stabilisers.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition stableAdjust = root.addOrReplaceChild(
                "stable_adjust",
                CubeListBuilder.create(),
                PartPose.offset(-6.0F, 0.0F, 0.0F));

        stableAdjust.addOrReplaceChild(
                "lever",
                CubeListBuilder.create()
                        .texOffs(28, 25).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 29).addBox(-1.5F, 6.2F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 29).addBox(-1.0F, 5.7F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 1.0F, 0.0F, LEVER_PITCH_ON, 0.0F, 0.0F));

        stableAdjust.addOrReplaceChild(
                "panel",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, 0.0F, -9.0F, 7.0F, 1.0F, 18.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, 1.0F, -8.0F, 5.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, 2.0F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(1.0F, 2.0F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, 3.0F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(1.0F, 3.0F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, 4.0F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(1.0F, 4.0F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, 1.5F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, 1.5F, -6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild(
                "panel2",
                CubeListBuilder.create()
                        .texOffs(0, 19).addBox(1.0F, 0.0F, -1.0F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 20).addBox(1.5F, 0.2F, -0.5F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 30).addBox(1.0F, 0.0F, 7.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(13, 29).addBox(3.5F, 0.3F, 1.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild(
                "stable_on",
                CubeListBuilder.create()
                        .texOffs(10, 28).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.5F, 0.7F, 1.6F));

        return LayerDefinition.create(modelData, 32, 32);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        lever.xRot = state.isStabilisersEnabled() ? LEVER_PITCH_ON : LEVER_PITCH_OFF;
    }
}
