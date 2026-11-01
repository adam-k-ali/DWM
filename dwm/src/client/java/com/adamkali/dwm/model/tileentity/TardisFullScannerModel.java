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
 * Full TARDIS monitor bank: roundel cabinet and desk, large centre screen flanked by two wing screens,
 * keyboard, buttons and knobs. Fits the 3-wide {@code TardisDecorShapes.FULL_SCANNER_NORTH} footprint.
 *
 * <p>Cubes are authored in entity (Y-down) space under a {@code monitor} part rotated 180° about Z,
 * so the floor-standing BER (Y-up, no flip) shows textures in the standard entity UV orientation.
 */
public class TardisFullScannerModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_full_scanner"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_full_scanner.png");
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;
    /** Centre of a reserved 2×2 white texel, used to tint the screen glow quad. */
    public static final float GLOW_U = 127.0F / TEXTURE_WIDTH;
    public static final float GLOW_V = 127.0F / TEXTURE_HEIGHT;
    /** Centre screen glass: 18×14 px. */
    public static final MonitorScreenRect SCREEN = new MonitorScreenRect(-9.0F, 9.0F, 17.0F, 31.0F, -4.0F);
    /** Viewer's left wing screen (rendered +X): 10×10 px. */
    public static final MonitorScreenRect LEFT_WING = new MonitorScreenRect(12.0F, 22.0F, 17.0F, 27.0F, -2.0F);
    /** Viewer's right wing screen (rendered -X): 10×10 px. */
    public static final MonitorScreenRect RIGHT_WING = new MonitorScreenRect(-22.0F, -12.0F, 17.0F, 27.0F, -2.0F);

    public TardisFullScannerModel(ModelPart root) {
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
                        .texOffs(0, 72).addBox(-24.0F, -2.0F, -8.0F, 48.0F, 2.0F, 16.0F)
                        .texOffs(0, 0).addBox(-23.0F, -14.0F, -7.0F, 46.0F, 12.0F, 14.0F)
                        .texOffs(0, 90).addBox(-24.0F, -16.0F, -8.0F, 48.0F, 2.0F, 16.0F),
                PartPose.ZERO);
        monitor.addOrReplaceChild(
                "screen",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-10.0F, -32.0F, -4.0F, 20.0F, 16.0F, 10.0F)
                        .texOffs(60, 26).addBox(-23.0F, -28.0F, -2.0F, 12.0F, 12.0F, 8.0F)
                        .texOffs(0, 52).addBox(11.0F, -28.0F, -2.0F, 12.0F, 12.0F, 8.0F),
                PartPose.ZERO);
        monitor.addOrReplaceChild(
                "bezel",
                CubeListBuilder.create()
                        .texOffs(0, 123).addBox(-10.0F, -32.0F, -5.0F, 20.0F, 1.0F, 1.0F)
                        .texOffs(42, 123).addBox(-10.0F, -17.0F, -5.0F, 20.0F, 1.0F, 1.0F)
                        .texOffs(0, 108).addBox(-10.0F, -31.0F, -5.0F, 1.0F, 14.0F, 1.0F)
                        .texOffs(4, 108).addBox(9.0F, -31.0F, -5.0F, 1.0F, 14.0F, 1.0F)
                        .texOffs(84, 123).addBox(-23.0F, -28.0F, -3.0F, 12.0F, 1.0F, 1.0F)
                        .texOffs(0, 125).addBox(-23.0F, -17.0F, -3.0F, 12.0F, 1.0F, 1.0F)
                        .texOffs(8, 108).addBox(-12.0F, -27.0F, -3.0F, 1.0F, 10.0F, 1.0F)
                        .texOffs(12, 108).addBox(-23.0F, -27.0F, -3.0F, 1.0F, 10.0F, 1.0F)
                        .texOffs(26, 125).addBox(11.0F, -28.0F, -3.0F, 12.0F, 1.0F, 1.0F)
                        .texOffs(52, 125).addBox(11.0F, -17.0F, -3.0F, 12.0F, 1.0F, 1.0F)
                        .texOffs(16, 108).addBox(11.0F, -27.0F, -3.0F, 1.0F, 10.0F, 1.0F)
                        .texOffs(20, 108).addBox(22.0F, -27.0F, -3.0F, 1.0F, 10.0F, 1.0F),
                PartPose.ZERO);
        monitor.addOrReplaceChild(
                "controls",
                CubeListBuilder.create()
                        .texOffs(24, 108).addBox(-7.0F, -17.0F, -8.0F, 14.0F, 1.0F, 3.0F)
                        .texOffs(58, 108).addBox(-15.0F, -17.0F, -7.0F, 2.0F, 1.0F, 2.0F)
                        .texOffs(66, 108).addBox(-18.0F, -17.0F, -7.0F, 2.0F, 1.0F, 2.0F)
                        .texOffs(74, 108).addBox(-21.0F, -17.0F, -7.0F, 2.0F, 1.0F, 2.0F)
                        .texOffs(82, 108).addBox(13.0F, -18.0F, -7.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(88, 108).addBox(16.0F, -18.0F, -7.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(94, 108).addBox(19.0F, -18.0F, -7.0F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(modelData, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static mesh; the screen content is drawn by the BER.
    }
}
