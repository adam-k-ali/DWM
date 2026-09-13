package com.adamkali.dwm.model.armor;

import com.adamkali.dwm.DWMReference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Compact EVA protective suit. Slot-specific meshes keep standard humanoid part
 * names so player animation copies cleanly; visor / backpack are children of the
 * moving parent parts.
 */
public class ProtectiveSuitModel extends HumanoidModel<HumanoidRenderState> {
    public static final String MODEL_ID = "protective_suit";

    public static final ArmorModelSet<ModelLayerLocation> LAYER_SET = new ArmorModelSet<>(
            layer("helmet"),
            layer("chestplate"),
            layer("leggings"),
            layer("boots")
    );

    public static final Identifier OUTER_TEXTURE = Identifier.fromNamespaceAndPath(
            DWMReference.MOD_ID,
            "textures/entity/equipment/humanoid/protective_suit.png"
    );

    public static final Identifier LEGGINGS_TEXTURE = Identifier.fromNamespaceAndPath(
            DWMReference.MOD_ID,
            "textures/entity/equipment/humanoid_leggings/protective_suit.png"
    );

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    public ProtectiveSuitModel(ModelPart root) {
        super(root);
    }

    public static ArmorModelSet<LayerDefinition> createArmorLayerSet() {
        return new ArmorModelSet<>(
                createHelmetLayer(),
                createChestLayer(),
                createLeggingsLayer(),
                createBootsLayer()
        );
    }

    public static LayerDefinition createHelmetLayer() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition head = mesh.getRoot().getChild("head");
        head.addOrReplaceChild(
                "helmet_shell",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.05F)),
                PartPose.ZERO
        );
        head.addOrReplaceChild(
                "visor",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(-4.0F, -7.0F, -6.25F, 8.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );
        head.addOrReplaceChild(
                "neck_ring",
                CubeListBuilder.create()
                        .texOffs(40, 8)
                        .addBox(-3.5F, 0.4F, -3.5F, 7.0F, 1.6F, 7.0F, new CubeDeformation(0.05F)),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChestLayer() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 32)
                        .addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.65F)),
                PartPose.ZERO
        );
        body.addOrReplaceChild(
                "backpack",
                CubeListBuilder.create()
                        .texOffs(96, 0)
                        .addBox(-3.5F, 1.0F, 2.4F, 7.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(96, 16)
                        .addBox(-2.0F, 2.5F, 6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(108, 16)
                        .addBox(-1.0F, 7.5F, 6.0F, 2.0F, 2.0F, 1.5F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );

        // Full-length sealed sleeves + glove cuffs that cover player hands.
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(64, 16)
                        .addBox(-3.5F, -2.5F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.2F))
                        .texOffs(64, 36)
                        .addBox(-3.6F, 7.5F, -2.6F, 5.2F, 3.2F, 5.2F, new CubeDeformation(0.15F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(64, 16)
                        .mirror()
                        .addBox(-1.5F, -2.5F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.2F))
                        .texOffs(64, 36)
                        .mirror()
                        .addBox(-1.6F, 7.5F, -2.6F, 5.2F, 3.2F, 5.2F, new CubeDeformation(0.15F)),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLeggingsLayer() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.3F, 8.0F, 6.0F, 4.6F, new CubeDeformation(0.35F)),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.15F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .mirror()
                        .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.15F)),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createBootsLayer() {
        MeshDefinition mesh = emptyHumanoid();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 48)
                        .addBox(-2.6F, 8.0F, -2.6F, 5.2F, 4.0F, 5.2F, new CubeDeformation(0.25F))
                        .texOffs(22, 48)
                        .addBox(-2.6F, 10.5F, -3.8F, 5.2F, 1.5F, 6.4F, new CubeDeformation(0.05F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 48)
                        .mirror()
                        .addBox(-2.6F, 8.0F, -2.6F, 5.2F, 4.0F, 5.2F, new CubeDeformation(0.25F))
                        .texOffs(22, 48)
                        .mirror()
                        .addBox(-2.6F, 10.5F, -3.8F, 5.2F, 1.5F, 6.4F, new CubeDeformation(0.05F)),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    /**
     * Full humanoid hierarchy with empty cubes so {@link HumanoidModel} can bind
     * every required part; slot layers then replace the parts they own.
     */
    private static MeshDefinition emptyHumanoid() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create(),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create(),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create(),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create(),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return mesh;
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, MODEL_ID),
                name
        );
    }
}
