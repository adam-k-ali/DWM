// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
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

public class FirstDoctorTardisModel extends TardisModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "first_doctor_box"), "first_doctor_box");
    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/first_doctor_box.png");


    public FirstDoctorTardisModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bone9 = modelPartData.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 11.9F, -5.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone10 = modelPartData.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, -6.3F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone11 = modelPartData.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.5F, -6.3F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone12 = modelPartData.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.7F, 6.9F, -5.2F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone13 = modelPartData.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.7F, 6.4F, -5.2F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone14 = modelPartData.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offsetAndRotation(1.2F, 4.6272F, -5.1728F, 0.0F, -0.7854F, 0.0F));

        PartDefinition post = modelPartData.addOrReplaceChild("post", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2F, -25.5F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2F, -25.5F, 12.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(11.2F, -25.5F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(11.2F, -25.5F, 12.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.5F, 23.0F, -7.0F));

        PartDefinition bone = post.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone2 = post.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition post2 = modelPartData.addOrReplaceChild("post2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 23.0F, -7.0F));

        PartDefinition bone4 = post2.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone5 = post2.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition post3 = modelPartData.addOrReplaceChild("post3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 23.0F, 5.0F));

        PartDefinition bone6 = post3.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone7 = post3.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition post4 = modelPartData.addOrReplaceChild("post4", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.5F, 23.0F, 5.0F));

        PartDefinition bone8 = post4.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone15 = post4.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition LeftDoor = modelPartData.addOrReplaceChild("LeftDoor", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 3.9F, -0.2F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 33).addBox(-4.5F, 7.9F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 29).addBox(-4.5F, 3.9F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 9).addBox(-0.2F, -12.1F, -0.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 27).addBox(-4.5F, 2.9F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -1.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.5F, -1.1F, -0.2F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -2.1F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.5F, -6.1F, -0.2F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 54).addBox(-2.35F, -4.6F, -0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -6.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -7.1F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -11.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -12.1F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(-4.2F, -11.125F, -0.25F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 14.0F, -5.5F));

        PartDefinition window3_1 = LeftDoor.addOrReplaceChild("window3_1", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.5F, -7.1F, -0.2F, 0.3491F, 0.0F, 0.0F));

        PartDefinition window2_1 = LeftDoor.addOrReplaceChild("window2_1", CubeListBuilder.create().texOffs(38, 48).addBox(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -8.6357F, 0.2766F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Window1_1 = LeftDoor.addOrReplaceChild("Window1_1", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.5F, -7.4F, -0.1F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rightDoor = modelPartData.addOrReplaceChild("rightDoor", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -7.1F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(83, 44).addBox(-0.4F, -6.0F, -0.15F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.5F, -6.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.5F, -11.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -12.1F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(-0.5F, -11.125F, -0.25F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -1.1F, -0.2F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, -2.1F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.5F, -1.1F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 29).addBox(3.5F, 3.9F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 27).addBox(-0.5F, 2.9F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 33).addBox(-0.5F, 7.9F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.5F, 3.9F, -0.2F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 10).addBox(-0.8F, -12.1F, -0.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 65).addBox(0.15F, -2.475F, -0.35F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 64).addBox(-0.45F, -6.375F, -0.35F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 65).addBox(0.55F, -6.775F, -0.35F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 65).addBox(3.15F, -5.775F, -0.35F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, 14.0F, -5.5F));

        PartDefinition bone3 = rightDoor.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -10.0F, -0.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, -2.1F, 0.2F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Window1_2 = rightDoor.addOrReplaceChild("Window1_2", CubeListBuilder.create().texOffs(38, 48).addBox(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 65).addBox(-0.3F, 3.518F, -3.9771F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 65).addBox(-0.3F, 0.6896F, -1.1487F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 65).addBox(-0.3F, 3.5534F, -4.0125F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -7.4F, -0.1F, 0.7854F, 0.0F, 0.0F));

        PartDefinition window2_2 = rightDoor.addOrReplaceChild("window2_2", CubeListBuilder.create().texOffs(38, 48).addBox(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 65).addBox(-4.177F, 2.525F, 3.177F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 65).addBox(-6.5104F, 2.525F, 5.5104F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7F, -8.6357F, 0.2766F, 0.0F, -0.7854F, 0.0F));

        PartDefinition window3_2 = rightDoor.addOrReplaceChild("window3_2", CubeListBuilder.create().texOffs(38, 48).addBox(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -7.1F, -0.2F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone18 = rightDoor.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(91, 28).addBox(-0.4036F, -0.4876F, -0.74F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F)), PartPose.offset(3.8F, -1.6F, -0.2F));

        PartDefinition Main = modelPartData.addOrReplaceChild("Main", CubeListBuilder.create().texOffs(59, 94).addBox(-2.0F, -7.0F, -1.5F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(8.0F, -7.0F, -1.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -9.25F, -0.9F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -6.35F, -0.7F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -6.1F, -0.6F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -5.75F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -7.0F, -1.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 6.9F, -5.7F));

        PartDefinition bone16 = Main.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -7.7F, -0.9F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone17 = Main.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -7.3F, -0.9F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Main2 = modelPartData.addOrReplaceChild("Main2", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -7.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -12.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -17.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.1F, -6.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(59, 94).addBox(-6.0F, -24.1F, -7.2F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -26.35F, -6.6F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.45F, -6.4F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.2F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -22.85F, -6.2F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -21.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -16.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -11.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -6.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone21 = Main2.addOrReplaceChild("bone21", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -11.0F, -0.5F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone22 = Main2.addOrReplaceChild("bone22", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone23 = Main2.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone24 = Main2.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition side_windows = Main2.addOrReplaceChild("side_windows", CubeListBuilder.create().texOffs(42, 89).addBox(0.3F, -2.15F, -0.125F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(-5.0F, -2.15F, -0.125F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -19.0F, -5.625F));

        PartDefinition bone26 = side_windows.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(38, 48).addBox(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, 0.3643F, 0.4016F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone25 = side_windows.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 1.6F, 0.025F, 0.7854F, 0.0F, 0.0F));

        PartDefinition Main3 = modelPartData.addOrReplaceChild("Main3", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(-5.0F, -21.15F, -5.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -7.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -12.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -17.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.1F, -6.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(59, 94).addBox(-6.0F, -24.1F, -7.2F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -26.35F, -6.6F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.45F, -6.4F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.2F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -22.85F, -6.2F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -21.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -16.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -11.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -6.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(0.3F, -21.15F, -5.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone27 = Main3.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -11.0F, -0.5F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone28 = Main3.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone29 = Main3.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone30 = Main3.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone31 = Main3.addOrReplaceChild("bone31", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone32 = Main3.addOrReplaceChild("bone32", CubeListBuilder.create().texOffs(38, 48).addBox(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Main4 = modelPartData.addOrReplaceChild("Main4", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(-5.0F, -20.95F, -5.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -7.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -12.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -17.1F, -6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.1F, -6.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(59, 94).addBox(-6.0F, -24.1F, -7.2F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -26.35F, -6.6F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.45F, -6.4F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.2F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -22.85F, -6.2F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -21.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -16.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -11.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -6.1F, -6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(42, 89).addBox(0.3F, -20.95F, -5.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone33 = Main4.addOrReplaceChild("bone33", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -11.0F, -0.5F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone34 = Main4.addOrReplaceChild("bone34", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone35 = Main4.addOrReplaceChild("bone35", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone36 = Main4.addOrReplaceChild("bone36", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone37 = Main4.addOrReplaceChild("bone37", CubeListBuilder.create().texOffs(38, 48).addBox(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone38 = Main4.addOrReplaceChild("bone38", CubeListBuilder.create().texOffs(38, 48).addBox(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone19 = modelPartData.addOrReplaceChild("bone19", CubeListBuilder.create().texOffs(79, 21).addBox(-1.0F, -1.75F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(87, 20).addBox(-1.0F, 0.25F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.55F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bb_main = modelPartData.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -1.0F, -7.5F, 15.0F, 1.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-7.0F, -1.1F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(11, 25).addBox(-6.0F, -26.9F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(85, 15).addBox(-0.5F, -29.51F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(81, 25).addBox(-0.5F, -28.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(modelData, 96, 96);
    }
}