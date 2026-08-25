// Made with Blockbench (converted from reader.bbmodel)
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
 * Shared 16x3x16 environment / refueler dial mesh. Texture is swapped per instrument.
 */
public class ReaderModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "reader"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/oxygen_reader.png");
    public static final Identifier OXYGEN_TEXTURE = TEXTURE_LOCATION;
    public static final Identifier PRESSURE_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/pressure_reader.png");
    public static final Identifier TEMPERATURE_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/temperature_reader.png");
    public static final Identifier REFUELER_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/refueler.png");
    /** Parked cosmetic needle for the placeholder artron gauge. */
    public static final float REFUELER_NEEDLE = 0.65F;
    /** Sweep from empty (−~69°) to full (+~69°). */
    public static final float NEEDLE_SWEEP_RAD = 2.4F;

    private final ModelPart needle;

    public ReaderModel(ModelPart root) {
        super(root);
        this.needle = root.getChild("needle");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        PartDefinition reader = root.addOrReplaceChild(
                "reader",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -2.066667F, -6.333333F, 16.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-7.0F, -2.066667F, -7.333333F, 14.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 18).addBox(-7.0F, -1.066667F, -6.333333F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 32).addBox(6.0F, -0.066667F, -5.333333F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 32).addBox(-7.0F, -0.066667F, -6.333333F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
                        .texOffs(42, 24).addBox(-6.0F, -0.066667F, -6.333333F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(42, 17).addBox(-6.0F, -0.766667F, -5.333333F, 12.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 15).addBox(-5.0F, -0.766667F, -1.333333F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(50, 12).addBox(-4.0F, -0.766667F, -0.333333F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(42, 22).addBox(-7.0F, -0.066667F, 6.666667F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.066667F, -0.666667F));

        PartDefinition needle = root.addOrReplaceChild(
                "needle",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 0.15F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.1F, 0.0F));

        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5F, 1.9F, -5.8F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, 128, 128);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        needle.yRot = (state.getNeedle() - 0.5F) * NEEDLE_SWEEP_RAD;
    }
}
