package com.adamkali.dwm.entity.definition;

import com.adamkali.dwm.entity.DalekEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Mob;

/**
 * Optional {@code can_use} predicate wrapping a constructed goal.
 */
public sealed interface GoalCondition {
    Kind kind();

    boolean test(Mob mob);

    Codec<GoalCondition> CODEC = Kind.CODEC.dispatch("type", GoalCondition::kind, Kind::mapCodec);

    enum Kind implements StringRepresentable {
        NOT_FLYING("dwm:not_flying");

        public static final StringRepresentable.EnumCodec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        MapCodec<? extends GoalCondition> mapCodec() {
            return switch (this) {
                case NOT_FLYING -> NotFlying.MAP_CODEC;
            };
        }
    }

    record NotFlying() implements GoalCondition {
        static final NotFlying INSTANCE = new NotFlying();
        static final MapCodec<NotFlying> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.NOT_FLYING;
        }

        @Override
        public boolean test(Mob mob) {
            if (!(mob instanceof DalekEntity dalek)) {
                throw new IllegalStateException("dwm:not_flying requires DalekEntity, got " + mob.getClass().getSimpleName());
            }
            return !dalek.isFlying();
        }
    }
}
