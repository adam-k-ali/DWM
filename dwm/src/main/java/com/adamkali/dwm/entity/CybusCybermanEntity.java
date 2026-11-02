package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import com.adamkali.dwm.sound.DWMSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/**
 * Cybus Cyberman: the heavy steel Cybus Industries design. Its melee grip electrocutes the target
 * ({@link CybusElectricGrip}). Stats and AI come from {@code cybus_cyberman.json}.
 */
public class CybusCybermanEntity extends CybermanEntity {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cybus_cyberman");

    public CybusCybermanEntity(EntityType<? extends CybusCybermanEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(CybusElectricGrip.slowness(), this);
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    living.getX(), living.getY(0.5), living.getZ(),
                    CybusElectricGrip.SPARK_COUNT,
                    living.getBbWidth() * 0.5, living.getBbHeight() * 0.3, living.getBbWidth() * 0.5,
                    0.1
            );
            this.playSound(DWMSounds.CYBUS_CYBERMAN_ZAP, 1.0F, 1.0F);
        }
        return hit;
    }
}
