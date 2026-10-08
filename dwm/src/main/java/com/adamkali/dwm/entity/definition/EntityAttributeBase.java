package com.adamkali.dwm.entity.definition;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;

/**
 * Vanilla attribute template applied before JSON attribute values.
 */
public enum EntityAttributeBase implements StringRepresentable {
    ANIMAL("animal"),
    MOB("mob"),
    MONSTER("monster");

    public static final StringRepresentable.EnumCodec<EntityAttributeBase> CODEC =
            StringRepresentable.fromEnum(EntityAttributeBase::values);

    private final String serializedName;

    EntityAttributeBase(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public AttributeSupplier.Builder create() {
        return switch (this) {
            case ANIMAL -> Animal.createAnimalAttributes();
            case MOB -> Mob.createMobAttributes();
            case MONSTER -> Monster.createMonsterAttributes();
        };
    }
}
