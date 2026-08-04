package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

/**
 * Planet-locator dial on Panel3. Geometry lives in {@link ConsoleSelectorModel}.
 */
public class PlanetLocatorModel extends ConsoleSelectorModel {
    public static final EntityModelLayer LAYER_LOCATION =
            new EntityModelLayer(Identifier.of(DWMReference.MOD_ID, "planet_locator"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.of(DWMReference.MOD_ID, "textures/entity/planet_locator.png");

    public PlanetLocatorModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData() {
        return ConsoleSelectorModel.getTexturedModelData();
    }
}
