package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Model layers of the chameleon exteriors. Each variant's layer id is its variant id; mesh,
 * texture and door animation come from {@code models/entity/<id>.json}.
 */
public final class TardisModels {
    private TardisModels() {
    }

    public static ModelLayerLocation layer(TardisChameleonVariant variant) {
        return new ModelLayerLocation(variant.getId(), variant.getId().getPath());
    }

    public static TardisModel create(TardisChameleonVariant variant, ModelPart root) {
        return new TardisModel(root, layer(variant));
    }
}
