// Converted from broakir.bbmodel (Blockbench skin / horse-based entity).
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

public class BroakirModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "broakir"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/broakir.png");

    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public BroakirModel(ModelPart root) {
        super(root);
        this.head = root.getChild("neck").getChild("head");
        this.tail = root.getChild("tail");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bodyPart = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 11.0F, 9.0F));

        bodyPart.addOrReplaceChild(
                "mesh_0",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-5.0F, -11.0F, -9.0F, 10.0F, 10.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 33).addBox(-4.0F, -12.0F, -10.0F, 8.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.0F, -14.0F, -21.0F, 12.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 14).addBox(-4.0F, -15.0F, -20.5F, 8.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 64).addBox(-1.0F, -13.0F, 0.6F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 64).addBox(-1.0F, -13.0F, -3.4F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 64).addBox(-1.0F, -13.0F, -7.4F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 64).addBox(-1.0F, -16.0F, -12.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 64).addBox(-1.0F, -16.0F, -16.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 64).addBox(-1.0F, -16.0F, -20.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition tailPart = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 3.0F, 11.0F, 0.523599F, 0.0F, 0.0F));

        tailPart.addOrReplaceChild(
                "mesh_1",
                CubeListBuilder.create()
                        .texOffs(22, 50).addBox(-1.5F, -1.732051F, -1.0F, 3.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leg1Part = root.addOrReplaceChild(
                "leg1",
                CubeListBuilder.create(),
                PartPose.offset(-3.0F, 10.0F, 9.0F));

        leg1Part.addOrReplaceChild(
                "mesh_2",
                CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leg2Part = root.addOrReplaceChild(
                "leg2",
                CubeListBuilder.create(),
                PartPose.offset(3.0F, 10.0F, 9.0F));

        leg2Part.addOrReplaceChild(
                "mesh_3",
                CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leg3Part = root.addOrReplaceChild(
                "leg3",
                CubeListBuilder.create(),
                PartPose.offset(-3.0F, 11.0F, -9.0F));

        leg3Part.addOrReplaceChild(
                "mesh_4",
                CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition leg4Part = root.addOrReplaceChild(
                "leg4",
                CubeListBuilder.create(),
                PartPose.offset(3.0F, 11.0F, -9.0F));

        leg4Part.addOrReplaceChild(
                "mesh_5",
                CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition neckPart = root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 6.2F, -10.0F, 1.047198F, 0.0F, 0.0F));

        neckPart.addOrReplaceChild(
                "mesh_6",
                CubeListBuilder.create()
                        .texOffs(0, 47).addBox(-2.0F, -12.5F, -0.401924F, 4.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 66).addBox(-1.0F, -16.5F, -4.401924F, 2.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition headPart = neckPart.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -10.633975F, 0.962178F, -0.523599F, 0.0F, 0.0F));

        headPart.addOrReplaceChild(
                "mesh_7",
                CubeListBuilder.create()
                        .texOffs(36, 0).addBox(-3.0F, -5.964102F, -3.133975F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(63, 65).addBox(-2.0F, -5.964102F, -8.133975F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition ear1Part = headPart.addOrReplaceChild(
                "ear1",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 13.0F, 3.0F, 0.0F, 0.0F, 0.087266F));

        ear1Part.addOrReplaceChild(
                "mesh_8",
                CubeListBuilder.create()
                        .texOffs(33, 31).addBox(-1.154964F, -21.943781F, -0.266922F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        PartDefinition ear2Part = headPart.addOrReplaceChild(
                "ear2",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 13.0F, 3.0F, 0.0F, 0.0F, -0.087266F));

        ear2Part.addOrReplaceChild(
                "mesh_9",
                CubeListBuilder.create()
                        .texOffs(33, 31).addBox(-0.845036F, -21.943781F, -0.266922F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD;
        float speed = state.walkAnimationSpeed;
        float pos = state.walkAnimationPos;
        this.leg1.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leg2.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
        this.leg3.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
        this.leg4.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.tail.xRot += Mth.cos(state.ageInTicks * 0.05F) * 0.1F;
    }
}
