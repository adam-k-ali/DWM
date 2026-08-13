package com.adamkali.dwm.world;

import com.adamkali.dwm.DWMReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import java.util.ArrayList;
import java.util.List;

public final class DWMVillagePools {
    public static final ResourceKey<StructureTemplatePool> TOWN_CENTERS = key("village/gallifrey/town_centers");
    public static final ResourceKey<StructureTemplatePool> STREETS = key("village/gallifrey/streets");
    public static final ResourceKey<StructureTemplatePool> HOUSES = key("village/gallifrey/houses");
    public static final ResourceKey<StructureTemplatePool> TERMINATORS = key("village/gallifrey/terminators");
    public static final ResourceKey<StructureTemplatePool> DECOR = key("village/gallifrey/decor");
    public static final ResourceKey<StructureTemplatePool> TREES = key("village/gallifrey/trees");

    /**
     * Plains piece pools rewritten to Gallifrey pools (vanilla NBT still references these IDs).
     * Kept as Identifiers so unit tests do not touch {@link PoolAliasBinding} (needs registry bootstrap).
     */
    private static final List<String> PIECE_POOL_NAMES = List.of(
            "streets",
            "houses",
            "terminators",
            "decor",
            "trees"
    );

    /** Vanilla jigsaw pools that place villagers / cats / golems / pen animals. */
    private static final List<String> EMPTY_ENTITY_POOLS = List.of(
            "village/plains/villagers",
            "village/common/cats",
            "village/common/iron_golem",
            "village/common/animals"
    );

    private DWMVillagePools() {
    }

    public static List<PoolAliasBinding> poolAliases() {
        List<PoolAliasBinding> aliases = new ArrayList<>();
        for (String name : PIECE_POOL_NAMES) {
            aliases.add(pieceAlias(name));
        }
        for (String poolPath : EMPTY_ENTITY_POOLS) {
            aliases.add(emptyEntityAlias(poolPath));
        }
        return List.copyOf(aliases);
    }

    /** Source IDs for plains piece pool rewrites ({@code village/plains/...} → Gallifrey). */
    public static List<Identifier> plainsAliasSources() {
        return PIECE_POOL_NAMES.stream()
                .map(name -> Identifier.withDefaultNamespace("village/plains/" + name))
                .toList();
    }

    /** Source IDs for entity pools rewritten to {@code minecraft:empty}. */
    public static List<Identifier> emptyEntityAliasSources() {
        return EMPTY_ENTITY_POOLS.stream()
                .map(Identifier::withDefaultNamespace)
                .toList();
    }

    public static Identifier emptyEntityAliasTarget() {
        return Pools.EMPTY.identifier();
    }

    private static PoolAliasBinding pieceAlias(String name) {
        return PoolAliasBinding.direct(
                ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace("village/plains/" + name)),
                key("village/gallifrey/" + name)
        );
    }

    private static PoolAliasBinding emptyEntityAlias(String poolPath) {
        return PoolAliasBinding.direct(
                ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.withDefaultNamespace(poolPath)),
                Pools.EMPTY
        );
    }

    private static ResourceKey<StructureTemplatePool> key(String path) {
        return ResourceKey.create(
                Registries.TEMPLATE_POOL,
                Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path)
        );
    }
}
