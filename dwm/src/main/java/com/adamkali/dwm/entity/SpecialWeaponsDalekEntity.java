package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/**
 * Special Weapons Dalek: a heavy casing with no manipulator arm, a single large cannon and a rear
 * weapon pack. Shares the core Dalek flight, laser and patrol behaviour; stats come from {@code special_weapons_dalek.json}.
 */
public class SpecialWeaponsDalekEntity extends DalekEntity {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "special_weapons_dalek");

    private static final float VOICE_PITCH_SCALE = 0.85F;

    public SpecialWeaponsDalekEntity(EntityType<? extends SpecialWeaponsDalekEntity> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }

    @Override
    public DalekFamily family() {
        return DalekFamily.SPECIAL_WEAPONS;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * VOICE_PITCH_SCALE;
    }
}
