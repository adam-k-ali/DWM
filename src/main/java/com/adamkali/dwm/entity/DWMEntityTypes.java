package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class DWMEntityTypes {
    public static EntityType<BoatEntity> ASH_BOAT;

    private DWMEntityTypes() {
    }

    public static void initialize() {
        Identifier id = Identifier.of(DWMReference.MOD_ID, "ash_boat");
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        ASH_BOAT = Registry.register(
                Registries.ENTITY_TYPE,
                key,
                EntityType.Builder.<BoatEntity>create(
                                (entityType, world) -> new BoatEntity(entityType, world, () -> DWMItems.ASH_BOAT),
                                SpawnGroup.MISC
                        )
                        .dropsNothing()
                        .dimensions(1.375F, 0.5625F)
                        .eyeHeight(0.5625F)
                        .maxTrackingRange(10)
                        .build(key)
        );
    }
}
