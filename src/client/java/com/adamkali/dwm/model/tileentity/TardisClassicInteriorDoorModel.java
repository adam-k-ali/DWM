// Made with Blockbench (converted from Tardis_classic_doors.bbmodel)
// Exported for Minecraft version 1.17+ for Yarn

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import java.util.List;

public class TardisClassicInteriorDoorModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_classic_interior_door"), "main");
    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_classic_doors.png");

    private final ModelPart door1;
    private final ModelPart door2;

    public TardisClassicInteriorDoorModel(ModelPart root) {
        super(root);
        this.door1 = root.getChild("frame").getChild("Door1");
        this.door2 = root.getChild("frame2").getChild("Door2");
    }

    /**
     * Door leaves only ({@code Door1}, {@code Door2}); frames and jambs stay visible.
     */
    public List<ModelPart> getDoorParts() {
        return List.of(door1, door2);
    }

    /**
     * Renders frames and jambs without door leaves (for SOTO to fill the aperture first).
     */
    public void renderShell(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        List<ModelPart> doors = getDoorParts();
        for (ModelPart door : doors) {
            door.visible = false;
        }
        try {
            this.renderToBuffer(matrices, vertices, light, overlay, -1);
        } finally {
            for (ModelPart door : doors) {
                door.visible = true;
            }
        }
    }

    /**
     * Renders only door leaves, applying frame ancestor transforms so nested doors stay aligned.
     */
    public void renderDoors(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        matrices.pushPose();
        try {
            root.translateAndRotate(matrices);

            matrices.pushPose();
            ModelPart frame = root.getChild("frame");
            frame.translateAndRotate(matrices);
            door1.render(matrices, vertices, light, overlay);
            matrices.popPose();

            matrices.pushPose();
            ModelPart frame2 = root.getChild("frame2");
            frame2.translateAndRotate(matrices);
            door2.render(matrices, vertices, light, overlay);
            matrices.popPose();
        } finally {
            matrices.popPose();
        }
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition frame = modelPartData.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(10, 0).addBox(8.0F, -29.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(10.0F, -29.0F, 0.0F, 1.0F, 32.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(8.0F, -21.0F, 0.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(8.0F, -10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(8.0F, 0.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(13.0F, 29.0F, -8.0F));

        PartDefinition Door1 = frame.addOrReplaceChild("Door1", CubeListBuilder.create().texOffs(0, 22).addBox(-12.0F, 11.5F, 0.2F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(26, 16).addBox(-1.0F, 14.0F, -0.8F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(19, 0).addBox(-1.0F, 3.0F, -0.8F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(26, 26).addBox(-1.0F, 24.0F, -0.8F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 22).addBox(-12.0F, 1.0F, 0.2F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 25).addBox(-10.0F, 3.0F, -0.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 25).addBox(-10.0F, 24.0F, -0.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 25).addBox(-10.0F, 13.5F, -0.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 22).addBox(-12.0F, 22.0F, 0.2F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 0.0F, -0.8F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 10.0F, -0.8F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 20.0F, -0.8F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 31.0F, -0.8F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(-14.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(-3.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(9.0F, -29.0F, 1.0F));

        PartDefinition frame2 = modelPartData.addOrReplaceChild("frame2", CubeListBuilder.create().texOffs(10, 0).addBox(8.0F, -29.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(10.0F, -29.0F, 0.0F, 1.0F, 32.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(8.0F, -21.0F, 0.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(8.0F, -10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(8.0F, 0.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 3.0F, -8.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition Door2 = frame2.addOrReplaceChild("Door2", CubeListBuilder.create().texOffs(24, 14).addBox(-1.0F, 14.0F, -0.8F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(19, 0).addBox(-1.0F, 3.0F, -0.8F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(26, 25).addBox(-1.0F, 24.0F, -0.8F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 0.0F, -0.8F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 10.0F, -0.8F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 20.0F, -0.8F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-12.0F, 31.0F, -0.8F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(-14.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(10, 0).addBox(-3.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(20, 25).addBox(-10.0F, 3.0F, -0.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 25).addBox(-10.0F, 13.5F, -0.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 25).addBox(-10.0F, 24.0F, -0.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(9.0F, -29.0F, 1.0F));

        PartDefinition bone = Door2.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 22).addBox(-4.5F, -4.5F, -0.3F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 22).addBox(-4.5F, 6.0F, -0.3F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 22).addBox(-4.5F, -15.0F, -0.3F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.5F, 16.0F, 0.5F, 0.0F, 0.0F, -3.1416F));

        // Side jambs: extend closed mesh from 2 to 3 blocks wide (8px each side), keeping center at X=8.
        modelPartData.addOrReplaceChild(
                "jambs",
                CubeListBuilder.create()
                        .texOffs(10, 0).addBox(-16.0F, 0.0F, -8.0F, 8.0F, 32.0F, 9.2F, new CubeDeformation(0.0F))
                        .texOffs(10, 0).addBox(24.0F, 0.0F, -8.0F, 8.0F, 32.0F, 9.2F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, 32, 32);
    }

    /** Fully open yaw: 135°. */
    private static final float MAX_DOOR_YAW = (float) (3.0 * Math.PI / 4.0);

    /**
     * Door1 (right leaf when closed) and Door2 (left leaf; parent frame2 is Z-flipped)
     * both swing negative Y so they open outward in world space.
     */
    public static float door1Yaw(float doorSwingProgress) {
        return -doorSwingProgress * MAX_DOOR_YAW;
    }

    public static float door2Yaw(float doorSwingProgress) {
        return -doorSwingProgress * MAX_DOOR_YAW;
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        float doorSwingProgress = state.getDoorSwingProgress();
        this.door1.setRotation(0.0F, door1Yaw(doorSwingProgress), 0.0F);
        this.door2.setRotation(0.0F, door2Yaw(doorSwingProgress), 0.0F);
    }
}
