package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.model.json.JsonEntityModel;
import com.adamkali.dwm.model.json.anim.AnimationVariables;
import com.adamkali.dwm.render.state.TardisRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

public class TardisModel extends JsonEntityModel<TardisRenderState> {
    private static final AnimationVariables<TardisRenderState> VARIABLES = AnimationVariables.<TardisRenderState>builder()
            .add("door_progress", TardisRenderState::getDoorSwingProgress)
            .build();
    private static final List<String> DOOR_PART_NAMES = List.of("LeftDoor", "rightDoor", "Door2", "door");

    /** Door swing comes from the layer's JSON {@code animation} ({@code door_progress}, 0-1). */
    public TardisModel(ModelPart root, ModelLayerLocation layer) {
        super(root, layer, VARIABLES);
    }

    /**
     * Door meshes across chameleon hierarchies: root {@code LeftDoor}/{@code rightDoor},
     * {@code Main}/{@code LeftDoor}|{@code Door2}, or TT Capsule {@code bone}/{@code door}.
     */
    public List<ModelPart> getDoorParts() {
        List<ModelPart> doors = new ArrayList<>(4);
        collectDoorChildren(root, doors);
        if (root.hasChild("Main")) {
            collectDoorChildren(root.getChild("Main"), doors);
        }
        if (root.hasChild("bone")) {
            collectDoorChildren(root.getChild("bone"), doors);
        }
        return doors;
    }

    /**
     * Renders the exterior shell without door meshes (for BOTI to fill the aperture first).
     */
    public void renderShell(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        renderShell(matrices, vertices, light, overlay, -1);
    }

    public void renderShell(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        List<ModelPart> doors = getDoorParts();
        for (ModelPart door : doors) {
            door.visible = false;
        }
        try {
            this.renderToBuffer(matrices, vertices, light, overlay, color);
        } finally {
            for (ModelPart door : doors) {
                door.visible = true;
            }
        }
    }

    /**
     * Renders only door meshes, applying ancestor transforms so nested doors stay aligned.
     */
    public void renderDoors(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        renderDoors(matrices, vertices, light, overlay, -1);
    }

    public void renderDoors(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        matrices.pushPose();
        try {
            root.translateAndRotate(matrices);

            for (String name : DOOR_PART_NAMES) {
                if (root.hasChild(name)) {
                    root.getChild(name).render(matrices, vertices, light, overlay, color);
                }
            }

            if (root.hasChild("Main")) {
                matrices.pushPose();
                ModelPart main = root.getChild("Main");
                main.translateAndRotate(matrices);
                for (String name : DOOR_PART_NAMES) {
                    if (main.hasChild(name)) {
                        main.getChild(name).render(matrices, vertices, light, overlay, color);
                    }
                }
                matrices.popPose();
            }

            if (root.hasChild("bone")) {
                matrices.pushPose();
                ModelPart bone = root.getChild("bone");
                bone.translateAndRotate(matrices);
                if (bone.hasChild("door")) {
                    bone.getChild("door").render(matrices, vertices, light, overlay, color);
                }
                matrices.popPose();
            }
        } finally {
            matrices.popPose();
        }
    }

    private static void collectDoorChildren(ModelPart parent, List<ModelPart> doors) {
        for (String name : DOOR_PART_NAMES) {
            if (parent.hasChild(name)) {
                doors.add(parent.getChild(name));
            }
        }
    }
}
