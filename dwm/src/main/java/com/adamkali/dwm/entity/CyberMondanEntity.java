package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/**
 * CyberMondan: an original Mondasian Cyberman with a cloth-wrapped face, fabric suit, chest unit and
 * headlamp. Slow but relentless; it shrugs off knockback. Stats come from {@code cybermondan.json}.
 */
public class CyberMondanEntity extends CybermanEntity {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cybermondan");

    public CyberMondanEntity(EntityType<? extends CyberMondanEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityDefinitions.createAttributes(ID);
    }
}
