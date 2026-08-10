package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.world.DWMBiomeTags;
import com.adamkali.dwm.world.GallifreyDimensions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

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
        if (Level.OVERWORLD.identifier().equals(id)) {
            return Optional.of(BiomeTags.IS_OVERWORLD);
        }
        if (Level.NETHER.identifier().equals(id)) {
            return Optional.of(BiomeTags.IS_NETHER);
        }
        if (Level.END.identifier().equals(id)) {
            return Optional.of(BiomeTags.IS_END);
        }
        if (GallifreyDimensions.DIMENSION_ID.equals(id)) {
            return Optional.of(DWMBiomeTags.IS_GALLIFREY);
        }
        return Optional.empty();
    }

    /**
     * Sorted biome registry keys belonging to the dimension's biome tag.
     */
    public static List<ResourceKey<Biome>> biomesForDimension(
            Registry<Biome> biomeRegistry,
            @Nullable String exteriorDimensionId
    ) {
        Optional<TagKey<Biome>> tag = tagForDimension(exteriorDimensionId);
        if (tag.isEmpty()) {
            return List.of();
        }
        List<ResourceKey<Biome>> keys = new ArrayList<>();
        for (Holder<Biome> entry : biomeRegistry.getTagOrEmpty(tag.get())) {
            entry.unwrapKey().ifPresent(keys::add);
        }
        keys.sort(Comparator.comparing(k -> k.identifier().toString()));
        return List.copyOf(keys);
    }

    /**
     * Next biome after {@code currentId} in {@code biomes}, wrapping to the first.
     * Null / missing current selects the first entry. Empty list → empty.
     */
    public static Optional<Identifier> nextBiome(
            @Nullable String currentId,
            List<ResourceKey<Biome>> biomes
    ) {
        if (biomes.isEmpty()) {
            return Optional.empty();
        }
        if (currentId == null || currentId.isBlank()) {
            return Optional.of(biomes.getFirst().identifier());
        }
        Identifier current = Identifier.tryParse(currentId);
        int index = -1;
        if (current != null) {
            for (int i = 0; i < biomes.size(); i++) {
                if (biomes.get(i).identifier().equals(current)) {
                    index = i;
                    break;
                }
            }
        }
        int next = index < 0 ? 0 : (index + 1) % biomes.size();
        return Optional.of(biomes.get(next).identifier());
    }
}
