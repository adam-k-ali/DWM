// Made with Blockbench (converted from tardis_compact_scanner.bbmodel)
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

public class TardisCompactScannerModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_compact_scanner"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_full_scanner.png");

    public TardisCompactScannerModel(ModelPart root) {
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
                        .texOffs(1, 1).addBox(-7.5F, 9.0F, -0.5F, 15.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
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
                        .texOffs(28, 50).addBox(5.0F, 19.0F, 4.1F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(9, 50).addBox(20.0F, 19.0F, 4.1F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 48).addBox(5.0F, 18.0F, 4.6F, 16.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(5.0F, 32.0F, 0.6F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 16).addBox(5.0F, 1.0F, 0.6F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static decor mesh.
    }
}
