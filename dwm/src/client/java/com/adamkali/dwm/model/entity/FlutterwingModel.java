// Converted from flutterwing.bbmodel (Blockbench bat-based entity).
package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class FlutterwingModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "flutterwing"), "main");

    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;

    public FlutterwingModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        ModelPart body = root.getChild("body");
        this.leftWing = body.getChild("leftWing");
        this.rightWing = body.getChild("rightWing");
        this.leftWingTip = this.leftWing.getChild("leftWingTip");
        this.rightWingTip = this.rightWing.getChild("rightWingTip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition headPart = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 20.0F, 1.0F));

        headPart.addOrReplaceChild(
                "mesh_0",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition rightEarPart = headPart.addOrReplaceChild(
                "rightEar",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 4.0F, -1.0F));

        rightEarPart.addOrReplaceChild(
                "mesh_1",
                CubeListBuilder.create()
                        .texOffs(24, 16).addBox(1.0F, -3.0F, 1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leftEarPart = headPart.addOrReplaceChild(
                "leftEar",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 4.0F, -1.0F));

        leftEarPart.addOrReplaceChild(
                "mesh_2",
                CubeListBuilder.create()
                        .texOffs(24, 16).addBox(-4.0F, -3.0F, 1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition bodyPart = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -0.523599F, 0.0F, 0.0F));

        bodyPart.addOrReplaceChild(
                "mesh_3",
                CubeListBuilder.create()
                        .texOffs(6, 6).addBox(-3.0F, -21.0F, -3.0F, 6.0F, 17.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        bodyPart.addOrReplaceChild(
                "mesh_4",
                CubeListBuilder.create()
                        .texOffs(24, 10).addBox(-2.0F, -23.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leftWingPart = bodyPart.addOrReplaceChild(
                "leftWing",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        leftWingPart.addOrReplaceChild(
                "mesh_5",
                CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-12.0F, -16.0F, 1.5F, 10.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        leftWingPart.addOrReplaceChild(
                "mesh_6",
                CubeListBuilder.create()
                        .texOffs(3, 38).addBox(-11.0F, -18.0F, 1.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leftWingTipPart = leftWingPart.addOrReplaceChild(
                "leftWingTip",
                CubeListBuilder.create(),
                PartPose.offset(-12.0F, -1.0F, 1.5F));

        leftWingTipPart.addOrReplaceChild(
                "mesh_7",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-8.0F, -10.0F, 0.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        leftWingTipPart.addOrReplaceChild(
                "mesh_8",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(-6.0F, -11.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition rightWingPart = bodyPart.addOrReplaceChild(
                "rightWing",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        rightWingPart.addOrReplaceChild(
                "mesh_9",
                CubeListBuilder.create()
                        .texOffs(0, 23).addBox(2.0F, -16.0F, 1.5F, 10.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        rightWingPart.addOrReplaceChild(
                "mesh_10",
                CubeListBuilder.create()
                        .texOffs(3, 38).addBox(3.0F, -18.0F, 1.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition rightWingTipPart = rightWingPart.addOrReplaceChild(
                "rightWingTip",
                CubeListBuilder.create(),
                PartPose.offset(12.0F, -1.0F, 1.5F));

        rightWingTipPart.addOrReplaceChild(
                "mesh_11",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(0.0F, -10.0F, 0.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        rightWingTipPart.addOrReplaceChild(
                "mesh_12",
                CubeListBuilder.create()
                        .texOffs(22, 23).addBox(0.0F, -11.0F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
        float flap = Mth.cos(state.ageInTicks * 74.48451F * Mth.DEG_TO_RAD) * Mth.PI * 0.25F;
        this.rightWing.yRot = flap;
        this.leftWing.yRot = -flap;
        this.rightWingTip.yRot = flap * 0.5F;
        this.leftWingTip.yRot = -flap * 0.5F;
    }
}
