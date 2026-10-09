package com.adamkali.dwm.model.json.anim;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.model.json.EntityModelFile;
import com.adamkali.dwm.model.json.EntityModelJson;
import com.adamkali.dwm.model.json.JsonEntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityAnimationTest {
    private static final String MESH = """
            "texture_width": 16, "texture_height": 16, "texture": "dwm:textures/entity/x.png",
            "parts": [
              {"name": "neck", "rotation": [60, 0, 0], "children": [{"name": "head", "pivot": [0, 2, 0]}]},
              {"name": "leg"}
            ]""";

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static EntityModelFile file(String animation) {
        return EntityModelJson.parse("{" + MESH + ", \"animation\": " + animation + "}");
    }

    private static ModelPart bake(EntityModelFile file) {
        return EntityModelJson.toLayerDefinition(file).bakeRoot();
    }

    private static LivingEntityRenderState state(float walkPos, float walkSpeed, float age, float pitch, float yaw) {
        LivingEntityRenderState state = new LivingEntityRenderState();
        state.walkAnimationPos = walkPos;
        state.walkAnimationSpeed = walkSpeed;
        state.ageInTicks = age;
        state.xRot = pitch;
        state.yRot = yaw;
        return state;
    }

    private static JsonEntityModel<LivingEntityRenderState> model(EntityModelFile file, ModelPart root) {
        return new JsonEntityModel<>(root, file.animation(), AnimationVariables.<LivingEntityRenderState>living().build());
    }

    @Test
    void bindingsAddToRestPoseAfterReset() {
        EntityModelFile file = file("""
                {"bindings": [
                  {"part": "neck/head", "channel": "rot_x", "value": "head_pitch"},
                  {"part": "neck", "channel": "rot_y", "value": "head_yaw"},
                  {"part": "leg", "channel": "pos_y", "value": "age"}
                ]}""");
        ModelPart root = bake(file);
        JsonEntityModel<LivingEntityRenderState> model = model(file, root);

        model.setupAnim(state(0, 0, 3, 90, 45));
        assertEquals((float) Math.toRadians(90), root.getChild("neck").getChild("head").xRot, 1.0e-6F);
        assertEquals((float) Math.toRadians(60), root.getChild("neck").xRot, 1.0e-6F);
        assertEquals((float) Math.toRadians(45), root.getChild("neck").yRot, 1.0e-6F);
        assertEquals(3.0F, root.getChild("leg").y);

        // A second frame starts from the rest pose again rather than accumulating.
        model.setupAnim(state(0, 0, 1, 0, 0));
        assertEquals(0.0F, root.getChild("neck").getChild("head").xRot);
        assertEquals(1.0F, root.getChild("leg").y);
    }

    @Test
    void walkCycleMatchesJavaFormula() {
        EntityModelFile file = file("""
                {"bindings": [
                  {"part": "leg", "channel": "rot_x", "value": "cos(walk_pos * 0.6662) * 1.4 * walk_speed * deg"}
                ]}""");
        ModelPart root = bake(file);
        JsonEntityModel<LivingEntityRenderState> model = model(file, root);
        for (float pos : new float[]{0.0F, 0.9F, 2.5F, 7.3F}) {
            model.setupAnim(state(pos, 0.7F, 0, 0, 0));
            assertEquals(Mth.cos(pos * 0.6662F) * 1.4F * 0.7F, root.getChild("leg").xRot, 1.0e-5F);
        }
    }

    @Test
    void bindingWithoutPartTargetsRoot() {
        EntityModelFile file = file("""
                {"bindings": [{"channel": "pos_y", "value": "2"}]}""");
        ModelPart root = bake(file);
        model(file, root).setupAnim(state(0, 0, 0, 0, 0));
        assertEquals(2.0F, root.y);
    }

    @Test
    void sameChannelBindingsSum() {
        EntityModelFile file = file("""
                {"bindings": [
                  {"part": "leg", "channel": "pos_x", "value": "1"},
                  {"part": "leg", "channel": "pos_x", "value": "2"}
                ]}""");
        ModelPart root = bake(file);
        model(file, root).setupAnim(state(0, 0, 0, 0, 0));
        assertEquals(3.0F, root.getChild("leg").x);
    }

    @Test
    void unknownPartFailsAtConstruction() {
        EntityModelFile file = file("""
                {"bindings": [{"part": "neck/tail", "channel": "rot_x", "value": "1"}]}""");
        ModelPart root = bake(file);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> model(file, root));
        assertTrue(error.getMessage().contains("tail") && error.getMessage().contains("neck/tail"));
    }

    @Test
    void unknownVariableFailsAtConstruction() {
        EntityModelFile file = file("""
                {"bindings": [{"part": "leg", "channel": "rot_x", "value": "door_progress"}]}""");
        ModelPart root = bake(file);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> model(file, root));
        assertTrue(error.getMessage().contains("door_progress") && error.getMessage().contains("walk_pos"));
    }

    @Test
    void unknownChannelFailsToParse() {
        assertThrows(RuntimeException.class, () -> file("""
                {"bindings": [{"part": "leg", "channel": "rot_w", "value": "1"}]}"""));
    }

    @Test
    void childOverlayReplacesSameBindingAndAppendsNewOnes() {
        EntityAnimation parent = file("""
                {"bindings": [
                  {"part": "leg", "channel": "rot_x", "value": "1"},
                  {"part": "leg", "channel": "rot_y", "value": "2"}
                ]}""").animation();
        EntityAnimation child = file("""
                {"bindings": [
                  {"part": "leg", "channel": "rot_x", "value": "9"},
                  {"part": "neck", "channel": "rot_z", "value": "3"}
                ]}""").animation();
        EntityAnimation merged = parent.overlay(child);
        assertEquals(3, merged.bindings().size());
        assertEquals("9", merged.bindings().get(0).value().source());
        assertEquals("2", merged.bindings().get(1).value().source());
        assertEquals("neck", merged.bindings().get(2).partPath());
        assertEquals(parent, parent.overlay(EntityAnimation.EMPTY));
    }

    @Test
    void parentResolutionMergesAnimation() {
        EntityModelFile parent = file("""
                {"bindings": [{"part": "leg", "channel": "rot_x", "value": "1"}]}""");
        EntityModelFile child = EntityModelJson.parse("""
                {"parent": "dwm:p", "animation": {"bindings": [{"part": "leg", "channel": "rot_x", "value": "5"}]}}""");
        EntityModelFile resolved = EntityModelJson.resolveParents(id -> parent, child);
        assertEquals("5", resolved.animation().bindings().getFirst().value().source());
    }

    @Test
    void modelWithoutAnimationLeavesPoseAtRest() {
        EntityModelFile file = EntityModelJson.parse("{" + MESH + "}");
        assertTrue(file.animation().isEmpty());
        ModelPart root = bake(file);
        model(file, root).setupAnim(state(5, 1, 5, 5, 5));
        assertEquals((float) Math.toRadians(60), root.getChild("neck").xRot, 1.0e-6F);
    }
}
