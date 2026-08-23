package com.adamkali.dwm.render;

import com.adamkali.dwm.entity.BroakirEntity;
import com.adamkali.dwm.model.entity.BroakirModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class BroakirRenderer extends MobRenderer<BroakirEntity, LivingEntityRenderState, BroakirModel> {
    public BroakirRenderer(EntityRendererProvider.Context context) {
        super(context, new BroakirModel(context.bakeLayer(BroakirModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return BroakirModel.TEXTURE_LOCATION;
    }
}
