// Made with Blockbench (converted from fast_return.bbmodel)
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

public class FastReturnModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "fast_return"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/fast_return.png");

    public FastReturnModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        root.addOrReplaceChild(
                "switch",
                CubeListBuilder.create()
                .texOffs(0, 1).addBox(-3.0F, 0.0F, -8.0F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-3.0F, 2.0F, 0.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(6, 11).addBox(0.5F, 2.6F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-2.5F, 2.6F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 2).addBox(-1.0F, 0.6F, 6.3F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(7, 14).addBox(-2.2F, 1.7F, -7.3F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-3.0F, 0.0F, -7.0F, 6.0F, 2.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(4, 10).addBox(-2.5F, 1.2F, -4.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 6).addBox(1.5F, 1.8F, -4.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(1.5F, 1.8F, -6.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-2.5F, 1.8F, -2.2F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(-2.0F, 1.8F, -2.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, 16, 16);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static control mesh.
    }
}
