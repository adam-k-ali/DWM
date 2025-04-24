// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class TTCapsuleModel extends EntityModel<TTCapsuleRenderState> {
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "tardis"), "tardis");
	public static final Identifier TEXTURE_LOCATION = Identifier.of(DWMReference.MOD_ID, "textures/entity/tt_capsule.png");

	private final ModelPart door;

	public TTCapsuleModel(ModelPart root) {
        super(root);
		ModelPart bone = root.getChild("bone");
		this.door = bone.getChild("door");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create(), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		bone.addChild("door", ModelPartBuilder.create().uv(27, 37).cuboid(6.5F, -13.5F, -5.0F, 1.0F, 22.0F, 10.0F, new Dilation(0.0F))
		.uv(27, 37), ModelTransform.pivot(-0.5F, -9.5F, 0.0F));

		bone.addChild("base", ModelPartBuilder.create().uv(0, 11).cuboid(-6.0F, -1.0F, 0.0F, 14.0F, 1.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 13).cuboid(8.0F, -1.0F, 1.0F, 1.0F, 1.0F, 12.0F, new Dilation(0.0F))
		.uv(0, 10).cuboid(9.0F, -1.0F, 2.0F, 1.0F, 1.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 6).cuboid(-7.0F, -1.0F, 1.0F, 1.0F, 1.0F, 12.0F, new Dilation(0.0F))
		.uv(0, 8).cuboid(-5.0F, -1.0F, -1.0F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 25).cuboid(-5.0F, -1.0F, 14.0F, 12.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.0F, 0.0F, -7.0F));

		bone.addChild("roof", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -3.0F, -2.0F, 10.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-6.0F, -3.0F, 0.0F, 14.0F, 3.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(8.0F, -3.0F, 1.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(8.0F, -3.0F, 1.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(8.0F, -3.0F, 12.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(8.0F, -3.0F, 2.0F, 2.0F, 7.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-8.0F, -3.0F, 2.0F, 1.0F, 3.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-7.0F, -3.0F, 1.0F, 1.0F, 3.0F, 12.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-5.0F, -3.0F, -1.0F, 12.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-5.0F, -3.0F, 14.0F, 12.0F, 3.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-4.0F, -3.0F, 15.0F, 10.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.0F, -21.0F, -7.0F));

		bone.addChild("structure", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -21.0F, 0.0F, 10.0F, 21.0F, 1.0F, new Dilation(0.0F))
		.uv(7, 0).cuboid(-2.0F, -21.0F, -17.0F, 10.0F, 21.0F, 1.0F, new Dilation(0.0F))
		.uv(8, 0).cuboid(-6.0F, -21.0F, -13.0F, 1.0F, 21.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-5.0F, -21.0F, -14.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-4.0F, -21.0F, -15.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-3.0F, -21.0F, -16.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(8.0F, -21.0F, -16.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(9.0F, -21.0F, -15.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(1, 15).cuboid(10.0F, -21.0F, -14.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(4, 15).cuboid(10.0F, -21.0F, -3.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(9.0F, -21.0F, -2.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(8.0F, -21.0F, -1.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-3.0F, -21.0F, -1.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-4.0F, -21.0F, -2.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-5.0F, -21.0F, -3.0F, 1.0F, 20.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, 0.0F, 8.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	public void setAngles(TTCapsuleRenderState state) {
		float doorSwingProgress = state.getDoorSwingProgress();
		this.door.setAngles(0.0F, doorSwingProgress * (float) Math.PI / 2, 0.0F);
	}
}