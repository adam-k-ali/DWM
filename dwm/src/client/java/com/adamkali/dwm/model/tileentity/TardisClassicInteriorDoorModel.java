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
