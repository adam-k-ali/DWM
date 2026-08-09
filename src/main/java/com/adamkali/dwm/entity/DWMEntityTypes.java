package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
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
    public static EntityType<BoatEntity> DARK_ASH_BOAT;

    private DWMEntityTypes() {
    }

    public static void initialize() {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyRegistrar.registerBoatEntity(family);
        }
        ASH_BOAT = DWMBlocks.ASH.boatEntity();
        DARK_ASH_BOAT = DWMBlocks.DARK_ASH.boatEntity();
    }

    public static EntityType<BoatEntity> registerBoat(String path, java.util.function.Supplier<net.minecraft.item.Item> boatItem) {
        Identifier id = Identifier.of(DWMReference.MOD_ID, path);
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        return Registry.register(
                Registries.ENTITY_TYPE,
                key,
                EntityType.Builder.<BoatEntity>create(
                                (entityType, world) -> new BoatEntity(entityType, world, boatItem),
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
