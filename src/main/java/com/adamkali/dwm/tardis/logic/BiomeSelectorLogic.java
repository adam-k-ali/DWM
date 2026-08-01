package com.adamkali.dwm.tardis.logic;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pure helpers for cycling biomes tagged for the TARDIS exterior dimension.
 */
public final class BiomeSelectorLogic {
    private BiomeSelectorLogic() {
    }

    /**
     * Returns the biome tag used for the given exterior dimension id, or empty if unsupported.
     */
    public static Optional<TagKey<Biome>> tagForDimension(@Nullable String exteriorDimensionId) {
        if (exteriorDimensionId == null || exteriorDimensionId.isBlank()) {
            return Optional.empty();
        }
        Identifier id = Identifier.tryParse(exteriorDimensionId);
        if (id == null) {
            return Optional.empty();
        }
        if (World.OVERWORLD.getValue().equals(id)) {
            return Optional.of(BiomeTags.IS_OVERWORLD);
        }
        if (World.NETHER.getValue().equals(id)) {
            return Optional.of(BiomeTags.IS_NETHER);
        }
        if (World.END.getValue().equals(id)) {
            return Optional.of(BiomeTags.IS_END);
        }
        return Optional.empty();
    }

    /**
     * Sorted biome registry keys belonging to the dimension's biome tag.
     */
    public static List<RegistryKey<Biome>> biomesForDimension(
            Registry<Biome> biomeRegistry,
            @Nullable String exteriorDimensionId
    ) {
        Optional<TagKey<Biome>> tag = tagForDimension(exteriorDimensionId);
        if (tag.isEmpty()) {
            return List.of();
        }
        List<RegistryKey<Biome>> keys = new ArrayList<>();
        for (RegistryEntry<Biome> entry : biomeRegistry.iterateEntries(tag.get())) {
            entry.getKey().ifPresent(keys::add);
        }
        keys.sort(Comparator.comparing(k -> k.getValue().toString()));
        return List.copyOf(keys);
    }

    /**
     * Next biome after {@code currentId} in {@code biomes}, wrapping to the first.
     * Null / missing current selects the first entry. Empty list → empty.
     */
    public static Optional<Identifier> nextBiome(
            @Nullable String currentId,
            List<RegistryKey<Biome>> biomes
    ) {
        if (biomes.isEmpty()) {
            return Optional.empty();
        }
        if (currentId == null || currentId.isBlank()) {
            return Optional.of(biomes.getFirst().getValue());
        }
        Identifier current = Identifier.tryParse(currentId);
        int index = -1;
        if (current != null) {
            for (int i = 0; i < biomes.size(); i++) {
                if (biomes.get(i).getValue().equals(current)) {
                    index = i;
                    break;
                }
            }
        }
        int next = index < 0 ? 0 : (index + 1) % biomes.size();
        return Optional.of(biomes.get(next).getValue());
    }
}
