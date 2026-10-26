package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class DWMEntityTypeTags {
    /** Entity types that {@code dwm:avoid_entity} goals on Thals flee from (the Daleks). */
    public static final TagKey<EntityType<?>> THAL_FEARS = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "thal_fears")
    );

    private DWMEntityTypeTags() {
    }
}
