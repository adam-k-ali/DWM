// Made with Blockbench (converted from tardis_cloak.bbmodel)
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
 * Panel4 cloak / perception-filter lever.
 */
public class CloakLeverModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cloak_lever"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/misc_levers_1.png");

    /** Bbmodel rest pose (disengaged). */
    public static final float LEVER_PITCH_OFF = -1.134464F;
    /** Raised when the perception filter is engaged. */
    public static final float LEVER_PITCH_ON = -0.261799F;

    private final ModelPart lever;

    public CloakLeverModel(ModelPart root) {
        super(root);
        this.lever = root.getChild("lever").getChild("lever_control2");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition lever = root.addOrReplaceChild(
                "lever",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 2.0F));

        PartDefinition lever_control2 = lever.addOrReplaceChild(
                "lever_control2",
                CubeListBuilder.create()
                        .texOffs(20, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 28).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.5F, -2.5F, -1.134464F, 0.0F, 0.0F));

        PartDefinition lever_panel2 = lever.addOrReplaceChild(
                "lever_panel2",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(5.5F, 0.725F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 0).addBox(3.0F, -2.275F, -5.5F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 0).addBox(3.5F, -1.275F, -4.5F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(3.5F, -0.275F, -3.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(5.5F, -0.275F, -3.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 16).addBox(4.5F, -0.275F, -3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 16).addBox(4.5F, -0.275F, 2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(3.5F, 0.725F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.275F, -2.5F));

        return LayerDefinition.create(modelData, 16, 16);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        lever.xRot = state.isCloaked() ? LEVER_PITCH_ON : LEVER_PITCH_OFF;
    }
}
