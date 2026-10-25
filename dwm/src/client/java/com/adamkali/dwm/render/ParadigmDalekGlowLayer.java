package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.render.state.DalekRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Full-bright overlay for the Paradigm eye and dome lights, so they glow in the dark. The glow
 * texture is shared by every caste: only the eye and light texels are opaque.
 */
public class ParadigmDalekGlowLayer extends EyesLayer<DalekRenderState, DalekModel> {
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/dalek/paradigm_glow.png");
    private static final RenderType GLOW = RenderTypes.eyes(TEXTURE);

    public ParadigmDalekGlowLayer(RenderLayerParent<DalekRenderState, DalekModel> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
