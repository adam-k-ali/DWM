// Converted from time_lord.bbmodel (Blockbench skin / villager-based entity).
package com.adamkali.dwm.model.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Villager-like mesh with ceremonial collar. Coordinates follow vanilla
 * {@code VillagerModel} (Y-down from neck); Blockbench skin Y-up values were converted.
 */
public class TimeLordModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "time_lord"), "main");

    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TimeLordModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(31, 49).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(23, 20).addBox(-1.0F, -3.0F, -5.7F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 23.0F, 6.0F, new CubeDeformation(0.5F))
                        .texOffs(44, 10).addBox(-4.5F, -2.8F, 3.5F, 9.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 0).addBox(-7.5F, -11.8F, 4.0F, 15.0F, 9.0F, 1.0F, new CubeDeformation(0.5F))
                        .texOffs(36, 11).addBox(2.0F, -14.3F, 4.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 11).mirror().addBox(-8.0F, -14.3F, 4.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.ZERO);

        // Folded arms — pitch baked into PartPose; setupAnim does not swing them.
        root.addOrReplaceChild(
                "arms",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, true)
                        .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        float speed = state.walkAnimationSpeed;
        float pos = state.walkAnimationPos;
        this.rightLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leftLeg.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
    }
}
