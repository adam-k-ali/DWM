// Hand-built monitor mesh; cubes and texture are generated together so UVs match 1:1.
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Full TARDIS monitor bank: roundel cabinet and desk, large centre screen flanked by two wing screens,
 * keyboard, buttons and knobs. Fits the 3-wide {@code TardisDecorShapes.FULL_SCANNER_NORTH} footprint.
 *
 * <p>Cubes are authored in entity (Y-down) space under a {@code monitor} part rotated 180° about Z,
 * so the floor-standing BER (Y-up, no flip) shows textures in the standard entity UV orientation.
 */
public class TardisFullScannerModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_full_scanner"), "main");
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
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
