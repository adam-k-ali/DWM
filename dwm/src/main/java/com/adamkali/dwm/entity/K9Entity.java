package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * K-9, the robot dog companion. Only obtained from the K-9 unit item (spawned already tamed to the
 * player). Follows its owner, sits (halts) when ordered, and defends its owner. No breeding, food or
 * natural spawning. Attributes and goals live in {@code data/dwm/entity/definition/k9.json}.
 */
public class K9Entity extends TamableAnimal {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "k9");

    public K9Entity(EntityType<? extends K9Entity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    protected void registerGoals() {
        EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            EntityDefinitions.applyAttributeOverride(this, "tamed");
            this.setHealth(this.getMaxHealth());
        } else {
            EntityDefinitions.applyAttributeOverride(this, EntityDefinitions.DEFAULT_OVERRIDE);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        this.setOrderedToSit(false);
        return super.hurtServer(level, source, damage);
    }

    /** The owner toggles sit with an empty-handed or any-item interaction; others are ignored. */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
            }
            return InteractionResult.SUCCESS.withoutItem();
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean canMate(Animal partner) {
        return false;
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper) {
            return false;
        }
        if (target instanceof Player playerTarget && owner instanceof Player playerOwner
                && !playerOwner.canHarmPlayer(playerTarget)) {
            return false;
        }
        return !(target instanceof TamableAnimal animal && animal.isTame());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
