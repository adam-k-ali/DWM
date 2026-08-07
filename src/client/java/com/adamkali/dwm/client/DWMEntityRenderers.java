package com.adamkali.dwm.client;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.DWMEntityTypes;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class DWMEntityRenderers {
    public static final EntityModelLayer ASH_BOAT_LAYER = new EntityModelLayer(
            Identifier.of(DWMReference.MOD_ID, "boat/ash"),
            "main"
    );

    private DWMEntityRenderers() {
    }

    public static void initialize() {
        EntityModelLayerRegistry.registerModelLayer(ASH_BOAT_LAYER, BoatEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(
                DWMEntityTypes.ASH_BOAT,
                context -> new BoatEntityRenderer(context, ASH_BOAT_LAYER)
        );
    }
}
