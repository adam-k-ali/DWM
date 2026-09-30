package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.json.EntityModelJson;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

public class TardisGlobeModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_globe"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_globe.png");

    public TardisGlobeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition getTexturedModelData() {
        return EntityModelJson.loadClasspath(LAYER_LOCATION.model());
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        // Static decor mesh.
    }
}
