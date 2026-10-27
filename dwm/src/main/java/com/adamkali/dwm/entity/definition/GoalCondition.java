package com.adamkali.dwm.entity.definition;

import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.ThalEntity;
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
        NOT_FLYING("dwm:not_flying"),
        ARMED("dwm:armed"),
        NOT_ARMED("dwm:not_armed");

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
                case ARMED -> Armed.MAP_CODEC;
                case NOT_ARMED -> NotArmed.MAP_CODEC;
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

    record Armed() implements GoalCondition {
        static final Armed INSTANCE = new Armed();
        static final MapCodec<Armed> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.ARMED;
        }

        @Override
        public boolean test(Mob mob) {
            return requireThal(mob, "dwm:armed").isArmed();
        }
    }

    record NotArmed() implements GoalCondition {
        static final NotArmed INSTANCE = new NotArmed();
        static final MapCodec<NotArmed> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.NOT_ARMED;
        }

        @Override
        public boolean test(Mob mob) {
            return !requireThal(mob, "dwm:not_armed").isArmed();
        }
    }

    private static ThalEntity requireThal(Mob mob, String name) {
        if (!(mob instanceof ThalEntity thal)) {
            throw new IllegalStateException(name + " requires ThalEntity, got " + mob.getClass().getSimpleName());
        }
        return thal;
    }
}
