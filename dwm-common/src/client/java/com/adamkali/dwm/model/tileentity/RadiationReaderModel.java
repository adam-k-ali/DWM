// Made with Blockbench (converted from radiation_reader.bbmodel)
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
 * Panel1 bottom-row radiation reader.
 */
public class RadiationReaderModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "radiation_reader"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/radiation_reader.png");

    public static final float NEEDLE_SWEEP_RAD = 1.6F;

    private final ModelPart needle;

    public RadiationReaderModel(ModelPart root) {
        super(root);
        this.needle = root.getChild("needle");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition reader = root.addOrReplaceChild(
                "reader",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -2.066667F, -7.333333F, 14.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 18).addBox(-6.0F, -2.066667F, -8.333333F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(29, 19).addBox(-4.0F, -2.066667F, -9.333333F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(29, 20).addBox(-2.0F, -2.066667F, -10.333333F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(5.5F, -1.966667F, -6.833333F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(5.5F, -1.966667F, 7.166667F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(5.5F, -1.966667F, 0.166667F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.5F, -1.966667F, -6.833333F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.5F, -1.966667F, 7.166667F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.5F, -1.966667F, 0.166667F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(58, 38).addBox(4.0F, -1.466667F, -4.233333F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 20).addBox(-4.0F, -1.466667F, -3.833333F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(58, 38).addBox(-5.0F, -1.466667F, -4.233333F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(58, 38).addBox(-5.0F, 2.533333F, -4.233333F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.066667F, -0.666667F));

        PartDefinition bone = reader.addOrReplaceChild(
                "bone",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -1.439693F, -10.65798F, 10.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(11, 40).addBox(-5.0F, -2.439693F, -10.258F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 55).addBox(-5.0F, -3.439693F, -9.783F, 10.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(26, 57).addBox(-5.0F, -4.439693F, -9.358F, 10.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 56).addBox(-5.0F, -5.439693F, -9.358F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -0.566667F, 6.666667F, 0.436332F, 0.0F, 0.0F));

        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(68, 30).addBox(-1.0F, 1.0F, -9.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition needle = root.addOrReplaceChild(
                "needle",
                CubeListBuilder.create()
                        .texOffs(5, 1).addBox(-0.5F, -0.2F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, -4.8F));

        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        needle.xRot = (state.getNeedle() - 0.5F) * NEEDLE_SWEEP_RAD;
    }
}
