// Made with Blockbench (converted from reader.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.adamkali.dwm.model.json.EntityModelAnimations;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Shared 16x3x16 environment / refueler dial mesh. Texture is swapped per instrument.
 */
public class ReaderModel extends JsonEntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "reader"), "main");
    public static final Identifier OXYGEN_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/oxygen_reader.png");
    public static final Identifier PRESSURE_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/pressure_reader.png");
    public static final Identifier TEMPERATURE_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/temperature_reader.png");
    public static final Identifier REFUELER_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/refueler.png");

    public ReaderModel(ModelPart root) {
        super(root, EntityModelAnimations.get(LAYER_LOCATION), TardisControlVariables.VARIABLES);
    }
}
