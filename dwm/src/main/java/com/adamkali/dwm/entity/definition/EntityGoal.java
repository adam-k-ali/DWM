package com.adamkali.dwm.entity.definition;

import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.DalekFlightGoal;
import com.adamkali.dwm.entity.DalekShareTargetGoal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Tagged AI goal spec. Dispatch key is {@code type}.
 */
public sealed interface EntityGoal {
    Kind kind();

    Goal create(Mob mob);

    MapCodec<EntityGoal> MAP_CODEC = Kind.CODEC.dispatchMap("type", EntityGoal::kind, Kind::mapCodec);
    Codec<EntityGoal> CODEC = MAP_CODEC.codec();

    enum Selector {
        GOAL,
        TARGET
    }

    enum Kind implements StringRepresentable {
        FLOAT("minecraft:float", Selector.GOAL),
        PANIC("minecraft:panic", Selector.GOAL),
        WATER_AVOIDING_RANDOM_STROLL("minecraft:water_avoiding_random_stroll", Selector.GOAL),
        WATER_AVOIDING_RANDOM_FLYING("minecraft:water_avoiding_random_flying", Selector.GOAL),
        LOOK_AT("minecraft:look_at", Selector.GOAL),
        RANDOM_LOOK_AROUND("minecraft:random_look_around", Selector.GOAL),
        OPEN_DOOR("minecraft:open_door", Selector.GOAL),
        RANGED_ATTACK("minecraft:ranged_attack", Selector.GOAL),
        MELEE_ATTACK("minecraft:melee_attack", Selector.GOAL),
        LEAP_AT_TARGET("minecraft:leap_at_target", Selector.GOAL),
        BREED("minecraft:breed", Selector.GOAL),
        FOLLOW_OWNER("minecraft:follow_owner", Selector.GOAL),
        SIT_WHEN_ORDERED("minecraft:sit_when_ordered", Selector.GOAL),
        DALEK_FLIGHT("dwm:dalek_flight", Selector.GOAL),
        TAMABLE_PANIC("dwm:tamable_panic", Selector.GOAL),
        HURT_BY_TARGET("minecraft:hurt_by_target", Selector.TARGET),
        NEAREST_ATTACKABLE_TARGET("minecraft:nearest_attackable_target", Selector.TARGET),
        OWNER_HURT_BY_TARGET("minecraft:owner_hurt_by_target", Selector.TARGET),
        OWNER_HURT_TARGET("minecraft:owner_hurt_target", Selector.TARGET),
        RESET_UNIVERSAL_ANGER("minecraft:reset_universal_anger", Selector.TARGET),
        DALEK_SHARE_TARGET("dwm:dalek_share_target", Selector.TARGET);

        public static final StringRepresentable.EnumCodec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String serializedName;
        private final Selector selector;

        Kind(String serializedName, Selector selector) {
            this.serializedName = serializedName;
            this.selector = selector;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public Selector selector() {
            return selector;
        }

        MapCodec<? extends EntityGoal> mapCodec() {
            return switch (this) {
                case FLOAT -> FloatOnWater.MAP_CODEC;
                case PANIC -> Panic.MAP_CODEC;
                case WATER_AVOIDING_RANDOM_STROLL -> WaterAvoidingRandomStroll.MAP_CODEC;
                case WATER_AVOIDING_RANDOM_FLYING -> WaterAvoidingRandomFlying.MAP_CODEC;
                case LOOK_AT -> LookAt.MAP_CODEC;
                case RANDOM_LOOK_AROUND -> RandomLookAround.MAP_CODEC;
                case OPEN_DOOR -> OpenDoor.MAP_CODEC;
                case RANGED_ATTACK -> RangedAttack.MAP_CODEC;
                case MELEE_ATTACK -> MeleeAttack.MAP_CODEC;
                case LEAP_AT_TARGET -> LeapAtTarget.MAP_CODEC;
                case BREED -> Breed.MAP_CODEC;
                case FOLLOW_OWNER -> FollowOwner.MAP_CODEC;
                case SIT_WHEN_ORDERED -> SitWhenOrdered.MAP_CODEC;
                case DALEK_FLIGHT -> DalekFlight.MAP_CODEC;
                case TAMABLE_PANIC -> TamablePanic.MAP_CODEC;
                case HURT_BY_TARGET -> HurtByTarget.MAP_CODEC;
                case NEAREST_ATTACKABLE_TARGET -> NearestAttackableTarget.MAP_CODEC;
                case OWNER_HURT_BY_TARGET -> OwnerHurtByTarget.MAP_CODEC;
                case OWNER_HURT_TARGET -> OwnerHurtTarget.MAP_CODEC;
                case RESET_UNIVERSAL_ANGER -> ResetUniversalAnger.MAP_CODEC;
                case DALEK_SHARE_TARGET -> DalekShareTarget.MAP_CODEC;
            };
        }
    }

    record FloatOnWater() implements EntityGoal {
        static final FloatOnWater INSTANCE = new FloatOnWater();
        static final MapCodec<FloatOnWater> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.FLOAT;
        }

        @Override
        public Goal create(Mob mob) {
            return new FloatGoal(mob);
        }
    }

    record Panic(double speed) implements EntityGoal {
        static final MapCodec<Panic> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(Panic::speed)
        ).apply(instance, Panic::new));

        @Override
        public Kind kind() {
            return Kind.PANIC;
        }

        @Override
        public Goal create(Mob mob) {
            return new PanicGoal(require(mob, PathfinderMob.class, kind()), speed);
        }
    }

    record WaterAvoidingRandomStroll(double speed) implements EntityGoal {
        static final MapCodec<WaterAvoidingRandomStroll> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(WaterAvoidingRandomStroll::speed)
        ).apply(instance, WaterAvoidingRandomStroll::new));

        @Override
        public Kind kind() {
            return Kind.WATER_AVOIDING_RANDOM_STROLL;
        }

        @Override
        public Goal create(Mob mob) {
            return new WaterAvoidingRandomStrollGoal(require(mob, PathfinderMob.class, kind()), speed);
        }
    }

    record WaterAvoidingRandomFlying(double speed) implements EntityGoal {
        static final MapCodec<WaterAvoidingRandomFlying> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(WaterAvoidingRandomFlying::speed)
        ).apply(instance, WaterAvoidingRandomFlying::new));

        @Override
        public Kind kind() {
            return Kind.WATER_AVOIDING_RANDOM_FLYING;
        }

        @Override
        public Goal create(Mob mob) {
            return new WaterAvoidingRandomFlyingGoal(require(mob, PathfinderMob.class, kind()), speed);
        }
    }

    record LookAt(Identifier target, float lookDistance) implements EntityGoal {
        static final MapCodec<LookAt> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("target").forGetter(LookAt::target),
                Codec.FLOAT.fieldOf("look_distance").forGetter(LookAt::lookDistance)
        ).apply(instance, LookAt::new));

        @Override
        public Kind kind() {
            return Kind.LOOK_AT;
        }

        @Override
        public Goal create(Mob mob) {
            return new LookAtPlayerGoal(mob, livingClass(target), lookDistance);
        }
    }

    record RandomLookAround() implements EntityGoal {
        static final RandomLookAround INSTANCE = new RandomLookAround();
        static final MapCodec<RandomLookAround> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.RANDOM_LOOK_AROUND;
        }

        @Override
        public Goal create(Mob mob) {
            return new RandomLookAroundGoal(mob);
        }
    }

    record OpenDoor(boolean closeDoor) implements EntityGoal {
        static final MapCodec<OpenDoor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("close_door").forGetter(OpenDoor::closeDoor)
        ).apply(instance, OpenDoor::new));

        @Override
        public Kind kind() {
            return Kind.OPEN_DOOR;
        }

        @Override
        public Goal create(Mob mob) {
            return new OpenDoorGoal(mob, closeDoor);
        }
    }

    record RangedAttack(double speed, int interval, float attackRange) implements EntityGoal {
        static final MapCodec<RangedAttack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(RangedAttack::speed),
                Codec.INT.fieldOf("interval").forGetter(RangedAttack::interval),
                Codec.FLOAT.fieldOf("attack_range").forGetter(RangedAttack::attackRange)
        ).apply(instance, RangedAttack::new));

        @Override
        public Kind kind() {
            return Kind.RANGED_ATTACK;
        }

        @Override
        public Goal create(Mob mob) {
            RangedAttackMob ranged = require(mob, RangedAttackMob.class, kind());
            return new RangedAttackGoal(ranged, speed, interval, attackRange);
        }
    }

    record MeleeAttack(double speed, boolean followingIfNotSeen) implements EntityGoal {
        static final MapCodec<MeleeAttack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(MeleeAttack::speed),
                Codec.BOOL.fieldOf("following_if_not_seen").forGetter(MeleeAttack::followingIfNotSeen)
        ).apply(instance, MeleeAttack::new));

        @Override
        public Kind kind() {
            return Kind.MELEE_ATTACK;
        }

        @Override
        public Goal create(Mob mob) {
            return new MeleeAttackGoal(require(mob, PathfinderMob.class, kind()), speed, followingIfNotSeen);
        }
    }

    record LeapAtTarget(float ydelta) implements EntityGoal {
        static final MapCodec<LeapAtTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("ydelta").forGetter(LeapAtTarget::ydelta)
        ).apply(instance, LeapAtTarget::new));

        @Override
        public Kind kind() {
            return Kind.LEAP_AT_TARGET;
        }

        @Override
        public Goal create(Mob mob) {
            return new LeapAtTargetGoal(mob, ydelta);
        }
    }

    record Breed(double speed) implements EntityGoal {
        static final MapCodec<Breed> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(Breed::speed)
        ).apply(instance, Breed::new));

        @Override
        public Kind kind() {
            return Kind.BREED;
        }

        @Override
        public Goal create(Mob mob) {
            return new BreedGoal(require(mob, Animal.class, kind()), speed);
        }
    }

    record FollowOwner(double speed, float startDistance, float stopDistance) implements EntityGoal {
        static final MapCodec<FollowOwner> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(FollowOwner::speed),
                Codec.FLOAT.fieldOf("start_distance").forGetter(FollowOwner::startDistance),
                Codec.FLOAT.fieldOf("stop_distance").forGetter(FollowOwner::stopDistance)
        ).apply(instance, FollowOwner::new));

        @Override
        public Kind kind() {
            return Kind.FOLLOW_OWNER;
        }

        @Override
        public Goal create(Mob mob) {
            return new FollowOwnerGoal(require(mob, TamableAnimal.class, kind()), speed, startDistance, stopDistance);
        }
    }

    record SitWhenOrdered() implements EntityGoal {
        static final SitWhenOrdered INSTANCE = new SitWhenOrdered();
        static final MapCodec<SitWhenOrdered> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.SIT_WHEN_ORDERED;
        }

        @Override
        public Goal create(Mob mob) {
            return new SitWhenOrderedToGoal(require(mob, TamableAnimal.class, kind()));
        }
    }

    record DalekFlight(double flyYDelta, double minDistanceForMissingPath) implements EntityGoal {
        static final MapCodec<DalekFlight> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("fly_y_delta").forGetter(DalekFlight::flyYDelta),
                Codec.DOUBLE.fieldOf("min_distance_for_missing_path").forGetter(DalekFlight::minDistanceForMissingPath)
        ).apply(instance, DalekFlight::new));

        @Override
        public Kind kind() {
            return Kind.DALEK_FLIGHT;
        }

        @Override
        public Goal create(Mob mob) {
            return new DalekFlightGoal(require(mob, DalekEntity.class, kind()), flyYDelta, minDistanceForMissingPath);
        }
    }

    record TamablePanic(double speed, Identifier damageTypes) implements EntityGoal {
        static final MapCodec<TamablePanic> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("speed").forGetter(TamablePanic::speed),
                Identifier.CODEC.fieldOf("damage_types").forGetter(TamablePanic::damageTypes)
        ).apply(instance, TamablePanic::new));

        @Override
        public Kind kind() {
            return Kind.TAMABLE_PANIC;
        }

        @Override
        public Goal create(Mob mob) {
            TamableAnimal tamable = require(mob, TamableAnimal.class, kind());
            TagKey<DamageType> tag = TagKey.create(Registries.DAMAGE_TYPE, damageTypes);
            return tamable.new TamableAnimalPanicGoal(speed, tag);
        }
    }

    record HurtByTarget(boolean alertOthers) implements EntityGoal {
        static final MapCodec<HurtByTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("alert_others", false).forGetter(HurtByTarget::alertOthers)
        ).apply(instance, HurtByTarget::new));

        @Override
        public Kind kind() {
            return Kind.HURT_BY_TARGET;
        }

        @Override
        public Goal create(Mob mob) {
            HurtByTargetGoal goal = new HurtByTargetGoal(require(mob, PathfinderMob.class, kind()));
            if (alertOthers) {
                goal.setAlertOthers();
            }
            return goal;
        }
    }

    record NearestAttackableTarget(
            Identifier target,
            boolean mustSee,
            Optional<Boolean> mustReach,
            Optional<Integer> interval,
            Optional<TargetFilter> filter
    ) implements EntityGoal {
        static final MapCodec<NearestAttackableTarget> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("target").forGetter(NearestAttackableTarget::target),
                Codec.BOOL.fieldOf("must_see").forGetter(NearestAttackableTarget::mustSee),
                Codec.BOOL.optionalFieldOf("must_reach").forGetter(NearestAttackableTarget::mustReach),
                Codec.INT.optionalFieldOf("interval").forGetter(NearestAttackableTarget::interval),
                TargetFilter.CODEC.optionalFieldOf("filter").forGetter(NearestAttackableTarget::filter)
        ).apply(instance, NearestAttackableTarget::new));

        @Override
        public Kind kind() {
            return Kind.NEAREST_ATTACKABLE_TARGET;
        }

        @Override
        public Goal create(Mob mob) {
            Class<? extends net.minecraft.world.entity.LivingEntity> targetClass = livingClass(target);
            if (mustReach.isEmpty() && interval.isEmpty() && filter.isEmpty()) {
                return new NearestAttackableTargetGoal<>(mob, targetClass, mustSee);
            }
            TargetingConditions.Selector selector = filter.map(value -> value.selector(mob)).orElse(null);
            return new NearestAttackableTargetGoal<>(
                    mob,
                    targetClass,
                    interval.orElse(10),
                    mustSee,
                    mustReach.orElse(false),
                    selector
            );
        }
    }

    record OwnerHurtByTarget() implements EntityGoal {
        static final OwnerHurtByTarget INSTANCE = new OwnerHurtByTarget();
        static final MapCodec<OwnerHurtByTarget> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.OWNER_HURT_BY_TARGET;
        }

        @Override
        public Goal create(Mob mob) {
            return new OwnerHurtByTargetGoal(require(mob, TamableAnimal.class, kind()));
        }
    }

    record OwnerHurtTarget() implements EntityGoal {
        static final OwnerHurtTarget INSTANCE = new OwnerHurtTarget();
        static final MapCodec<OwnerHurtTarget> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.OWNER_HURT_TARGET;
        }

        @Override
        public Goal create(Mob mob) {
            return new OwnerHurtTargetGoal(require(mob, TamableAnimal.class, kind()));
        }
    }

    record ResetUniversalAnger(boolean alertOthers) implements EntityGoal {
        static final MapCodec<ResetUniversalAnger> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("alert_others").forGetter(ResetUniversalAnger::alertOthers)
        ).apply(instance, ResetUniversalAnger::new));

        @Override
        public Kind kind() {
            return Kind.RESET_UNIVERSAL_ANGER;
        }

        @Override
        public Goal create(Mob mob) {
            if (!(mob instanceof NeutralMob)) {
                throw new IllegalStateException(kind().getSerializedName() + " requires NeutralMob, got "
                        + mob.getClass().getSimpleName());
            }
            return resetAnger(mob, alertOthers);
        }

        private static <T extends Mob & NeutralMob> Goal resetAnger(Mob mob, boolean alertOthers) {
            @SuppressWarnings("unchecked")
            T typed = (T) mob;
            return new ResetUniversalAngerTargetGoal<>(typed, alertOthers);
        }
    }

    record DalekShareTarget() implements EntityGoal {
        static final DalekShareTarget INSTANCE = new DalekShareTarget();
        static final MapCodec<DalekShareTarget> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Kind kind() {
            return Kind.DALEK_SHARE_TARGET;
        }

        @Override
        public Goal create(Mob mob) {
            return new DalekShareTargetGoal(require(mob, DalekEntity.class, kind()));
        }
    }

    private static <T> T require(Mob mob, Class<T> type, Kind kind) {
        if (!type.isInstance(mob)) {
            throw new IllegalStateException(kind.getSerializedName() + " requires " + type.getSimpleName()
                    + ", got " + mob.getClass().getSimpleName());
        }
        return type.cast(mob);
    }

    /**
     * Minecraft 26.2 {@code EntityType.getBaseClass()} always returns {@code Entity.class},
     * so look/target goals currently resolve {@code minecraft:player} only.
     */
    static Class<? extends net.minecraft.world.entity.LivingEntity> livingClass(Identifier id) {
        if (id.equals(Identifier.withDefaultNamespace("player"))) {
            return Player.class;
        }
        throw new IllegalArgumentException("Unsupported look/target entity class: " + id
                + " (only minecraft:player is supported)");
    }
}
