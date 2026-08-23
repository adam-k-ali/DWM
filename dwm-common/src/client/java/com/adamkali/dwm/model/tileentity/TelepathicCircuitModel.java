// Made with Blockbench (converted from telepathic_circuit.bbmodel)
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
 * Panel2 telepathic circuit strip.
 */
public class TelepathicCircuitModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "telepathic_circuit"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/telepathic_circuit.png");

    public TelepathicCircuitModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition light = root.addOrReplaceChild(
                "light",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(5.0F, -0.8F, 0.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 2).addBox(5.0F, -1.8F, 0.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, -0.8F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 2).addBox(-2.0F, -1.8F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, -0.8F, 6.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8).addBox(-2.0F, -1.8F, 6.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, -1.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8).addBox(-1.0F, -1.8F, -1.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 10).addBox(-1.0F, -0.1F, 0.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 1.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.0F, 1.8F, -3.0F));

        PartDefinition light2 = root.addOrReplaceChild(
                "light2",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(5.0F, -0.8F, 0.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 2).addBox(5.0F, -1.8F, 0.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, -0.8F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 2).addBox(-2.0F, -1.8F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, -0.8F, 6.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8).addBox(-2.0F, -1.8F, 6.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, -1.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8).addBox(-1.0F, -1.8F, -1.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 10).addBox(-1.0F, -0.1F, 0.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 1.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 1.8F, -3.0F));

        return LayerDefinition.create(modelData, 16, 16);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Pose applied by subclasses or renderer-specific setup.
    }
}
