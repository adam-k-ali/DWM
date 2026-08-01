// Made with Blockbench (converted from Tardis_classic_doors.bbmodel)
// Exported for Minecraft version 1.17+ for Yarn

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class TardisClassicInteriorDoorModel extends EntityModel<TardisRenderState> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "tardis_classic_interior_door"), "main");
    public static final Identifier TEXTURE_LOCATION = Identifier.of(DWMReference.MOD_ID, "textures/entity/tardis_classic_doors.png");

    private final ModelPart door1;
    private final ModelPart door2;

    public TardisClassicInteriorDoorModel(ModelPart root) {
        super(root);
        this.door1 = root.getChild("frame").getChild("Door1");
        this.door2 = root.getChild("frame2").getChild("Door2");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData frame = modelPartData.addChild("frame", ModelPartBuilder.create().uv(10, 0).cuboid(8.0F, -29.0F, 0.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(10.0F, -29.0F, 0.0F, 1.0F, 32.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(8.0F, -21.0F, 0.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(8.0F, -10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(8.0F, 0.0F, 0.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.pivot(13.0F, 29.0F, -8.0F));

        ModelPartData Door1 = frame.addChild("Door1", ModelPartBuilder.create().uv(0, 22).cuboid(-12.0F, 11.5F, 0.2F, 9.0F, 9.0F, 1.0F, new Dilation(0.0F)).uv(26, 16).cuboid(-1.0F, 14.0F, -0.8F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(19, 0).cuboid(-1.0F, 3.0F, -0.8F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(26, 26).cuboid(-1.0F, 24.0F, -0.8F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(0, 22).cuboid(-12.0F, 1.0F, 0.2F, 9.0F, 9.0F, 1.0F, new Dilation(0.0F)).uv(20, 25).cuboid(-10.0F, 3.0F, -0.8F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(20, 25).cuboid(-10.0F, 24.0F, -0.8F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(20, 25).cuboid(-10.0F, 13.5F, -0.8F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(0, 22).cuboid(-12.0F, 22.0F, 0.2F, 9.0F, 9.0F, 1.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 0.0F, -0.8F, 9.0F, 1.0F, 9.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 10.0F, -0.8F, 9.0F, 2.0F, 9.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 20.0F, -0.8F, 9.0F, 2.0F, 9.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 31.0F, -0.8F, 9.0F, 1.0F, 9.0F, new Dilation(0.0F)).uv(10, 0).cuboid(-14.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new Dilation(0.0F)).uv(10, 0).cuboid(-3.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new Dilation(0.0F)),
                ModelTransform.pivot(9.0F, -29.0F, 1.0F));

        ModelPartData frame2 = modelPartData.addChild("frame2", ModelPartBuilder.create().uv(10, 0).cuboid(8.0F, -29.0F, 0.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(10.0F, -29.0F, 0.0F, 1.0F, 32.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(8.0F, -21.0F, 0.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(8.0F, -10.0F, 0.0F, 2.0F, 5.0F, 2.0F, new Dilation(0.0F)).uv(10, 0).cuboid(8.0F, 0.0F, 0.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.of(3.0F, 3.0F, -8.0F, 0.0F, 0.0F, -3.1416F));

        ModelPartData Door2 = frame2.addChild("Door2", ModelPartBuilder.create().uv(24, 14).cuboid(-1.0F, 14.0F, -0.8F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(19, 0).cuboid(-1.0F, 3.0F, -0.8F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(26, 25).cuboid(-1.0F, 24.0F, -0.8F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 0.0F, -0.8F, 9.0F, 1.0F, 9.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 10.0F, -0.8F, 9.0F, 2.0F, 9.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 20.0F, -0.8F, 9.0F, 2.0F, 9.0F, new Dilation(0.0F)).uv(0, 0).cuboid(-12.0F, 31.0F, -0.8F, 9.0F, 1.0F, 9.0F, new Dilation(0.0F)).uv(10, 0).cuboid(-14.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new Dilation(0.0F)).uv(10, 0).cuboid(-3.0F, 0.0F, -0.8F, 2.0F, 32.0F, 9.0F, new Dilation(0.0F)).uv(20, 25).cuboid(-10.0F, 3.0F, -0.8F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(20, 25).cuboid(-10.0F, 13.5F, -0.8F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F)).uv(20, 25).cuboid(-10.0F, 24.0F, -0.8F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(9.0F, -29.0F, 1.0F));

        ModelPartData bone = Door2.addChild("bone", ModelPartBuilder.create().uv(0, 22).cuboid(-4.5F, -4.5F, -0.3F, 9.0F, 9.0F, 1.0F, new Dilation(0.0F)).uv(0, 22).cuboid(-4.5F, 6.0F, -0.3F, 9.0F, 9.0F, 1.0F, new Dilation(0.0F)).uv(0, 22).cuboid(-4.5F, -15.0F, -0.3F, 9.0F, 9.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-7.5F, 16.0F, 0.5F, 0.0F, 0.0F, -3.1416F));

        // Side jambs: extend closed mesh from 2 to 3 blocks wide (8px each side), keeping center at X=8.
        modelPartData.addChild(
                "jambs",
                ModelPartBuilder.create()
                        .uv(10, 0).cuboid(-16.0F, 0.0F, -8.0F, 8.0F, 32.0F, 9.2F, new Dilation(0.0F))
                        .uv(10, 0).cuboid(24.0F, 0.0F, -8.0F, 8.0F, 32.0F, 9.2F, new Dilation(0.0F)),
                ModelTransform.NONE);

        return TexturedModelData.of(modelData, 32, 32);
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
    public void setAngles(TardisRenderState state) {
        float doorSwingProgress = state.getDoorSwingProgress();
        this.door1.setAngles(0.0F, door1Yaw(doorSwingProgress), 0.0F);
        this.door2.setAngles(0.0F, door2Yaw(doorSwingProgress), 0.0F);
    }
}
