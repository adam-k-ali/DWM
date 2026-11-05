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
 * Compact TARDIS monitor: roundel cabinet on a plinth, steel neck, bezelled screen housing with hood.
 * Fits the 1×1×2 {@code TardisDecorShapes.COMPACT_SCANNER} footprint.
 *
 * <p>Cubes are authored in entity (Y-down) space under a {@code monitor} part rotated 180° about Z,
 * so the floor-standing BER (Y-up, no flip) shows textures in the standard entity UV orientation.
 */
public class TardisCompactScannerModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_compact_scanner"), "main");
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;
    /** Centre of a reserved 2×2 white texel, used to tint the screen glow quad. */
    public static final float GLOW_U = 127.0F / TEXTURE_WIDTH;
    public static final float GLOW_V = 63.0F / TEXTURE_HEIGHT;
    /** Screen glass: 14×12 px on the housing front. */
    public static final MonitorScreenRect SCREEN = new MonitorScreenRect(-7.0F, 7.0F, 18.0F, 30.0F, -5.0F);

    public TardisCompactScannerModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
