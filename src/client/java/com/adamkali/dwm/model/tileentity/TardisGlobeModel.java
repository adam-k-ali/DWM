// Made with Blockbench (converted from tardis_globe.bbmodel)
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

public class TardisGlobeModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_globe"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_globe.png");

    public TardisGlobeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        root.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(2, 2).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 4).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 4).addBox(-2.0F, 18.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(14, 8).addBox(-1.0F, 20.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(15, 9).addBox(-1.5F, 19.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(13, 7).addBox(-0.5F, 21.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition GlobePart = root.addOrReplaceChild(
                "Globe",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 25.5F, 0.0F));

        GlobePart.addOrReplaceChild(
                "mesh_1",
                CubeListBuilder.create()
                        .texOffs(9, 7).addBox(3.5F, -2.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 7).addBox(-4.5F, -2.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 7).addBox(-4.5F, 4.5F, -0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 7).addBox(-4.5F, -3.5F, -0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition arrowPart = root.addOrReplaceChild(
                "arrow",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 24.5F, 0.5F, 0.0F, 0.0F, -0.785398F));

        arrowPart.addOrReplaceChild(
                "mesh_2",
                CubeListBuilder.create()
                        .texOffs(12, 8).addBox(-1.914214F, -7.085786F, -0.5F, 1.0F, 4.0F, 0.01F, new CubeDeformation(0.0F))
                        .texOffs(8, 6).addBox(-1.914214F, 5.914214F, -0.5F, 1.0F, 3.0F, 0.01F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition Globe2Part = root.addOrReplaceChild(
                "Globe2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 25.5F, 0.0F, 0.785398F, 1.570796F, 0.0F));

        Globe2Part.addOrReplaceChild(
                "mesh_3",
                CubeListBuilder.create()
                        .texOffs(9, 9).addBox(3.5F, -2.792893F, -1.207107F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 9).addBox(-4.5F, -2.792893F, -1.207107F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 9).addBox(-4.5F, 4.207107F, -1.207107F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 9).addBox(-4.5F, -3.792893F, -1.207107F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition Globe3Part = root.addOrReplaceChild(
                "Globe3",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 25.5F, 0.0F, -0.785398F, 1.570796F, 0.0F));

        Globe3Part.addOrReplaceChild(
                "mesh_4",
                CubeListBuilder.create()
                        .texOffs(9, 9).addBox(3.5F, -2.792893F, 0.207107F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 9).addBox(-4.5F, -2.792893F, 0.207107F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 9).addBox(-4.5F, 4.207107F, 0.207107F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 9).addBox(-4.5F, -3.792893F, 0.207107F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition arrow2Part = root.addOrReplaceChild(
                "arrow2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(1.0F, 25.5F, 0.5F, 0.0F, 0.0F, 0.785398F));

        arrow2Part.addOrReplaceChild(
                "mesh_5",
                CubeListBuilder.create()
                        .texOffs(8, 6).addBox(-0.5F, -4.085786F, -0.5F, 1.0F, 1.0F, 0.01F, new CubeDeformation(0.0F))
                        .texOffs(12, 8).addBox(-0.5F, 5.914214F, -0.5F, 1.0F, 1.0F, 0.01F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, 16, 16);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static decor mesh.
    }
}
