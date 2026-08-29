package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.DirectPoolAlias;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import java.util.List;

public final class DWMVillagePools {
    public static final ResourceKey<StructureTemplatePool> TOWN_CENTERS = key("village/gallifrey/town_centers");
    public static final ResourceKey<StructureTemplatePool> STREETS = key("village/gallifrey/streets");
    public static final ResourceKey<StructureTemplatePool> HOUSES = key("village/gallifrey/houses");
    public static final ResourceKey<StructureTemplatePool> TERMINATORS = key("village/gallifrey/terminators");
    public static final ResourceKey<StructureTemplatePool> DECOR = key("village/gallifrey/decor");
    public static final ResourceKey<StructureTemplatePool> TREES = key("village/gallifrey/trees");

    private DWMVillagePools() {
    }

    public static List<PoolAliasBinding> poolAliases() {
        return List.of(
                alias("streets"),
                alias("houses"),
                alias("terminators"),
                alias("decor"),
                alias("trees")
        );
    }

    public static List<Identifier> plainsAliasSources() {
        return poolAliases().stream()
                .map(DirectPoolAlias.class::cast)
                .map(binding -> binding.alias().identifier())
                .toList();
    }

    private static PoolAliasBinding alias(String name) {
        return PoolAliasBinding.direct(
                ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("village/plains/" + name)),
                key("village/gallifrey/" + name)
        );
    }

    private static ResourceKey<StructureTemplatePool> key(String path) {
        return ResourceKey.create(
                Registries.TEMPLATE_POOL,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path)
        );
    }
}
