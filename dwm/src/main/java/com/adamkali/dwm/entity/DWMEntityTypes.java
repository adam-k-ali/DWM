package com.adamkali.dwm.entity;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.block.wood.RegisteredWoodFamily;
import com.adamkali.dwm.block.wood.WoodFamilyRegistrar;
import com.adamkali.dwm.entity.definition.EntityDefinitions;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.BlockTags;

import java.util.function.Supplier;

public final class DWMEntityTypes {
    public static EntityType<Boat> ASH_BOAT;
    public static EntityType<Boat> DARK_ASH_BOAT;
    public static EntityType<Boat> CARDINAL_BOAT;
    public static EntityType<TardisSeatEntity> TARDIS_SEAT;
    public static EntityType<ConsoleControlInteractionEntity> CONSOLE_CONTROL;
    public static EntityType<BroakirEntity> BROAKIR;
    public static EntityType<FlutterwingEntity> FLUTTERWING;
    public static EntityType<MewingDogEntity> MEWING_DOG;
    public static EntityType<TimeLordEntity> TIME_LORD;
    public static EntityType<ThalEntity> THAL;
    public static EntityType<DalekEntity> DALEK;
    public static EntityType<ParadigmDalekEntity> PARADIGM_DALEK;
    public static EntityType<DalekMutantEntity> DALEK_MUTANT;
    public static EntityType<DalekLaserEntity> DALEK_LASER;
    public static EntityType<CyberMondanEntity> CYBERMONDAN;
    public static EntityType<CybusCybermanEntity> CYBUS_CYBERMAN;
    public static EntityType<CybermasterEntity> CYBERMASTER;
    public static EntityType<WeaponsGradeCybermanEntity> WEAPONS_GRADE_CYBERMAN;

    private DWMEntityTypes() {
    }

    public static void initialize() {
        EntityDefinitions.initialize();

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
        MEWING_DOG = registerMewingDog();
        TIME_LORD = registerTimeLord();
        THAL = registerThal();
        DALEK = registerDalek();
        PARADIGM_DALEK = registerParadigmDalek();
        DALEK_MUTANT = registerDalekMutant();
        DALEK_LASER = registerDalekLaser();
        CYBERMONDAN = registerCyberman(
                CyberMondanEntity.ID, CyberMondanEntity::new, CyberMondanEntity::createAttributes);
        CYBUS_CYBERMAN = registerCyberman(
                CybusCybermanEntity.ID, CybusCybermanEntity::new, CybusCybermanEntity::createAttributes);
        CYBERMASTER = registerCyberman(
                CybermasterEntity.ID, CybermasterEntity::new, CybermasterEntity::createAttributes);
        WEAPONS_GRADE_CYBERMAN = registerCyberman(
                WeaponsGradeCybermanEntity.ID, WeaponsGradeCybermanEntity::new, WeaponsGradeCybermanEntity::createAttributes);
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

    private static EntityType<MewingDogEntity> registerMewingDog() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "mewing_dog");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                MewingDogEntity::new,
                                MobCategory.CREATURE,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                (type, level, reason, pos, random) ->
                                                        Animal.checkAnimalSpawnRules(type, level, reason, pos, random)
                                        )
                                        .defaultAttributes(MewingDogEntity::createAttributes)
                        )
                        .sized(0.6F, 0.85F)
                        .eyeHeight(0.68F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<TimeLordEntity> registerTimeLord() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "time_lord");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                TimeLordEntity::new,
                                MobCategory.CREATURE,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                (type, level, reason, pos, random) ->
                                                        Mob.checkMobSpawnRules(type, level, reason, pos, random)
                                                                && level.getBlockState(pos.below()).is(BlockTags.DIRT)
                                        )
                                        .defaultAttributes(TimeLordEntity::createAttributes)
                        )
                        .sized(0.6F, 1.95F)
                        .eyeHeight(1.62F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<ThalEntity> registerThal() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ThalEntity.ID);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                ThalEntity::new,
                                MobCategory.CREATURE,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                ThalSpawnRules::checkSpawnRules
                                        )
                                        .defaultAttributes(ThalEntity::createAttributes)
                        )
                        .sized(0.6F, 1.95F)
                        .eyeHeight(1.62F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<DalekEntity> registerDalek() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                DalekEntity::new,
                                MobCategory.MONSTER,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                DalekPatrolLogic::canSpawn
                                        )
                                        .defaultAttributes(DalekEntity::createAttributes)
                        )
                        .sized(0.8F, 1.7F)
                        .eyeHeight(1.5F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<ParadigmDalekEntity> registerParadigmDalek() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ParadigmDalekEntity.ID);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                ParadigmDalekEntity::new,
                                MobCategory.MONSTER,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                DalekPatrolLogic::canSpawn
                                        )
                                        .defaultAttributes(ParadigmDalekEntity::createAttributes)
                        )
                        .sized(1.0F, 2.4F)
                        .eyeHeight(2.1F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<DalekMutantEntity> registerDalekMutant() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, DalekMutantEntity.ID);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                DalekMutantEntity::new,
                                MobCategory.MONSTER,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                DalekPatrolLogic::canSpawn
                                        )
                                        .defaultAttributes(DalekMutantEntity::createAttributes)
                        )
                        .sized(0.7F, 0.7F)
                        .eyeHeight(0.4F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    /**
     * Cybermen are player-sized hostile humanoids. They have no biome spawner entries, so the vanilla
     * monster placement rule only matters for spawn eggs, commands and spawners.
     */
    private static <T extends CybermanEntity> EntityType<T> registerCyberman(
            Identifier id,
            EntityType.EntityFactory<T> factory,
            Supplier<AttributeSupplier.Builder> attributes
    ) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                FabricEntityType.Builder.createMob(
                                factory,
                                MobCategory.MONSTER,
                                mob -> mob
                                        .spawnPlacement(
                                                SpawnPlacementTypes.ON_GROUND,
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                Monster::checkMonsterSpawnRules
                                        )
                                        .defaultAttributes(attributes)
                        )
                        .sized(0.6F, 1.95F)
                        .eyeHeight(1.62F)
                        .clientTrackingRange(10)
                        .build(key)
        );
    }

    private static EntityType<DalekLaserEntity> registerDalekLaser() {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dalek_laser");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.<DalekLaserEntity>of(DalekLaserEntity::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(8)
                        .updateInterval(1)
                        .noSummon()
                        .noLootTable()
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
