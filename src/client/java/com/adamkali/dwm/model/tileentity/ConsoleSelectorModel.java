package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;

/**
 * Shared dial mesh for Panel3 console selectors (biome / planet locator).
 */
public class ConsoleSelectorModel extends EntityModel<TardisRenderState> {
    public static final String PART_NAME = "console_selector";

    public ConsoleSelectorModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild(
                PART_NAME,
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-5.0F, 0.5F, -5.0F, 10.0F, 1.0F, 10.0F, new Dilation(0.0F))
                        .uv(14, 14).cuboid(5.0F, 0.0F, -5.0F, 2.0F, 1.0F, 10.0F, new Dilation(0.0F))
                        .uv(0, 12).cuboid(-7.0F, 0.0F, -5.0F, 2.0F, 1.0F, 10.0F, new Dilation(0.0F))
                        .uv(24, 25).cuboid(-5.0F, 0.0F, 5.0F, 10.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(0, 25).cuboid(-5.0F, 0.0F, -7.0F, 10.0F, 1.0F, 2.0F, new Dilation(0.0F))
                        .uv(3, 1).cuboid(-6.0F, 0.0F, 5.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(8, 21).cuboid(-6.0F, 0.0F, -6.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(25, 27).cuboid(5.0F, 0.0F, 5.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(0, 25).cuboid(5.0F, 0.0F, -6.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                        .uv(3, 1).cuboid(-4.0F, 1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.NONE);
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(TardisRenderState state) {
        // Static control mesh.
    }
}
