// Hand-built monitor mesh; cubes and texture are generated together so UVs match 1:1.
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * Compact TARDIS monitor: roundel cabinet on a plinth, steel neck, bezelled screen housing with hood.
 * Fits the 1×1×2 {@code TardisDecorShapes.COMPACT_SCANNER} footprint.
 *
 * <p>Cubes are authored in entity (Y-down) space under a {@code monitor} part rotated 180° about Z,
 * so the floor-standing BER (Y-up, no flip) shows textures in the standard entity UV orientation.
 */
public class TardisCompactScannerModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_compact_scanner"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_compact_scanner.png");
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;
    /** Centre of a reserved 2×2 white texel, used to tint the screen glow quad. */
    public static final float GLOW_U = 127.0F / TEXTURE_WIDTH;
    public static final float GLOW_V = 63.0F / TEXTURE_HEIGHT;
    /** Screen glass: 14×12 px on the housing front. */
    public static final MonitorScreenRect SCREEN = new MonitorScreenRect(-7.0F, 7.0F, 18.0F, 30.0F, -5.0F);

    public TardisCompactScannerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        PartDefinition monitor = root.addOrReplaceChild(
                "monitor",
                CubeListBuilder.create(),
                PartPose.rotation(0.0F, 0.0F, (float) Math.PI));

        monitor.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-8.0F, -2.0F, -7.0F, 16.0F, 2.0F, 14.0F)
                        .texOffs(56, 0).addBox(-7.0F, -14.0F, -6.0F, 14.0F, 12.0F, 12.0F)
                        .texOffs(68, 26).addBox(-2.0F, -17.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.ZERO);
        monitor.addOrReplaceChild(
                "screen",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -31.0F, -5.0F, 16.0F, 14.0F, 12.0F),
                PartPose.ZERO);
        monitor.addOrReplaceChild(
                "bezel",
                CubeListBuilder.create()
                        .texOffs(6, 42).addBox(-8.0F, -31.0F, -6.0F, 16.0F, 1.0F, 1.0F)
                        .texOffs(40, 42).addBox(-8.0F, -18.0F, -6.0F, 16.0F, 1.0F, 1.0F)
                        .texOffs(60, 26).addBox(-8.0F, -30.0F, -6.0F, 1.0F, 12.0F, 1.0F)
                        .texOffs(64, 26).addBox(7.0F, -30.0F, -6.0F, 1.0F, 12.0F, 1.0F)
                        .texOffs(84, 26).addBox(-8.0F, -32.0F, -7.0F, 16.0F, 1.0F, 2.0F),
                PartPose.ZERO);
        monitor.addOrReplaceChild(
                "controls",
                CubeListBuilder.create()
                        .texOffs(120, 26).addBox(-5.0F, -11.0F, -7.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(0, 42).addBox(3.0F, -11.0F, -7.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(74, 42).addBox(-1.0F, -12.0F, -7.0F, 2.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static mesh; the screen content is drawn by the BER.
    }
}
