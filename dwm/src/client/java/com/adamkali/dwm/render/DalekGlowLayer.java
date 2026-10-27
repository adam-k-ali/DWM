package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.render.state.DalekRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Full-bright overlay for the eye and dome lights, so they glow in the dark. One glow texture per
 * chassis, shared by every variant on it (only the eye and light texels are opaque); a chassis
 * without one (the 1963 casing) stays unlit.
 */
public class DalekGlowLayer extends RenderLayer<DalekRenderState, DalekModel> {
    public static final Identifier PARADIGM_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/dalek/paradigm_glow.png");
    public static final Identifier SPECIAL_WEAPONS_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/dalek/special_weapons_glow.png");
    public static final Identifier TIME_WAR_TEXTURE =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/dalek/time_war_glow.png");

    private static final Map<DalekChassis, Identifier> TEXTURES = new EnumMap<>(Map.of(
            DalekChassis.TIME_WAR, TIME_WAR_TEXTURE,
            DalekChassis.PARADIGM, PARADIGM_TEXTURE,
            DalekChassis.SPECIAL_WEAPONS, SPECIAL_WEAPONS_TEXTURE
    ));
    private static final Map<DalekChassis, RenderType> RENDER_TYPES = new EnumMap<>(DalekChassis.class);

    static {
        TEXTURES.forEach((chassis, texture) -> RENDER_TYPES.put(chassis, RenderTypes.eyes(texture)));
    }

    public DalekGlowLayer(RenderLayerParent<DalekRenderState, DalekModel> renderer) {
        super(renderer);
    }

    /** Glow texture for {@code chassis}, or {@code null} when that casing has no lit eye. */
    public static @Nullable Identifier glowTexture(DalekChassis chassis) {
        return TEXTURES.get(chassis);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            DalekRenderState state,
            float yRot,
            float xRot
    ) {
        RenderType glow = RENDER_TYPES.get(state.variant.chassis());
        if (glow == null) {
            return;
        }
        submitNodeCollector.order(1)
                .submitModel(this.getParentModel(), state, poseStack, glow, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
