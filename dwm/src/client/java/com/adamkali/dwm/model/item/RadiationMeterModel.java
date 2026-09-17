package com.adamkali.dwm.model.item;

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
 * Chunky handheld radiation meter with a raised screen and industrial controls.
 */
public class RadiationMeterModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "radiation_meter"),
                    "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/radiation_meter.png");

    public RadiationMeterModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, 3.0F, -2.0F, 10.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "grip",
                CubeListBuilder.create()
                        .texOffs(30, 0)
                        .addBox(-3.0F, 0.0F, -1.5F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "sensor",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .addBox(-3.0F, 15.0F, -1.5F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "screen_bezel",
                CubeListBuilder.create()
                        .texOffs(20, 18)
                        .addBox(-4.0F, 9.0F, -2.5F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "screen_glass",
                CubeListBuilder.create()
                        .texOffs(40, 18)
                        .addBox(-3.5F, 9.5F, -2.76F, 7.0F, 4.0F, 0.5F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "side_knob",
                CubeListBuilder.create()
                        .texOffs(0, 26)
                        .addBox(5.0F, 5.0F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "button",
                CubeListBuilder.create()
                        .texOffs(10, 26)
                        .addBox(1.5F, 5.0F, -2.35F, 2.0F, 2.0F, 0.5F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        CubeListBuilder grille = CubeListBuilder.create();
        for (int i = 0; i < 4; i++) {
            grille.texOffs(20, 26)
                    .addBox(-3.5F + i * 2.0F, 5.0F, -2.3F, 1.0F, 3.0F, 0.35F, new CubeDeformation(0.0F));
        }
        root.addOrReplaceChild("sensor_grille", grille, PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static item mesh.
    }
}
