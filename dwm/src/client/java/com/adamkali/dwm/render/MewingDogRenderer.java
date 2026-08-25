package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.MewingDogEntity;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.animal.wolf.BabyWolfModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;

public class MewingDogRenderer extends AgeableMobRenderer<MewingDogEntity, WolfRenderState, WolfModel> {
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/mewing_dog.png");

    public MewingDogRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new AdultWolfModel(context.bakeLayer(ModelLayers.WOLF)),
                new BabyWolfModel(context.bakeLayer(ModelLayers.WOLF_BABY)),
                0.5F
        );
        this.addLayer(new WolfCollarLayer(this));
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(MewingDogEntity entity, WolfRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAngry = entity.isAngry();
        state.isSitting = entity.isInSittingPose();
        state.tailAngle = entity.getTailAngle();
        state.headRollAngle = 0.0F;
        state.shakeAnim = 0.0F;
        state.wetShade = 1.0F;
        state.texture = TEXTURE_LOCATION;
        state.collarColor = entity.isTame() ? entity.getCollarColor() : null;
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return TEXTURE_LOCATION;
    }
}
