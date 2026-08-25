package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.levelgen.Heightmap;

public final class DWMEntityTypes {
    public static EntityType<Boat> ASH_BOAT;
    public static EntityType<Boat> DARK_ASH_BOAT;
    public static EntityType<Boat> CARDINAL_BOAT;
    public static EntityType<TardisSeatEntity> TARDIS_SEAT;
    public static EntityType<ConsoleControlInteractionEntity> CONSOLE_CONTROL;
    public static EntityType<BroakirEntity> BROAKIR;
    public static EntityType<FlutterwingEntity> FLUTTERWING;

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
        CONSOLE_CONTROL = registerConsoleControl();
        BROAKIR = registerBroakir();
        FLUTTERWING = registerFlutterwing();
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

    private static EntityType<ConsoleControlInteractionEntity> registerConsoleControl() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "console_control");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.of(ConsoleControlInteractionEntity::new, MobCategory.MISC)
                        .sized(0.0F, 0.0F)
                        .noSummon()
                        .noSave()
                        .fireImmune()
                        .noLootTable()
                        .clientTrackingRange(10)
                        .updateInterval(Integer.MAX_VALUE)
                        .build(key)
        );
    }

    private static EntityType<BroakirEntity> registerBroakir() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "broakir");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                BroakirEntity::new,
                                MobCategory.CREATURE,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                (type, level, reason, pos, random) ->
                                                        Animal.checkAnimalSpawnRules(type, level, reason, pos, random)
                                        )
                                        .defaultAttributes(BroakirEntity::createAttributes)
                        )
                        .sized(1.4F, 2.3F)
                        .eyeHeight(2.0F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<FlutterwingEntity> registerFlutterwing() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "flutterwing");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                FlutterwingEntity::new,
                                MobCategory.CREATURE,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                (type, level, reason, pos, random) ->
                                                        Animal.checkAnimalSpawnRules(type, level, reason, pos, random)
                                        )
                                        .defaultAttributes(FlutterwingEntity::createAttributes)
                        )
                        .sized(0.9F * FlutterwingEntity.SCALE, 1.5F * FlutterwingEntity.SCALE)
                        .eyeHeight(1.2F * FlutterwingEntity.SCALE)
                        .clientTrackingRange(10)
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
