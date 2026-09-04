package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekFlightFx;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.render.state.DalekRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class DalekRenderer extends MobRenderer<DalekEntity, DalekRenderState, DalekModel> {
    private static final float GROUND_SHADOW_RADIUS = 0.55F;
    private static final float FLIGHT_SHADOW_RADIUS = 0.2F;

    public DalekRenderer(EntityRendererProvider.Context context) {
        super(context, new DalekModel(context.bakeLayer(DalekModel.LAYER_LOCATION)), GROUND_SHADOW_RADIUS);
    }

    @Override
    public DalekRenderState createRenderState() {
        return new DalekRenderState();
    }

    @Override
    public void extractRenderState(DalekEntity entity, DalekRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.variant = entity.getVariant();
        state.flying = entity.isFlying();
        Vec3 localVel = DalekFlightFx.toLocalVelocity(entity.getDeltaMovement(), state.yRot);
        state.leanPitch = DalekFlightFx.leanPitchDegrees(localVel, state.flying);
        state.leanRoll = DalekFlightFx.leanRollDegrees(localVel, state.flying);
    }

    @Override
    protected float getShadowRadius(DalekRenderState state) {
        return state.flying ? FLIGHT_SHADOW_RADIUS : GROUND_SHADOW_RADIUS;
    }

    @Override
    public Identifier getTextureLocation(DalekRenderState state) {
        return state.variant.textureLocation();
    }
}
