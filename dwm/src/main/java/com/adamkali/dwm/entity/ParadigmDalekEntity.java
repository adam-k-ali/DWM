package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/**
 * Paradigm (New Dalek Paradigm) Dalek: a taller, heavier casing in five caste colours. Shares the
 * core Dalek flight, laser and patrol behaviour; stats come from {@code paradigm_dalek.json}.
 */
public class ParadigmDalekEntity extends DalekEntity {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "paradigm_dalek");

    private static final float VOICE_PITCH_SCALE = 0.85F;

    public ParadigmDalekEntity(EntityType<? extends ParadigmDalekEntity> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    public DalekChassis chassis() {
        return DalekChassis.PARADIGM;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * VOICE_PITCH_SCALE;
    }
}
