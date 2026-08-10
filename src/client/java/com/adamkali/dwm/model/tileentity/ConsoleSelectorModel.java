package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Shared dial mesh for Panel3 console selectors (biome / planet locator).
 */
public class ConsoleSelectorModel extends EntityModel<TardisRenderState> {
    public static final String PART_NAME = "console_selector";

    public ConsoleSelectorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        root.addOrReplaceChild(
                PART_NAME,
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, 0.5F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(14, 14).addBox(5.0F, 0.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 12).addBox(-7.0F, 0.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 25).addBox(-5.0F, 0.0F, 5.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(-5.0F, 0.0F, -7.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(3, 1).addBox(-6.0F, 0.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 21).addBox(-6.0F, 0.0F, -6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(25, 27).addBox(5.0F, 0.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(5.0F, 0.0F, -6.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(3, 1).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static control mesh.
    }
}
