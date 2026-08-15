// Made with Blockbench (converted from tardis_full_scanner.bbmodel)
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

public class TardisFullScannerModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_full_scanner"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_full_scanner.png");

    public TardisFullScannerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition sliderPart = root.addOrReplaceChild(
                "slider",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 8.0F, -4.5F));

        sliderPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(1, 1).addBox(-8.0F, 9.0F, -0.5F, 16.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition scannerPart = root.addOrReplaceChild(
                "scanner",
                CubeListBuilder.create(),
                PartPose.offset(-13.0F, -1.0F, -8.6F));

        scannerPart.addOrReplaceChild(
                "mesh_1",
                CubeListBuilder.create()
                        .texOffs(3, 3).addBox(5.0F, 17.0F, 0.6F, 16.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 12).addBox(5.0F, 17.0F, 4.6F, 16.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(5.0F, 18.0F, 4.1F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(5.0F, 31.0F, 4.1F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(5.0F, 19.0F, 4.1F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(20.0F, 19.0F, 4.1F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 48).addBox(5.0F, 18.0F, 4.6F, 16.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(5.0F, 32.0F, 0.6F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(21.0F, 17.0F, 0.6F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(-11.0F, 17.0F, 0.6F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(-11.0F, 1.0F, 0.6F, 48.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(29.0F, 19.0F, 0.1F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(33.0F, 20.0F, 0.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-8.0F, 20.0F, 0.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-4.0F, 19.0F, 0.1F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(27.0F, 18.5F, 0.1F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-2.0F, 18.5F, 0.1F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(31.0F, 19.5F, 0.1F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-6.0F, 19.5F, 0.1F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(23.0F, 17.5F, 0.1F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(2.0F, 17.5F, 0.1F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(25.0F, 18.0F, 0.1F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(0.0F, 18.0F, 0.1F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static decor mesh.
    }
}
