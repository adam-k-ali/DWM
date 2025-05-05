// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ThirdDoctorTardisModel extends TardisModel {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "third_doctor_box"), "third_doctor_box");
    public static final Identifier TEXTURE_LOCATION = Identifier.of(DWMReference.MOD_ID, "textures/entity/third_doctor_box.png");

    public ThirdDoctorTardisModel(ModelPart root) {
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

        ModelPartData post = modelPartData.addChild("post", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 0.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.2F, -25.0F, 0.3F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.2F, -25.0F, 12.7F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(11.2F, -25.0F, 0.3F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(11.2F, -25.0F, 12.7F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 1.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.5F, 23.0F, -7.0F));

        ModelPartData bone = post.addChild("bone", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone2 = post.addChild("bone2", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData post2 = modelPartData.addChild("post2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 0.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 1.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(6.5F, 23.0F, -7.0F));

        ModelPartData bone4 = post2.addChild("bone4", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone5 = post2.addChild("bone5", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData post3 = modelPartData.addChild("post3", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 0.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 1.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(6.5F, 23.0F, 5.0F));

        ModelPartData bone6 = post3.addChild("bone6", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone7 = post3.addChild("bone7", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData post4 = modelPartData.addChild("post4", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.5F, -24.0F, 0.5F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 0.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.4F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.1F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-0.6F, -24.0F, 0.9F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -24.0F, 1.0F, 1.0F, 24.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.5F, 23.0F, 5.0F));

        ModelPartData bone8 = post4.addChild("bone8", ModelPartBuilder.create(), ModelTransform.of(-0.8F, -12.5F, 0.7F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone15 = post4.addChild("bone15", ModelPartBuilder.create(), ModelTransform.of(-0.2F, -12.5F, 0.9F, 0.0F, 0.7854F, 0.0F));

        ModelPartData Main = modelPartData.addChild("Main", ModelPartBuilder.create().uv(6, 20).cuboid(-2.0F, -6.9F, -1.5F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(8.0F, -6.9F, -1.6F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, -8.75F, -0.9F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.5F, -6.45F, -1.2F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.5F, -6.25F, -1.0F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.5F, -6.05F, -0.9F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.5F, -5.85F, -0.8F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, -6.9F, -1.6F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-4.0F, 6.9F, -5.7F));

        ModelPartData LeftDoor = Main.addChild("LeftDoor", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 4.0F, 0.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 25).mirrored().cuboid(-5.0F, 8.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false)
                .uv(0, 10).cuboid(-5.0F, 4.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 6).cuboid(-0.7F, -12.0F, -0.3F, 1.0F, 21.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 17).cuboid(-5.0F, 3.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-5.0F, -1.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).mirrored().cuboid(-4.0F, -1.0F, 0.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)).mirrored(false)
                .uv(0, 15).cuboid(-5.0F, -2.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, -6.0F, 0.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-5.0F, -6.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 5).cuboid(-5.0F, -7.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 4).cuboid(-5.0F, -12.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(-4.7F, -11.1F, -0.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(9.0F, 7.1F, -0.3F));

        ModelPartData window3_1 = LeftDoor.addChild("window3_1", ModelPartBuilder.create(), ModelTransform.of(-9.0F, -7.1F, 0.3F, 0.3491F, 0.0F, 0.0F));

        ModelPartData window2_1 = LeftDoor.addChild("window2_1", ModelPartBuilder.create().uv(22, 19).cuboid(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.8F, -8.6357F, 0.7766F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Window1_1 = LeftDoor.addChild("Window1_1", ModelPartBuilder.create().uv(0, 26).cuboid(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 20).cuboid(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(12, 23).cuboid(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-9.0F, -7.4F, 0.4F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone19 = LeftDoor.addChild("bone19", ModelPartBuilder.create().uv(6, 29).cuboid(-0.2879F, -1.3F, -0.7121F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.5036F, -3.6876F, 0.56F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bone18 = LeftDoor.addChild("bone18", ModelPartBuilder.create().uv(10, 24).cuboid(0.7964F, -0.3876F, -0.94F, 1.0F, 1.0F, 1.0F, new Dilation(-0.2F)), ModelTransform.pivot(-5.7F, -1.6F, 0.3F));

        ModelPartData Door2 = Main.addChild("Door2", ModelPartBuilder.create().uv(0, 5).cuboid(0.0F, -7.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(10, 27).cuboid(0.1F, -6.0F, -0.25F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(4.0F, -6.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -11.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 4).cuboid(0.0F, -12.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(0.0F, -11.1F, -0.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(0.0F, -1.0F, 0.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 15).cuboid(0.0F, -2.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(4.0F, -1.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, 4.0F, -0.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 17).cuboid(0.0F, 3.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 25).cuboid(0.0F, 8.0F, -0.3F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(0.0F, 4.0F, 0.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 5).cuboid(-0.3F, -12.0F, -0.3F, 1.0F, 21.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.0F, 7.1F, -0.3F));

        ModelPartData bone3 = Door2.addChild("bone3", ModelPartBuilder.create().uv(0, 0).cuboid(-0.9621F, -9.9F, -0.7121F, 1.0F, 21.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(5.25F, -2.1F, 0.7F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Window1_2 = Door2.addChild("Window1_2", ModelPartBuilder.create().uv(2, 23).cuboid(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 20).cuboid(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(11, 17).cuboid(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, -7.4F, 0.4F, 0.7854F, 0.0F, 0.0F));

        ModelPartData window2_2 = Door2.addChild("window2_2", ModelPartBuilder.create().uv(22, 19).cuboid(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-4.4775F, 5.4F, 2.8764F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-4.4775F, 2.9F, 2.8764F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(9.2F, -8.6357F, 0.7766F, 0.0F, -0.7854F, 0.0F));

        ModelPartData window3_2 = Door2.addChild("window3_2", ModelPartBuilder.create(), ModelTransform.of(1.0F, -7.1F, 0.3F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone16 = Main.addChild("bone16", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.4397F, -0.842F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, -7.7F, -0.9F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone17 = Main.addChild("bone17", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.6276F, -0.0896F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, -7.3F, -0.9F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone20 = Main.addChild("bone20", ModelPartBuilder.create().uv(15, 25).cuboid(-0.6F, -0.6F, -0.79F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(15, 25).cuboid(1.0F, -0.6F, -0.79F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -5.25F, -0.3F, 0.0F, 0.0F, -3.1416F));

        ModelPartData Main2 = modelPartData.addChild("Main2", ModelPartBuilder.create().uv(1, 22).cuboid(-5.0F, -2.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(-5.0F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -7.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 15).cuboid(-5.0F, -12.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 5).cuboid(-5.0F, -17.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.0F, -6.3F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 20).cuboid(-6.0F, -24.0F, -7.2F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -24.0F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -25.85F, -6.6F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.55F, -6.9F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.35F, -6.7F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.15F, -6.6F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -22.95F, -6.5F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -24.0F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-5.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(4.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-1.0F, -16.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 7).cuboid(-5.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(20, 7).cuboid(4.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-1.0F, -11.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-5.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(4.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-1.0F, -6.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(0.3F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData bone21 = Main2.addChild("bone21", ModelPartBuilder.create().uv(0, 0).cuboid(-0.9621F, -10.9F, -0.7121F, 1.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone22 = Main2.addChild("bone22", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.4397F, -0.842F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone23 = Main2.addChild("bone23", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.6276F, -0.0896F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone24 = Main2.addChild("bone24", ModelPartBuilder.create(), ModelTransform.of(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone25 = Main2.addChild("bone25", ModelPartBuilder.create().uv(0, 26).cuboid(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone26 = Main2.addChild("bone26", ModelPartBuilder.create().uv(22, 19).cuboid(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Main3 = modelPartData.addChild("Main3", ModelPartBuilder.create().uv(1, 22).cuboid(-5.0F, -2.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(-5.0F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -7.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 15).cuboid(-5.0F, -12.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 5).cuboid(-5.0F, -17.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.0F, -6.3F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 20).cuboid(-6.0F, -24.0F, -7.2F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -24.0F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -25.85F, -6.6F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.55F, -6.9F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.35F, -6.7F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.15F, -6.6F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -22.95F, -6.5F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -24.0F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-5.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(4.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-1.0F, -16.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 7).cuboid(-5.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(20, 7).cuboid(4.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-1.0F, -11.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-5.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(4.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-1.0F, -6.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(0.3F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        ModelPartData bone27 = Main3.addChild("bone27", ModelPartBuilder.create().uv(0, 0).cuboid(-0.9621F, -10.9F, -0.7121F, 1.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone28 = Main3.addChild("bone28", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.4397F, -0.842F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone29 = Main3.addChild("bone29", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.6276F, -0.0896F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone30 = Main3.addChild("bone30", ModelPartBuilder.create(), ModelTransform.of(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone31 = Main3.addChild("bone31", ModelPartBuilder.create().uv(0, 26).cuboid(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone32 = Main3.addChild("bone32", ModelPartBuilder.create().uv(22, 19).cuboid(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        ModelPartData Main4 = modelPartData.addChild("Main4", ModelPartBuilder.create().uv(1, 22).cuboid(-5.0F, -2.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(-5.0F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(1.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -6.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -11.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -16.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -7.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 15).cuboid(-5.0F, -12.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 5).cuboid(-5.0F, -17.0F, -6.3F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -23.0F, -6.3F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(6, 20).cuboid(-6.0F, -24.0F, -7.2F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.0F, -24.0F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -25.85F, -6.6F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.55F, -6.9F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.35F, -6.7F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -23.15F, -6.6F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -22.95F, -6.5F, 11.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -24.0F, -7.3F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(4.3F, -21.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.0F, -21.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-5.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(4.3F, -16.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 8).cuboid(-1.0F, -16.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 7).cuboid(-5.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(20, 7).cuboid(4.3F, -11.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-1.0F, -11.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-5.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(4.3F, -6.0F, -6.3F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-1.0F, -6.0F, -6.3F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 15).cuboid(0.3F, -21.1F, -6.08F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData bone33 = Main4.addChild("bone33", ModelPartBuilder.create().uv(0, 0).cuboid(-0.9621F, -10.9F, -0.7121F, 1.0F, 22.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -12.1F, -5.3F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone39 = Main4.addChild("bone39", ModelPartBuilder.create(), ModelTransform.of(-0.0071F, -22.5F, -5.7071F, 0.6981F, -0.7854F, -0.5236F));

        ModelPartData bone34 = Main4.addChild("bone34", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.4397F, -0.842F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.8F, -6.6F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone35 = Main4.addChild("bone35", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, 0.6276F, -0.0896F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.4F, -6.6F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone36 = Main4.addChild("bone36", ModelPartBuilder.create(), ModelTransform.of(-4.0F, -17.1F, -5.7F, 0.3491F, 0.0F, 0.0F));

        ModelPartData bone37 = Main4.addChild("bone37", ModelPartBuilder.create().uv(0, 26).cuboid(4.7F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(4.7F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(4.7F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -0.1943F, -0.6891F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -2.952F, 2.0686F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(-0.3F, -1.5378F, 0.6544F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -17.4F, -5.6F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone38 = Main4.addChild("bone38", ModelPartBuilder.create().uv(22, 19).cuboid(-0.6414F, -2.4F, -0.7828F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-4.3891F, -2.4F, 2.9648F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.9749F, -2.4F, 1.5506F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-2.1971F, -2.4F, 0.7728F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.9447F, -2.4F, 4.5205F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-1.4192F, -2.4F, -0.005F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-5.1669F, -2.4F, 3.7426F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 19).cuboid(-6.7225F, -2.4F, 5.2983F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(4.2F, -18.6357F, -5.2234F, 0.0F, -0.7854F, 0.0F));

        ModelPartData bone40 = modelPartData.addChild("bone40", ModelPartBuilder.create().uv(20, 29).cuboid(-0.5F, -0.25F, -0.5F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(28, 26).cuboid(-0.5F, -1.25F, -0.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -4.05F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-6.0F, -26.4F, -6.0F, 12.0F, 3.0F, 12.0F, new Dilation(0.0F))
                .uv(24, 23).cuboid(-1.0F, -27.1F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(24, 28).cuboid(-1.0F, -29.1F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.5F, -26.45F, -5.5F, 11.0F, 3.0F, 11.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-5.0F, -26.5F, -5.0F, 10.0F, 3.0F, 10.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.5F, -26.55F, -4.5F, 9.0F, 3.0F, 9.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-4.0F, -26.6F, -4.0F, 8.0F, 3.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-3.5F, -26.65F, -3.5F, 7.0F, 3.0F, 7.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-3.0F, -26.7F, -3.0F, 6.0F, 3.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.5F, -26.75F, -2.5F, 5.0F, 3.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, -26.8F, -2.0F, 4.0F, 3.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-1.5F, -26.85F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

}