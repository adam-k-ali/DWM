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

public class SeventhDoctorTardisModel extends TardisModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "seventh_doctor_box"), "seventh_doctor_box");
    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/seventh_doctor_box.png");


    public SeventhDoctorTardisModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition Main = modelPartData.addOrReplaceChild("Main", CubeListBuilder.create().texOffs(6, 20).addBox(-2.0F, -6.9F, -1.6F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 13).addBox(-1.5F, -7.4F, -1.2F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(10.0F, -7.4F, 0.2F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -7.4F, 0.2F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -7.4F, 11.6F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -8.35F, -0.9F, 13.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -6.25F, -1.1F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -5.95F, -1.0F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -5.55F, -0.9F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 6.9F, -5.7F));

        PartDefinition LeftDoor = Main.addOrReplaceChild("LeftDoor", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 4.0F, 0.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-5.0F, 8.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 20).addBox(-5.0F, 4.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.7F, -12.0F, -0.3F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-5.0F, 3.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-5.0F, -1.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 8).mirror().addBox(-4.0F, -1.0F, 0.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 16).mirror().addBox(-5.0F, -2.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).addBox(-4.0F, -6.0F, 0.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-5.0F, -6.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 5).mirror().addBox(-5.0F, -7.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).addBox(-5.0F, -11.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-5.0F, -12.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(-4.775F, -11.1F, -0.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(9.0F, 7.1F, -0.3F));

        PartDefinition window3_1 = LeftDoor.addOrReplaceChild("window3_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.0F, -7.1F, 0.3F, 0.3491F, 0.0F, 0.0F));

        PartDefinition window2_1 = LeftDoor.addOrReplaceChild("window2_1", CubeListBuilder.create().texOffs(22, 19).addBox(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, -8.6357F, 0.7766F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Window1_1 = LeftDoor.addOrReplaceChild("Window1_1", CubeListBuilder.create().texOffs(0, 26).addBox(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 20).addBox(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 23).addBox(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -7.4F, 0.4F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone18 = LeftDoor.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(11, 14).addBox(0.8464F, -3.3876F, -0.94F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(-5.7F, -1.6F, 0.3F));

        PartDefinition bone40 = LeftDoor.addOrReplaceChild("bone40", CubeListBuilder.create().texOffs(6, 26).addBox(-0.0581F, 0.0876F, -0.3409F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8786F, -3.6876F, 0.485F, 0.0F, 0.7854F, 0.0F));

        PartDefinition Door2 = Main.addOrReplaceChild("Door2", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -7.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 27).addBox(0.1F, -6.0F, 0.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(4.0F, -6.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, -11.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(0.0F, -12.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(-0.075F, -11.1F, -0.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 7).addBox(0.0F, -1.0F, 0.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(0.0F, -2.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(4.0F, -1.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 20).addBox(4.0F, 4.0F, -0.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(0.0F, 3.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).mirror().addBox(0.0F, 8.0F, -0.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(7, 15).addBox(0.0F, 4.0F, 0.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.3F, -12.0F, -0.3F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.35F, -2.4F, -0.2F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.05F, -6.3F, -0.2F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.05F, -6.7F, -0.2F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.65F, -5.7F, -0.2F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 7.1F, -0.3F));

        PartDefinition bone3 = Door2.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(19, 0).addBox(-1.1036F, -9.9F, -0.8536F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.25F, -2.1F, 0.7F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Window1_2 = Door2.addOrReplaceChild("Window1_2", CubeListBuilder.create().texOffs(2, 23).addBox(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-0.3F, 3.3766F, -4.26F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 20).addBox(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-0.3F, 0.5482F, -1.4315F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 17).addBox(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -7.4F, 0.4F, 0.7854F, 0.0F, 0.0F));

        PartDefinition window2_2 = Door2.addOrReplaceChild("window2_2", CubeListBuilder.create().texOffs(22, 19).addBox(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-4.3891F, 2.6F, 2.9648F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-6.7225F, 2.6F, 5.2983F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2F, -8.6357F, 0.7766F, 0.0F, -0.7854F, 0.0F));

        PartDefinition window3_2 = Door2.addOrReplaceChild("window3_2", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, -7.1F, 0.3F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone19 = Door2.addOrReplaceChild("bone19", CubeListBuilder.create().texOffs(6, 29).addBox(0.0834F, -0.1124F, -0.1995F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2214F, -4.2876F, 0.685F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone16 = Main.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3055F, -0.936F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -7.7F, -0.9F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone17 = Main.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.7618F, -0.1835F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -7.3F, -0.9F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone20 = Main.addOrReplaceChild("bone20", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, -5.25F, -0.3F, 0.0F, 0.0F, -3.1416F));

        PartDefinition bone9 = modelPartData.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 11.9F, -5.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone10 = modelPartData.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, -6.3F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone11 = modelPartData.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.5F, -6.3F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone12 = modelPartData.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.7F, 6.9F, -5.2F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone13 = modelPartData.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.7F, 6.4F, -5.2F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone14 = modelPartData.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offsetAndRotation(1.2F, 4.6272F, -5.1728F, 0.0F, -0.7854F, 0.0F));

        PartDefinition post = modelPartData.addOrReplaceChild("post", CubeListBuilder.create().texOffs(19, 0).addBox(-1.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 0.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -23.5F, 0.1F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.2F, -23.9F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.2F, -23.9F, 12.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(11.2F, -23.9F, 0.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(11.2F, -23.9F, 12.7F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -22.5F, 0.9F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -22.5F, 0.1F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -23.5F, 0.9F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 1.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.5F, 23.0F, -7.0F));

        PartDefinition bone = post.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone2 = post.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition post2 = modelPartData.addOrReplaceChild("post2", CubeListBuilder.create().texOffs(19, 0).addBox(-1.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 0.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -22.5F, 0.1F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -23.5F, 0.9F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -23.5F, 0.1F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -22.5F, 0.9F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 1.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 23.0F, -7.0F));

        PartDefinition bone4 = post2.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone5 = post2.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition post3 = modelPartData.addOrReplaceChild("post3", CubeListBuilder.create().texOffs(19, 0).addBox(-1.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 0.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -23.5F, 0.1F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -22.5F, 0.9F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -22.5F, 0.1F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -23.5F, 0.9F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 1.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, 23.0F, 5.0F));

        PartDefinition bone6 = post3.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone7 = post3.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition post4 = modelPartData.addOrReplaceChild("post4", CubeListBuilder.create().texOffs(19, 0).addBox(-1.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.5F, -23.5F, 0.5F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 0.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -22.5F, 0.1F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.4F, -23.5F, 0.9F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -23.5F, 0.1F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-0.6F, -22.5F, 0.9F, 1.0F, 23.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 0).addBox(-1.0F, -23.5F, 1.0F, 1.0F, 24.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.5F, 23.0F, 5.0F));

        PartDefinition bone8 = post4.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone15 = post4.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        PartDefinition Main2 = modelPartData.addOrReplaceChild("Main2", CubeListBuilder.create().texOffs(0, 8).addBox(-5.0F, -2.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(-5.0F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 8).addBox(1.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 8).addBox(1.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 8).addBox(-5.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(19, 8).addBox(-5.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 6).addBox(-5.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-5.0F, -7.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(-5.0F, -12.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 5).addBox(-5.0F, -17.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.0F, -6.3F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 20).addBox(-6.0F, -24.0F, -7.3F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, -25.45F, -6.6F, 13.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -23.35F, -6.8F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -23.05F, -6.7F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -22.65F, -6.6F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -21.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-5.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(4.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-1.0F, -16.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-5.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 7).addBox(4.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-1.0F, -11.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-5.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(4.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-1.0F, -6.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(0.3F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone21 = Main2.addOrReplaceChild("bone21", CubeListBuilder.create().texOffs(0, 2).addBox(-0.9621F, -10.9F, -0.7121F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone22 = Main2.addOrReplaceChild("bone22", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3055F, -0.936F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone23 = Main2.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.7618F, -0.1835F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone24 = Main2.addOrReplaceChild("bone24", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone25 = Main2.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(0, 26).addBox(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone26 = Main2.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(22, 19).addBox(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Main3 = modelPartData.addOrReplaceChild("Main3", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -2.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(-5.0F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 15).addBox(1.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 7).addBox(-5.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 8).addBox(-5.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 0).addBox(-5.0F, -7.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-5.0F, -12.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 5).addBox(-5.0F, -17.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.0F, -6.3F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 20).addBox(-6.0F, -24.0F, -7.3F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, -25.45F, -6.6F, 13.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -23.35F, -6.8F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -23.05F, -6.7F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -22.65F, -6.6F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -21.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-5.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(4.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-1.0F, -16.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-5.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 7).addBox(4.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-1.0F, -11.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-5.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 13).addBox(4.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-1.0F, -6.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(0.3F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone27 = Main3.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(19, 0).addBox(-0.9621F, -10.9F, -0.7121F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone28 = Main3.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3055F, -0.936F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone29 = Main3.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.7618F, -0.1835F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone30 = Main3.addOrReplaceChild("bone30", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone31 = Main3.addOrReplaceChild("bone31", CubeListBuilder.create().texOffs(0, 26).addBox(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone32 = Main3.addOrReplaceChild("bone32", CubeListBuilder.create().texOffs(22, 19).addBox(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        PartDefinition Main4 = modelPartData.addOrReplaceChild("Main4", CubeListBuilder.create().texOffs(3, 22).addBox(-5.0F, -2.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(-5.0F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(1.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 15).addBox(1.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 8).addBox(1.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 10).addBox(-5.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 15).addBox(-5.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 8).addBox(-5.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-5.0F, -7.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-5.0F, -12.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 5).addBox(-5.0F, -17.0F, -6.3F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -23.0F, -6.3F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 20).addBox(-6.0F, -24.0F, -7.3F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, -25.45F, -6.6F, 13.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -23.35F, -6.8F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -23.05F, -6.7F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.5F, -22.65F, -6.6F, 11.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -21.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 10).addBox(-5.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(13, 15).addBox(4.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 14).addBox(-1.0F, -16.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-5.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 21).addBox(4.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21).addBox(-1.0F, -11.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-5.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 23).addBox(4.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 21).addBox(-1.0F, -6.0F, -6.3F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 15).addBox(0.3F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone33 = Main4.addOrReplaceChild("bone33", CubeListBuilder.create().texOffs(0, 0).addBox(-0.9621F, -10.9F, -0.7121F, 1.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone39 = Main4.addOrReplaceChild("bone39", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0071F, -22.5F, -5.7071F, 0.6981F, -0.7854F, -0.5236F));

        PartDefinition bone34 = Main4.addOrReplaceChild("bone34", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.3055F, -0.936F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone35 = Main4.addOrReplaceChild("bone35", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.7618F, -0.1835F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone36 = Main4.addOrReplaceChild("bone36", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition bone37 = Main4.addOrReplaceChild("bone37", CubeListBuilder.create().texOffs(0, 26).addBox(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone38 = Main4.addOrReplaceChild("bone38", CubeListBuilder.create().texOffs(22, 19).addBox(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 19).addBox(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone41 = modelPartData.addOrReplaceChild("bone41", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, 26.1F, -13.0F, 7.0F, 1.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).mirror().addBox(-1.0F, 26.1F, -13.0F, 8.0F, 1.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).addBox(0.5F, 0.7F, -11.5F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, 0.6F, -11.0F, 11.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, 0.5F, -10.5F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(2.0F, 0.4F, -10.0F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(2.5F, 0.3F, -9.5F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.0F, 0.2F, -9.0F, 7.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.5F, 0.1F, -8.5F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, 0.0F, -8.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.5F, -0.3F, -7.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.5F, -3.1F, 5.5F));

        PartDefinition bone42 = bone41.addOrReplaceChild("bone42", CubeListBuilder.create().texOffs(24, 28).addBox(-1.0F, -1.6167F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.225F)), PartPose.offsetAndRotation(6.5F, -0.5333F, -5.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone43 = bone42.addOrReplaceChild("bone43", CubeListBuilder.create().texOffs(28, 26).addBox(-0.5F, -1.85F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F))
                .texOffs(20, 29).addBox(-0.5F, -0.45F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 0.1083F, 0.0F, 0.0F, 0.7854F, 0.0F));
        return LayerDefinition.create(modelData, 32, 32);
    }

}