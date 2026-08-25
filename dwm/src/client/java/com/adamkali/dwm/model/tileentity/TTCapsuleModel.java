// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
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

public class TTCapsuleModel extends TardisModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tt_capsule"), "tt_capsule");

    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tt_capsule.png");

    private final ModelPart door;

    public TTCapsuleModel(ModelPart root) {
        super(root);
        ModelPart bone = root.getChild("bone");
        this.door = bone.getChild("door");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bone = modelPartData.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        bone.addOrReplaceChild("door", CubeListBuilder.create().texOffs(27, 37).addBox(6.5F, -13.5F, -5.0F, 1.0F, 22.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(27, 37), PartPose.offset(-0.5F, -9.5F, 0.0F));

        bone.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 11).addBox(-6.0F, -1.0F, 0.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(8.0F, -1.0F, 1.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(9.0F, -1.0F, 2.0F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-7.0F, -1.0F, 1.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-5.0F, -1.0F, -1.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-5.0F, -1.0F, 14.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, -7.0F));

        bone.addOrReplaceChild("roof", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, -2.0F, 10.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -3.0F, 0.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(8.0F, -3.0F, 1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(8.0F, -3.0F, 12.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(8.0F, -3.0F, 2.0F, 2.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -3.0F, 2.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-7.0F, -3.0F, 1.0F, 1.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -3.0F, -1.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -3.0F, 14.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -3.0F, 15.0F, 10.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -21.0F, -7.0F));

        bone.addOrReplaceChild("structure", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -21.0F, 0.0F, 10.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 0).addBox(-2.0F, -21.0F, -17.0F, 10.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 0).addBox(-6.0F, -21.0F, -13.0F, 1.0F, 21.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -21.0F, -14.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -21.0F, -15.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -21.0F, -16.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(8.0F, -21.0F, -16.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(9.0F, -21.0F, -15.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(1, 15).addBox(10.0F, -21.0F, -14.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 15).addBox(10.0F, -21.0F, -3.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(9.0F, -21.0F, -2.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(8.0F, -21.0F, -1.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -21.0F, -1.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -21.0F, -2.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -21.0F, -3.0F, 1.0F, 20.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 8.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        float doorSwingProgress = state.getDoorSwingProgress();
        this.door.setRotation(0.0F, doorSwingProgress * (float) Math.PI / 2, 0.0F);
    }
}