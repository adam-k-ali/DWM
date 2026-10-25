package com.adamkali.dwm.model.json.anim;

import com.mojang.serialization.Codec;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.StringRepresentable;

/**
 * A pose component a binding drives. Rotations are in degrees (like the rest {@code rotation} in
 * the model file); positions are model units. Values are added to the part's rest pose.
 */
public enum AnimationChannel implements StringRepresentable {
    ROT_X("rot_x"), ROT_Y("rot_y"), ROT_Z("rot_z"),
    POS_X("pos_x"), POS_Y("pos_y"), POS_Z("pos_z");

    public static final Codec<AnimationChannel> CODEC = StringRepresentable.fromEnum(AnimationChannel::values);

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final String serializedName;

    AnimationChannel(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public void add(ModelPart part, float value) {
        switch (this) {
            case ROT_X -> part.xRot += value * DEG_TO_RAD;
            case ROT_Y -> part.yRot += value * DEG_TO_RAD;
            case ROT_Z -> part.zRot += value * DEG_TO_RAD;
            case POS_X -> part.x += value;
            case POS_Y -> part.y += value;
            case POS_Z -> part.z += value;
        }
    }
}
