// Made with Blockbench (converted from materialisation_lever.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class MaterialisationLeverModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "materialisation_lever"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/materialisation_lever.png");

    public MaterialisationLeverModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        PartDefinition demat = root.addOrReplaceChild(
                "demat",
                CubeListBuilder.create(),
                PartPose.offset(-1.0F, 0.0F, 0.0F));

        demat.addOrReplaceChild(
                "lever",
                CubeListBuilder.create()
                        .texOffs(28, 25).addBox(0.5F, -0.3F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 29).addBox(-0.5F, 6.2F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 29).addBox(0.0F, 5.7F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 1.0F, 0.0F, 1.047198F, 0.0F, 0.0F));

        demat.addOrReplaceChild(
                "panel",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5F, -2.7F, -9.0F, 7.0F, 1.0F, 18.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.5F, -1.7F, -8.0F, 5.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.5F, -0.7F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(3.5F, -0.7F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(2.5F, -1.2F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(2.5F, -1.2F, -6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.5F, 0.3F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(3.5F, 0.3F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.5F, 1.3F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(3.5F, 1.3F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 2.7F, 0.0F));

        return LayerDefinition.create(modelData, 32, 32);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static control mesh for v1 (no lever pitch sync).
    }
}
