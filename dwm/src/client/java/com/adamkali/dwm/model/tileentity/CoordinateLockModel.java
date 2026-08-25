// Made with Blockbench (converted from Coordinate_lock.bbmodel)
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
 * Panel3 bottom-row coordinate lock.
 */
public class CoordinateLockModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "coordinate_lock"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/coordinate_lock.png");

    public static final float SWITCH_PITCH_UNLOCKED = 0.0F;
    public static final float SWITCH_PITCH_LOCKED = 0.6F;

    private final ModelPart switchX;
    private final ModelPart switchY;
    private final ModelPart switchZ;

    public CoordinateLockModel(ModelPart root) {
        super(root);
        this.switchX = root.getChild("coord_button_x").getChild("switch");
        this.switchY = root.getChild("coord_button_y").getChild("switch3");
        this.switchZ = root.getChild("coord_button_z").getChild("switch2");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        root.addOrReplaceChild(
                "x",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(8.0F, 0.4F, -11.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 13).addBox(0.5F, 0.4F, -11.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 13).addBox(-7.0F, 0.4F, -11.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-7.0F, 0.0F, -7.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(13.0F, 0.0F, -13.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-7.0F, 0.0F, -13.0F, 20.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(5.4F, 0.0F, -11.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.3F, 0.0F, -11.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-18.0F, 0.0F, -13.0F, 11.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition light = root.addOrReplaceChild(
                "light",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(4.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.0F, -0.3F, 1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(0.4F, 0.0F, 1.4F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(2.4F, 0.0F, 1.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 0.8F, -3.0F));

        PartDefinition light2 = root.addOrReplaceChild(
                "light2",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(4.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.0F, -0.3F, 1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(0.4F, 0.0F, 1.4F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(2.4F, 0.0F, 1.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.1F, 0.8F, -3.0F));

        PartDefinition light3 = root.addOrReplaceChild(
                "light3",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(4.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.0F, -0.3F, 1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(0.4F, 0.0F, 1.4F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(2.4F, 0.0F, 1.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-11.1F, 0.8F, -3.0F));

        PartDefinition light4 = root.addOrReplaceChild(
                "light4",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(4.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.0F, -0.3F, 1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(0.4F, 0.0F, 1.4F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(2.4F, 0.0F, 1.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-17.2F, 0.8F, -3.0F));

        PartDefinition light5 = root.addOrReplaceChild(
                "light5",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(4.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.0F, -0.3F, 1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(0.4F, 0.0F, 1.4F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(2.4F, 0.0F, 1.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(7.2F, 0.8F, -3.0F));

        PartDefinition light6 = root.addOrReplaceChild(
                "light6",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(4.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(0.0F, -0.8F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.0F, -0.8F, 0.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 6).addBox(0.0F, -0.3F, 1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(0.4F, 0.0F, 1.4F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 10).addBox(2.4F, 0.0F, 1.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(13.3F, 0.8F, -3.0F));

        PartDefinition coord_button_x = root.addOrReplaceChild(
                "coord_button_x",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(-1.0F, -0.75F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(17.0F, 1.75F, -9.0F));

        PartDefinition switchXPart = coord_button_x.addOrReplaceChild(
                "switch",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.25F, 0.0F));

        PartDefinition coord_button_y = root.addOrReplaceChild(
                "coord_button_y",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(-1.0F, -0.75F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-11.0F, 1.75F, -9.0F));

        PartDefinition switchYPart = coord_button_y.addOrReplaceChild(
                "switch3",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.25F, 0.0F));

        PartDefinition coord_button_z = root.addOrReplaceChild(
                "coord_button_z",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(-1.0F, -0.75F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-15.0F, 1.75F, -9.0F));

        PartDefinition switchZPart = coord_button_z.addOrReplaceChild(
                "switch2",
                CubeListBuilder.create()
                        .texOffs(8, 13).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.25F, 0.0F));

        return LayerDefinition.create(modelData, 16, 16);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        switchX.xRot = state.isLockX() ? SWITCH_PITCH_LOCKED : SWITCH_PITCH_UNLOCKED;
        switchY.xRot = state.isLockY() ? SWITCH_PITCH_LOCKED : SWITCH_PITCH_UNLOCKED;
        switchZ.xRot = state.isLockZ() ? SWITCH_PITCH_LOCKED : SWITCH_PITCH_UNLOCKED;
    }
}
