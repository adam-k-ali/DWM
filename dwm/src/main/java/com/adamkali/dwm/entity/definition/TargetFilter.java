package com.adamkali.dwm.entity.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * Optional {@code filter} on {@code minecraft:nearest_attackable_target}.
 */
public sealed interface TargetFilter {
    Kind kind();

    TargetingConditions.Selector selector(Mob mob);

    Codec<TargetFilter> CODEC = Kind.CODEC.dispatch("type", TargetFilter::kind, Kind::mapCodec);

    enum Kind implements StringRepresentable {
        IS_ANGRY_AT("dwm:is_angry_at");

        public static final StringRepresentable.EnumCodec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        MapCodec<? extends TargetFilter> mapCodec() {
            return switch (this) {
                case IS_ANGRY_AT -> IsAngryAt.MAP_CODEC;
            };
        }
    }

    record IsAngryAt() implements TargetFilter {
        static final IsAngryAt INSTANCE = new IsAngryAt();
        static final MapCodec<IsAngryAt> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.IS_ANGRY_AT;
        }

        @Override
        public TargetingConditions.Selector selector(Mob mob) {
            if (!(mob instanceof NeutralMob neutral)) {
                throw new IllegalStateException("dwm:is_angry_at requires NeutralMob, got " + mob.getClass().getSimpleName());
            }
            return (LivingEntity target, ServerLevel level) -> neutral.isAngryAt(target, level);
        }
    }
}
