// Made with Blockbench (converted from door_lock.bbmodel)
package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * Panel4 bottom-row door lock.
 */
public class DoorLockModel extends EntityModel<TardisRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "door_lock"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "textures/entity/tardis_lever_switch.png");

    public static final float LEVER_PITCH_UNLOCKED = 0.0F;
    public static final float LEVER_PITCH_LOCKED = 0.45F;

    private final ModelPart lever;

    public DoorLockModel(ModelPart root) {
        super(root);
        this.lever = root.getChild("lever");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();

        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(0, 1).addBox(-2.0F, 0.0F, 4.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition lever = root.addOrReplaceChild(
                "lever",
                CubeListBuilder.create()
                        .texOffs(4, 0).addBox(-1.0F, -0.3F, -10.5F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, 6.0F));

        PartDefinition light = root.addOrReplaceChild(
                "light",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.957143F, -0.728571F, -2.114286F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.042857F, -0.728571F, -2.114286F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.042857F, -0.728571F, 1.885714F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.042857F, -0.728571F, -3.114286F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 17).addBox(-2.042857F, -0.228571F, -2.114286F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 22).addBox(-1.642857F, 0.071429F, -1.714286F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 22).addBox(0.357143F, 0.071429F, -1.714286F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.042857F, 0.728571F, -2.885714F));

        PartDefinition light2 = root.addOrReplaceChild(
                "light2",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.957143F, -0.728571F, -2.114286F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.042857F, -0.728571F, -2.114286F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.042857F, -0.728571F, 1.885714F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.042857F, -0.728571F, -3.114286F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 17).addBox(-2.042857F, -0.228571F, -2.114286F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 22).addBox(-1.642857F, 0.071429F, -1.714286F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 22).addBox(0.357143F, 0.071429F, -1.714286F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.957143F, 0.728571F, -2.885714F));

        PartDefinition light3 = root.addOrReplaceChild(
                "light3",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.957143F, -0.728571F, -2.114286F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.042857F, -0.728571F, -2.114286F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.042857F, -0.728571F, 1.885714F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.042857F, -0.728571F, -3.114286F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 17).addBox(-2.042857F, -0.228571F, -2.114286F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(2, 22).addBox(-1.642857F, 0.071429F, -1.714286F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 22).addBox(0.357143F, 0.071429F, -1.714286F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.042857F, 0.728571F, -9.885714F));

        return LayerDefinition.create(modelData, 32, 32);
    }

    @Override
    public void setupAnim(TardisRenderState state) {
        lever.xRot = state.areDoorsLocked() ? LEVER_PITCH_LOCKED : LEVER_PITCH_UNLOCKED;
    }
}
