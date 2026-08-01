// Made with Blockbench (converted from materialisation_lever.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class MaterialisationLeverModel extends EntityModel<TardisRenderState> {
    public static final EntityModelLayer LAYER_LOCATION =
            new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "materialisation_lever"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.of(DWMReference.MOD_ID, "textures/entity/materialisation_lever.png");

    public MaterialisationLeverModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData demat = root.addChild(
                "demat",
                ModelPartBuilder.create(),
                ModelTransform.pivot(-1.0F, 0.0F, 0.0F));

        demat.addChild(
                "lever",
                ModelPartBuilder.create()
                        .uv(28, 25).cuboid(0.5F, -0.3F, -0.5F, 1.0F, 6.0F, 1.0F, new Dilation(0.0F))
                        .uv(0, 29).cuboid(-0.5F, 6.2F, -1.5F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F))
                        .uv(0, 29).cuboid(0.0F, 5.7F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.of(-0.5F, 1.0F, 0.0F, 1.047198F, 0.0F, 0.0F));

        demat.addChild(
                "panel",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-0.5F, -2.7F, -9.0F, 7.0F, 1.0F, 18.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(0.5F, -1.7F, -8.0F, 5.0F, 1.0F, 16.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(0.5F, -0.7F, -6.0F, 2.0F, 1.0F, 12.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(3.5F, -0.7F, -6.0F, 2.0F, 1.0F, 12.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(2.5F, -1.2F, 5.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(2.5F, -1.2F, -6.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(0.5F, 0.3F, -4.0F, 2.0F, 1.0F, 8.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(3.5F, 0.3F, -4.0F, 2.0F, 1.0F, 8.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(0.5F, 1.3F, -2.5F, 2.0F, 1.0F, 5.0F, new Dilation(0.0F))
                        .uv(0, 0).cuboid(3.5F, 1.3F, -2.5F, 2.0F, 1.0F, 5.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-2.5F, 2.7F, 0.0F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(TardisRenderState state) {
        // Static control mesh for v1 (no lever pitch sync).
    }
}
