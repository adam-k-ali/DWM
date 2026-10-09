package com.adamkali.dwm.model.json;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.entity.DalekFlightFx;
import com.adamkali.dwm.model.entity.BroakirModel;
import com.adamkali.dwm.model.entity.DalekModel;
import com.adamkali.dwm.model.entity.FlutterwingModel;
import com.adamkali.dwm.model.entity.TimeLordModel;
import com.adamkali.dwm.render.state.DalekRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mesh and animation of Broakir, Time Lord, Flutterwing and Dalek live in JSON; these pin the
 * poses the old Java {@code setupAnim} produced.
 */
class ConvertedEntityModelsTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static <M extends EntityModel<?>> ModelPart bake(ModelLayerLocation layer, Function<ModelPart, M> factory, ModelHolder<M> out) {
        EntityModelFile file = EntityModelJson.loadClasspathFile(layer.model());
        EntityModelAnimations.put(layer.model(), file.animation());
        ModelPart root = EntityModelJson.toLayerDefinition(file).bakeRoot();
        out.model = factory.apply(root);
        return root;
    }

    private static final class ModelHolder<M> {
        M model;
    }

    private static LivingEntityRenderState living(LivingEntityRenderState s, float pos, float speed, float age, float pitch, float yaw) {
        s.walkAnimationPos = pos;
        s.walkAnimationSpeed = speed;
        s.ageInTicks = age;
        s.xRot = pitch;
        s.yRot = yaw;
        return s;
    }

    @Test
    void broakirWalkLookAndTailSway() {
        ModelHolder<BroakirModel> holder = new ModelHolder<>();
        ModelPart root = bake(BroakirModel.LAYER_LOCATION, BroakirModel::new, holder);
        float pos = 2.5F;
        float speed = 0.7F;
        float age = 13.0F;
        holder.model.setupAnim(living(new LivingEntityRenderState(), pos, speed, age, 20.0F, -30.0F));

        assertEquals(Mth.cos(pos * 0.6662F) * 1.4F * speed, root.getChild("leg1").xRot, 1.0e-5F);
        assertEquals(Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed, root.getChild("leg2").xRot, 1.0e-5F);
        assertEquals(Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed, root.getChild("leg3").xRot, 1.0e-5F);
        assertEquals(Mth.cos(pos * 0.6662F) * 1.4F * speed, root.getChild("leg4").xRot, 1.0e-5F);
        ModelPart head = root.getChild("neck").getChild("head");
        assertEquals(-0.523599F + 20.0F * Mth.DEG_TO_RAD, head.xRot, 1.0e-5F);
        assertEquals(-30.0F * Mth.DEG_TO_RAD, head.yRot, 1.0e-5F);
        assertEquals(0.523599F + Mth.cos(age * 0.05F) * 0.1F, root.getChild("tail").xRot, 1.0e-5F);
    }

    @Test
    void timeLordWalkAndLook() {
        ModelHolder<TimeLordModel> holder = new ModelHolder<>();
        ModelPart root = bake(TimeLordModel.LAYER_LOCATION, TimeLordModel::new, holder);
        holder.model.setupAnim(living(new LivingEntityRenderState(), 1.5F, 0.5F, 0.0F, 10.0F, 25.0F));
        assertEquals(Mth.cos(1.5F * 0.6662F) * 1.4F * 0.5F, root.getChild("right_leg").xRot, 1.0e-5F);
        assertEquals(Mth.cos(1.5F * 0.6662F + Mth.PI) * 1.4F * 0.5F, root.getChild("left_leg").xRot, 1.0e-5F);
        assertEquals(10.0F * Mth.DEG_TO_RAD, root.getChild("head").xRot, 1.0e-5F);
        assertEquals(25.0F * Mth.DEG_TO_RAD, root.getChild("head").yRot, 1.0e-5F);
    }

    @Test
    void flutterwingFlapsWingsInOpposition() {
        ModelHolder<FlutterwingModel> holder = new ModelHolder<>();
        ModelPart root = bake(FlutterwingModel.LAYER_LOCATION, FlutterwingModel::new, holder);
        float age = 7.0F;
        holder.model.setupAnim(living(new LivingEntityRenderState(), 0, 0, age, 0, 0));
        float flap = Mth.cos(age * 74.48451F * Mth.DEG_TO_RAD) * Mth.PI * 0.25F;
        ModelPart body = root.getChild("body");
        assertEquals(flap, body.getChild("rightWing").yRot, 2.0e-4F);
        assertEquals(-flap, body.getChild("leftWing").yRot, 2.0e-4F);
        assertEquals(flap * 0.5F, body.getChild("rightWing").getChild("rightWingTip").yRot, 2.0e-4F);
        assertEquals(-flap * 0.5F, body.getChild("leftWing").getChild("leftWingTip").yRot, 2.0e-4F);
    }

    @Test
    void dalekLooksLeansAndBobsOnlyWhenFlying() {
        ModelHolder<DalekModel> holder = new ModelHolder<>();
        ModelPart root = bake(DalekModel.LAYER_LOCATION, DalekModel::new, holder);
        DalekRenderState state = new DalekRenderState();
        living(state, 0, 0, 9.0F, 40.0F, 20.0F);
        state.leanPitch = 6.0F;
        state.leanRoll = -4.0F;

        state.flying = false;
        holder.model.setupAnim(state);
        assertEquals(0.0F, root.y, 1.0e-6F);
        assertEquals(6.0F * Mth.DEG_TO_RAD, root.xRot, 1.0e-5F);
        assertEquals(-4.0F * Mth.DEG_TO_RAD, root.zRot, 1.0e-5F);
        ModelPart head = root.getChild("head");
        assertEquals(40.0F * Mth.DEG_TO_RAD * 0.35F, head.xRot, 1.0e-5F);
        assertEquals(20.0F * Mth.DEG_TO_RAD * 0.35F, head.yRot, 1.0e-5F);
        assertEquals(40.0F * Mth.DEG_TO_RAD * 0.65F, head.getChild("eyestalk").xRot, 1.0e-5F);

        state.flying = true;
        holder.model.setupAnim(state);
        assertEquals(DalekFlightFx.bobOffset(9.0F, true), root.y, 1.0e-5F);
        assertTrue(Math.abs(root.y) > 0.0F);
    }
}
