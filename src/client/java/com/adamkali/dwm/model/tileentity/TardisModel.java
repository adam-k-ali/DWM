package com.adamkali.dwm.model.tileentity;

import com.adamkali.dwm.render.state.TardisRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public abstract class TardisModel extends EntityModel<TardisRenderState> {
    private static final List<String> DOOR_PART_NAMES = List.of("LeftDoor", "rightDoor", "Door2", "door");

    protected TardisModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setAngles(TardisRenderState state) {
        super.setAngles(state);
        float doorSwingProgress = state.getDoorSwingProgress();
        ModelPart leftDoor = null;

        if (root.hasChild("LeftDoor")) {
            leftDoor = root.getChild("LeftDoor");
        } else if (root.hasChild("Main") && root.getChild("Main").hasChild("LeftDoor")) {
            leftDoor = root.getChild("Main").getChild("LeftDoor");
        }

        if (leftDoor != null) {
            leftDoor.setAngles(0.0F, doorSwingProgress * (float) Math.PI / 3, 0.0F);
        }
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
    public void renderShell(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        List<ModelPart> doors = getDoorParts();
        for (ModelPart door : doors) {
            door.visible = false;
        }
        try {
            this.render(matrices, vertices, light, overlay);
        } finally {
            for (ModelPart door : doors) {
                door.visible = true;
            }
        }
    }

    /**
     * Renders only door meshes, applying ancestor transforms so nested doors stay aligned.
     */
    public void renderDoors(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        matrices.push();
        try {
            root.rotate(matrices);

            for (String name : DOOR_PART_NAMES) {
                if (root.hasChild(name)) {
                    root.getChild(name).render(matrices, vertices, light, overlay);
                }
            }

            if (root.hasChild("Main")) {
                matrices.push();
                ModelPart main = root.getChild("Main");
                main.rotate(matrices);
                for (String name : DOOR_PART_NAMES) {
                    if (main.hasChild(name)) {
                        main.getChild(name).render(matrices, vertices, light, overlay);
                    }
                }
                matrices.pop();
            }

            if (root.hasChild("bone")) {
                matrices.push();
                ModelPart bone = root.getChild("bone");
                bone.rotate(matrices);
                if (bone.hasChild("door")) {
                    bone.getChild("door").render(matrices, vertices, light, overlay);
                }
                matrices.pop();
            }
        } finally {
            matrices.pop();
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
