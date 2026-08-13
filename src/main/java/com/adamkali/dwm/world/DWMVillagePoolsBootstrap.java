package com.adamkali.dwm.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.placement.VillagePlacements;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import java.util.List;
import java.util.function.Function;

/**
 * Clones vanilla plains village template pools, swapping processors to
 * {@code dwm:gallifrey_village} and trees to the Ash village placed feature.
 * Piece locations stay vanilla so NBT is not copied.
 */
public final class DWMVillagePoolsBootstrap {
    private DWMVillagePoolsBootstrap() {
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> gallifrey = processors.getOrThrow(DWMProcessorLists.GALLIFREY_VILLAGE);

        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> ashVillage = placedFeatures.getOrThrow(DWMPlacedFeatures.ASH_VILLAGE);
        Holder<PlacedFeature> flowerPlainVillage = placedFeatures.getOrThrow(VillagePlacements.FLOWER_PLAIN_VILLAGE);
        Holder<PlacedFeature> pileHayVillage = placedFeatures.getOrThrow(VillagePlacements.PILE_HAY_VILLAGE);

        context.register(
                DWMVillagePools.TERMINATORS,
                new StructureTemplatePool(
                        empty,
                        List.of(
                                legacy("village/plains/terminators/terminator_01", gallifrey, 1),
                                legacy("village/plains/terminators/terminator_02", gallifrey, 1),
                                legacy("village/plains/terminators/terminator_03", gallifrey, 1),
                                legacy("village/plains/terminators/terminator_04", gallifrey, 1)
                        ),
                        StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
        );

        Holder<StructureTemplatePool> terminators = pools.getOrThrow(DWMVillagePools.TERMINATORS);

        context.register(
                DWMVillagePools.TOWN_CENTERS,
                new StructureTemplatePool(
                        empty,
                        List.of(
                                legacy("village/plains/town_centers/plains_fountain_01", gallifrey, 50),
                                legacy("village/plains/town_centers/plains_meeting_point_1", gallifrey, 50),
                                legacy("village/plains/town_centers/plains_meeting_point_2", gallifrey, 50),
                                legacy("village/plains/town_centers/plains_meeting_point_3", gallifrey, 50)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );

        context.register(
                DWMVillagePools.STREETS,
                new StructureTemplatePool(
                        terminators,
                        List.of(
                                legacy("village/plains/streets/corner_01", gallifrey, 2),
                                legacy("village/plains/streets/corner_02", gallifrey, 2),
                                legacy("village/plains/streets/corner_03", gallifrey, 2),
                                legacy("village/plains/streets/straight_01", gallifrey, 4),
                                legacy("village/plains/streets/straight_02", gallifrey, 4),
                                legacy("village/plains/streets/straight_03", gallifrey, 7),
                                legacy("village/plains/streets/straight_04", gallifrey, 7),
                                legacy("village/plains/streets/straight_05", gallifrey, 3),
                                legacy("village/plains/streets/straight_06", gallifrey, 4),
                                legacy("village/plains/streets/crossroad_01", gallifrey, 2),
                                legacy("village/plains/streets/crossroad_02", gallifrey, 1),
                                legacy("village/plains/streets/crossroad_03", gallifrey, 2),
                                legacy("village/plains/streets/crossroad_04", gallifrey, 2),
                                legacy("village/plains/streets/crossroad_05", gallifrey, 2),
                                legacy("village/plains/streets/crossroad_06", gallifrey, 2),
                                legacy("village/plains/streets/turn_01", gallifrey, 3)
                        ),
                        StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
        );

        context.register(
                DWMVillagePools.HOUSES,
                new StructureTemplatePool(
                        terminators,
                        List.of(
                                legacy("village/plains/houses/plains_small_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_small_house_2", gallifrey, 2),
                                legacy("village/plains/houses/plains_small_house_3", gallifrey, 2),
                                legacy("village/plains/houses/plains_small_house_4", gallifrey, 2),
                                legacy("village/plains/houses/plains_small_house_5", gallifrey, 2),
                                legacy("village/plains/houses/plains_small_house_6", gallifrey, 1),
                                legacy("village/plains/houses/plains_small_house_7", gallifrey, 2),
                                legacy("village/plains/houses/plains_small_house_8", gallifrey, 3),
                                legacy("village/plains/houses/plains_medium_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_medium_house_2", gallifrey, 2),
                                legacy("village/plains/houses/plains_big_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_butcher_shop_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_butcher_shop_2", gallifrey, 2),
                                legacy("village/plains/houses/plains_tool_smith_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_fletcher_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_shepherds_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_armorer_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_fisher_cottage_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_tannery_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_cartographer_1", gallifrey, 1),
                                legacy("village/plains/houses/plains_library_1", gallifrey, 5),
                                legacy("village/plains/houses/plains_library_2", gallifrey, 1),
                                legacy("village/plains/houses/plains_masons_house_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_weaponsmith_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_temple_3", gallifrey, 2),
                                legacy("village/plains/houses/plains_temple_4", gallifrey, 2),
                                legacy("village/plains/houses/plains_stable_1", gallifrey, 2),
                                legacy("village/plains/houses/plains_stable_2", gallifrey, 2),
                                legacy("village/plains/houses/plains_large_farm_1", gallifrey, 4),
                                legacy("village/plains/houses/plains_small_farm_1", gallifrey, 4),
                                legacy("village/plains/houses/plains_animal_pen_1", gallifrey, 1),
                                legacy("village/plains/houses/plains_animal_pen_2", gallifrey, 1),
                                legacy("village/plains/houses/plains_animal_pen_3", gallifrey, 5),
                                legacy("village/plains/houses/plains_accessory_1", gallifrey, 1),
                                legacy("village/plains/houses/plains_meeting_point_4", gallifrey, 3),
                                legacy("village/plains/houses/plains_meeting_point_5", gallifrey, 1),
                                Pair.of(StructurePoolElement.empty(), 10)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );

        context.register(
                DWMVillagePools.TREES,
                new StructureTemplatePool(
                        empty,
                        List.of(Pair.of(StructurePoolElement.feature(ashVillage), 1)),
                        StructureTemplatePool.Projection.RIGID
                )
        );

        context.register(
                DWMVillagePools.DECOR,
                new StructureTemplatePool(
                        empty,
                        List.of(
                                legacy("village/plains/plains_lamp_1", gallifrey, 2),
                                Pair.of(StructurePoolElement.feature(ashVillage), 1),
                                Pair.of(StructurePoolElement.feature(flowerPlainVillage), 1),
                                Pair.of(StructurePoolElement.feature(pileHayVillage), 1),
                                Pair.of(StructurePoolElement.empty(), 2)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
    }

    private static Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> legacy(
            String location,
            Holder<StructureProcessorList> processors,
            int weight
    ) {
        return Pair.of(StructurePoolElement.legacy(location, processors), weight);
    }
}
