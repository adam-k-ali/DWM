// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class FirstDoctorTardisModel extends TardisModel {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "first_doctor_box"), "first_doctor_box");
    public static final Identifier TEXTURE_LOCATION = Identifier.of(DWMReference.MOD_ID, "textures/entity/first_doctor_box.png");


    public FirstDoctorTardisModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData bone9 = modelPartData.addChild("bone9", ModelPartBuilder.create(), ModelTransform.of(0.0F, 11.9F, -5.5F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone10 = modelPartData.addChild("bone10", ModelPartBuilder.create(), ModelTransform.of(0.0F, -0.5F, -6.3F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone11 = modelPartData.addChild("bone11", ModelPartBuilder.create(), ModelTransform.of(0.0F, -1.5F, -6.3F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone12 = modelPartData.addChild("bone12", ModelPartBuilder.create(), ModelTransform.of(-2.7F, 6.9F, -5.2F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone13 = modelPartData.addChild("bone13", ModelPartBuilder.create(), ModelTransform.of(-2.7F, 6.4F, -5.2F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone14 = modelPartData.addChild("bone14", ModelPartBuilder.create(), ModelTransform.of(1.2F, 4.6272F, -5.1728F, 0.0F, -0.7854F, 0.0F));

        ModelPartData post = modelPartData.addChild("post", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.2F, -25.5F, 0.3F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.2F, -25.5F, 12.7F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(11.2F, -25.5F, 0.3F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(11.2F, -25.5F, 12.7F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.5F, 23.0F, -7.0F));

        ModelPartData bone = post.addChild("bone", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone2 = post.addChild("bone2", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData post2 = modelPartData.addChild("post2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(6.5F, 23.0F, -7.0F));

        ModelPartData bone4 = post2.addChild("bone4", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone5 = post2.addChild("bone5", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData post3 = modelPartData.addChild("post3", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(6.5F, 23.0F, 5.0F));

        ModelPartData bone6 = post3.addChild("bone6", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone7 = post3.addChild("bone7", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData post4 = modelPartData.addChild("post4", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.6F, 0.5F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 0.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.1F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.6F, 0.9F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.6F, 1.0F, 1.0F, 25.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.5F, 23.0F, 5.0F));

        ModelPartData bone8 = post4.addChild("bone8", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone15 = post4.addChild("bone15", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData LeftDoor = modelPartData.addChild("LeftDoor", ModelPartBuilder.create().uv(0, 0).cuboid(-3.5F, 3.9F, -0.2F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(4, 33).cuboid(-4.5F, 7.9F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 29).cuboid(-4.5F, 3.9F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(7, 9).cuboid(-0.2F, -12.1F, -0.5F, 1.0F, 21.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 27).cuboid(-4.5F, 2.9F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -1.1F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-3.5F, -1.1F, -0.2F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -2.1F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-3.5F, -6.1F, -0.2F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(82, 54).cuboid(-2.35F, -4.6F, -0.3F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -6.1F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -7.1F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -11.1F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -12.1F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(-4.2F, -11.125F, -0.25F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(4.5F, 14.0F, -5.5F));

        ModelPartData window3_1 = LeftDoor.addChild("window3_1", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-8.5F, -7.1F, -0.2F, 0.3491F, 0.0F, 0.0F));

        ModelPartData window2_1 = LeftDoor.addChild("window2_1", ModelPartBuilder.create().uv(38, 48).cuboid(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.3F, -8.6357F, 0.2766F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Window1_1 = LeftDoor.addChild("Window1_1", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-8.5F, -7.4F, -0.1F, 0.7854F, 0.0F, 0.0F));

        ModelPartData rightDoor = modelPartData.addChild("rightDoor", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5F, -7.1F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(83, 44).cuboid(-0.4F, -6.0F, -0.15F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(3.5F, -6.1F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(3.5F, -11.1F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -12.1F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(-0.5F, -11.125F, -0.25F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -1.1F, -0.2F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -2.1F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(3.5F, -1.1F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 29).cuboid(3.5F, 3.9F, -0.5F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 27).cuboid(-0.5F, 2.9F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(4, 33).cuboid(-0.5F, 7.9F, -0.5F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, 3.9F, -0.2F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 10).cuboid(-0.8F, -12.1F, -0.5F, 1.0F, 21.0F, 1.0F, new Dilation(0.0F))
                .uv(66, 65).cuboid(0.15F, -2.475F, -0.35F, 3.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(66, 64).cuboid(-0.45F, -6.375F, -0.35F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(66, 65).cuboid(0.55F, -6.775F, -0.35F, 3.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(66, 65).cuboid(3.15F, -5.775F, -0.35F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-4.5F, 14.0F, -5.5F));

        ModelPartData bone3 = rightDoor.addChild("bone3", ModelPartBuilder.create().uv(0, 0).cuboid(-0.75F, -10.0F, -0.5F, 1.0F, 21.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.75F, -2.1F, 0.2F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Window1_2 = rightDoor.addChild("Window1_2", ModelPartBuilder.create().uv(38, 48).cuboid(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(68, 65).cuboid(-0.3F, 3.518F, -3.9771F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(68, 65).cuboid(-0.3F, 0.6896F, -1.1487F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(68, 65).cuboid(-0.3F, 3.5534F, -4.0125F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, -7.4F, -0.1F, 0.7854F, 0.0F, 0.0F));

        ModelPartData window2_2 = rightDoor.addChild("window2_2", ModelPartBuilder.create().uv(38, 48).cuboid(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(68, 65).cuboid(-4.177F, 2.525F, 3.177F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(68, 65).cuboid(-6.5104F, 2.525F, 5.5104F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(8.7F, -8.6357F, 0.2766F, 0.0F, -0.7854F, 0.0F));

        ModelPartData window3_2 = rightDoor.addChild("window3_2", ModelPartBuilder.create().uv(38, 48).cuboid(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, -7.1F, -0.2F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone18 = rightDoor.addChild("bone18", ModelPartBuilder.create().uv(91, 28).cuboid(-0.4036F, -0.4876F, -0.74F, 1.0F, 1.0F, 1.0F, new Dilation(-0.3F)), ModelTransform.pivot(3.8F, -1.6F, -0.2F));

        ModelPartData Main = modelPartData.addChild("Main", ModelPartBuilder.create().uv(59, 94).cuboid(-2.0F, -7.0F, -1.5F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(8.0F, -7.0F, -1.6F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, -9.25F, -0.9F, 12.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -6.35F, -0.7F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -6.1F, -0.6F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -5.75F, -0.5F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, -7.0F, -1.6F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-4.0F, 6.9F, -5.7F));

        ModelPartData bone16 = Main.addChild("bone16", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, -7.7F, -0.9F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone17 = Main.addChild("bone17", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, -7.3F, -0.9F, -0.3491F, 0.0F, 0.0F));

        ModelPartData Main2 = modelPartData.addChild("Main2", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -2.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -7.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -12.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -17.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.1F, -6.0F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(59, 94).cuboid(-6.0F, -24.1F, -7.2F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -26.35F, -6.6F, 12.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.45F, -6.4F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.2F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -22.85F, -6.2F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -16.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -11.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -6.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData bone21 = Main2.addChild("bone21", ModelPartBuilder.create().uv(0, 0).cuboid(-0.75F, -11.0F, -0.5F, 1.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone22 = Main2.addChild("bone22", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone23 = Main2.addChild("bone23", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone24 = Main2.addChild("bone24", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        ModelPartData side_windows = Main2.addChild("side_windows", ModelPartBuilder.create().uv(42, 89).cuboid(0.3F, -2.15F, -0.125F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(-5.0F, -2.15F, -0.125F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -19.0F, -5.625F));

        ModelPartData bone26 = side_windows.addChild("bone26", ModelPartBuilder.create().uv(38, 48).cuboid(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.2F, 0.3643F, 0.4016F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone25 = side_windows.addChild("bone25", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, 1.6F, 0.025F, 0.7854F, 0.0F, 0.0F));

        ModelPartData Main3 = modelPartData.addChild("Main3", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -2.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(-5.0F, -21.15F, -5.75F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -7.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -12.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -17.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.1F, -6.0F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(59, 94).cuboid(-6.0F, -24.1F, -7.2F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -26.35F, -6.6F, 12.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.45F, -6.4F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.2F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -22.85F, -6.2F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -16.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -11.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -6.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(0.3F, -21.15F, -5.75F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        ModelPartData bone27 = Main3.addChild("bone27", ModelPartBuilder.create().uv(0, 0).cuboid(-0.75F, -11.0F, -0.5F, 1.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone28 = Main3.addChild("bone28", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone29 = Main3.addChild("bone29", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone30 = Main3.addChild("bone30", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone31 = Main3.addChild("bone31", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone32 = Main3.addChild("bone32", ModelPartBuilder.create().uv(38, 48).cuboid(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Main4 = modelPartData.addChild("Main4", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -2.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(-5.0F, -20.95F, -5.75F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -6.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.1F, -5.7F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -7.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -12.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -17.1F, -6.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.1F, -6.0F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(59, 94).cuboid(-6.0F, -24.1F, -7.2F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -26.35F, -6.6F, 12.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.45F, -6.4F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.2F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -22.85F, -6.2F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -24.1F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -21.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -16.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -16.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -11.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -11.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -6.1F, -6.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -6.1F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(42, 89).cuboid(0.3F, -20.95F, -5.75F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData bone33 = Main4.addChild("bone33", ModelPartBuilder.create().uv(0, 0).cuboid(-0.75F, -11.0F, -0.5F, 1.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone34 = Main4.addChild("bone34", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.3457F, -0.8078F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone35 = Main4.addChild("bone35", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.5337F, -0.1238F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone36 = Main4.addChild("bone36", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -0.2621F, -0.1818F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone37 = Main4.addChild("bone37", ModelPartBuilder.create().uv(38, 48).cuboid(4.7F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(4.7F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -0.1943F, -0.2648F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -2.846F, 2.3868F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-0.3F, -1.5378F, 1.0787F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone38 = Main4.addChild("bone38", ModelPartBuilder.create().uv(38, 48).cuboid(-0.4293F, -2.7F, -0.5707F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.177F, -2.7F, 3.177F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-2.7627F, -2.7F, 1.7627F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.9849F, -2.7F, 0.9849F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-5.7326F, -2.7F, 4.7326F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-1.2071F, -2.7F, 0.2071F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-4.9548F, -2.7F, 3.9548F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(38, 48).cuboid(-6.5104F, -2.7F, 5.5104F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone19 = modelPartData.addChild("bone19", ModelPartBuilder.create().uv(79, 21).cuboid(-1.0F, -1.75F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(87, 20).cuboid(-1.0F, 0.25F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.55F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-7.5F, -1.0F, -7.5F, 15.0F, 1.0F, 15.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-7.0F, -1.1F, -7.0F, 14.0F, 1.0F, 14.0F, new Dilation(0.0F))
                .uv(11, 25).cuboid(-6.0F, -26.9F, -6.0F, 12.0F, 1.0F, 12.0F, new Dilation(0.0F))
                .uv(85, 15).cuboid(-0.5F, -29.51F, -0.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(81, 25).cuboid(-0.5F, -28.5F, -0.5F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 96, 96);
    }
}