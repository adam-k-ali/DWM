package com.adamkali.dwm.world.radiation;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class DWMDamageTypes {
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "radiation")
    );

    private DWMDamageTypes() {
    }
}
