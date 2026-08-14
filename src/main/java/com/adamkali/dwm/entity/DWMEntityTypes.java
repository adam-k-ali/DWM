package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;

public final class DWMEntityTypes {
    public static EntityType<Boat> ASH_BOAT;
    public static EntityType<Boat> DARK_ASH_BOAT;
    public static EntityType<Boat> CARDINAL_BOAT;
    public static EntityType<TardisSeatEntity> TARDIS_SEAT;

    private DWMEntityTypes() {
    }

    public static void initialize() {
        for (RegisteredWoodFamily family : DWMBlocks.WOOD_FAMILIES) {
            WoodFamilyRegistrar.registerBoatEntity(family);
        }
        ASH_BOAT = DWMBlocks.ASH.boatEntity();
        DARK_ASH_BOAT = DWMBlocks.DARK_ASH.boatEntity();
        CARDINAL_BOAT = DWMBlocks.CARDINAL.boatEntity();
        TARDIS_SEAT = registerSeat();
    }

    private static EntityType<TardisSeatEntity> registerSeat() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_seat");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.of(TardisSeatEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.1F)
                        .passengerAttachments(0.0F)
                        .noSummon()
                        .fireImmune()
                        .noLootTable()
                        .clientTrackingRange(10)
                        .updateInterval(Integer.MAX_VALUE)
                        .build(key)
        );
    }

    public static EntityType<Boat> registerBoat(String path, java.util.function.Supplier<net.minecraft.world.item.Item> boatItem) {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.<Boat>of(
                                (entityType, world) -> new Boat(entityType, world, boatItem),
                                MobCategory.MISC
                        )
                        .noLootTable()
                        .sized(1.375F, 0.5625F)
                        .eyeHeight(0.5625F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }
}
