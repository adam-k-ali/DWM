// Converted from dalek_less.bbmodel (Blockbench modded_entity, 64x64).
package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.DalekFlightFx;
import com.adamkali.dwm.render.state.DalekRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class DalekModel extends EntityModel<DalekRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek"), "main");

    private final ModelPart skirt;
    private final ModelPart head;
    private final ModelPart eyestalk;
    private final ModelPart gun;

    public DalekModel(ModelPart root) {
        super(root);
        this.skirt = root.getChild("skirt");
        this.head = root.getChild("head");
        this.eyestalk = this.head.getChild("eyestalk");
        this.gun = root.getChild("shoulders").getChild("gun");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition skirtPart = root.addOrReplaceChild(
                "skirt",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 1.0F));
        skirtPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.5F, -6.0F, -5.75F, 11.0F, 4.0F, 11.0F)
                        .texOffs(0, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 4.0F, 10.0F)
                        .texOffs(0, 0).addBox(-4.5F, -14.0F, -4.25F, 9.0F, 4.0F, 9.0F)
                        .texOffs(0, 16).addBox(-3.5F, -15.0F, -3.5F, 7.0F, 1.0F, 8.0F)
                        .texOffs(0, 0).addBox(-6.0F, -2.0F, -7.0F, 12.0F, 2.0F, 13.0F)
                        .texOffs(40, 32).addBox(-4.0F, -13.0F, -4.75F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(-1.0F, -13.0F, -4.75F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(2.0F, -13.0F, -4.75F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(-5.0F, -13.0F, -0.75F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-5.0F, -13.0F, 2.25F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-5.0F, -13.0F, -3.75F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-4.0F, -9.0F, -5.5F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(-1.0F, -9.0F, -5.5F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(2.0F, -9.0F, -5.5F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(4.0F, -13.0F, -0.75F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(4.0F, -13.0F, 2.25F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(4.0F, -13.0F, -3.75F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-2.25F, -5.0F, -6.25F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(-4.75F, -5.0F, -6.25F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(0.25F, -5.0F, -6.25F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(2.75F, -5.0F, -6.25F, 2.0F, 2.0F, 1.0F)
                        .texOffs(40, 32).addBox(-5.5F, -9.0F, -1.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-5.5F, -9.0F, 2.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-5.5F, -9.0F, -4.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(4.5F, -9.0F, -1.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(4.5F, -9.0F, 2.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(4.5F, -9.0F, -4.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-6.0F, -5.0F, 0.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-6.0F, -5.0F, 2.5F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-6.0F, -5.0F, -2.5F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(-6.0F, -5.0F, -5.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(5.0F, -5.0F, 0.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(5.0F, -5.0F, 2.5F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(5.0F, -5.0F, -2.5F, 1.0F, 2.0F, 2.0F)
                        .texOffs(40, 32).addBox(5.0F, -5.0F, -5.0F, 1.0F, 2.0F, 2.0F),
                PartPose.ZERO);

        PartDefinition shouldersPart = root.addOrReplaceChild(
                "shoulders",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 9.0F, 1.0F));
        shouldersPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 9.0F)
                        .texOffs(0, 16).addBox(-4.5F, -6.0F, -4.25F, 9.0F, 1.0F, 9.0F),
                PartPose.ZERO);

        PartDefinition gunPart = shouldersPart.addOrReplaceChild(
                "gun",
                CubeListBuilder.create(),
                PartPose.offset(-2.5F, -3.0F, -4.25F));
        gunPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(36, 16).addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 6.0F)
                        .texOffs(36, 16).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 3.0F)
                        .texOffs(36, 16).addBox(-0.5F, -0.5F, -7.0F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition plungerPart = shouldersPart.addOrReplaceChild(
                "plunger",
                CubeListBuilder.create(),
                PartPose.offset(2.5F, -3.0F, -4.25F));
        plungerPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-0.5F, -0.5F, -7.0F, 1.0F, 1.0F, 7.0F)
                        .texOffs(0, 32).addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 1.0F)
                        .texOffs(0, 32).addBox(-1.0F, -1.0F, -8.5F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition neckPart = root.addOrReplaceChild(
                "neck",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 3.0F, 1.0F));
        neckPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 1.0F, 7.0F)
                        .texOffs(0, 0).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 1.0F, 6.0F)
                        .texOffs(0, 16).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 1.0F, 7.0F),
                PartPose.ZERO);

        PartDefinition headPart = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        headPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F)
                        .texOffs(0, 0).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 2.0F, 6.0F),
                PartPose.ZERO);

        PartDefinition light_leftPart = headPart.addOrReplaceChild(
                "light_left",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.5F, -4.5F, 0.0F, 0.0F, 0.0F, 0.610865F));
        light_leftPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(48, 32).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition light_rightPart = headPart.addOrReplaceChild(
                "light_right",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.5F, -4.5F, 0.0F, 0.0F, 0.0F, -0.610865F));
        light_rightPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(48, 32).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition eyestalkPart = headPart.addOrReplaceChild(
                "eyestalk",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, -4.0F));
        eyestalkPart.addOrReplaceChild(
                "mesh",
                CubeListBuilder.create()
                        .texOffs(16, 32).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F)
                        .texOffs(16, 32).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(16, 32).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(32, 32).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);


        return LayerDefinition.create(mesh, 64, 64);
    }

    public static float bobOffset(float ageInTicks, boolean flying) {
        return DalekFlightFx.bobOffset(ageInTicks, flying);
    }

    @Override
    public void setupAnim(DalekRenderState state) {
        super.setupAnim(state);
        this.root.resetPose();
        this.head.resetPose();
        this.eyestalk.resetPose();
        this.skirt.resetPose();
        this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.35F;
        this.head.yRot += state.yRot * Mth.DEG_TO_RAD * 0.35F;
        this.eyestalk.xRot += state.xRot * Mth.DEG_TO_RAD * 0.65F;
        this.eyestalk.yRot += state.yRot * Mth.DEG_TO_RAD * 0.65F;
        this.root.y += bobOffset(state.ageInTicks, state.flying);
        this.root.xRot += state.leanPitch * Mth.DEG_TO_RAD;
        this.root.zRot += state.leanRoll * Mth.DEG_TO_RAD;
    }
}
