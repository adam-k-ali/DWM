package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.DalekChassis;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekFlightFx;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.model.entity.TimeWarDalekModel;
import com.adamkali.dwm.render.state.DalekRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders every Dalek family. One model per chassis the family can wear; each frame draws the
 * mesh of the variant's chassis (like vanilla's tropical fish small/large models).
 */
public class DalekRenderer extends MobRenderer<DalekEntity, DalekRenderState, DalekModel> {
    private static final float GROUND_SHADOW_RADIUS = 0.55F;
    private static final float FLIGHT_SHADOW_RADIUS = 0.2F;

    private final Map<DalekChassis, DalekModel> models;
    private final DalekModel defaultModel;
    private final float groundShadowRadius;

    public DalekRenderer(EntityRendererProvider.Context context) {
        this(
                context,
                Map.of(
                        DalekChassis.CLASSIC_1963, new DalekModel(context.bakeLayer(DalekModel.LAYER_LOCATION)),
                        DalekChassis.TIME_WAR, new TimeWarDalekModel(context.bakeLayer(TimeWarDalekModel.LAYER_LOCATION))
                ),
                DalekChassis.CLASSIC_1963,
                GROUND_SHADOW_RADIUS
        );
    }

    protected DalekRenderer(
            EntityRendererProvider.Context context,
            Map<DalekChassis, DalekModel> models,
            DalekChassis defaultChassis,
            float groundShadowRadius
    ) {
        super(context, models.get(defaultChassis), groundShadowRadius);
        this.models = new EnumMap<>(models);
        this.defaultModel = models.get(defaultChassis);
        this.groundShadowRadius = groundShadowRadius;
        this.addLayer(new DalekGlowLayer(this));
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
    public void submit(
            DalekRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        this.model = models.getOrDefault(state.variant.chassis(), defaultModel);
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    protected float getShadowRadius(DalekRenderState state) {
        return state.flying ? FLIGHT_SHADOW_RADIUS : groundShadowRadius;
    }

    @Override
    public Identifier getTextureLocation(DalekRenderState state) {
        return state.variant.textureLocation();
    }
}
