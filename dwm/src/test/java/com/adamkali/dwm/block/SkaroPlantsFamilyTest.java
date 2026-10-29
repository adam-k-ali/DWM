package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.plant.ThalCropBlock;
import com.adamkali.dwm.item.DWMItems;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkaroPlantsFamilyTest {
    private static final Path BIOME_DIR = Path.of("src/main/generated/data/dwm/worldgen/biome");

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void skaroPlantsListHoldsPlaceablePlantsOnly() {
        assertEquals(4, DWMBlocks.SKARO_PLANTS.size());
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.VARGA_PLANT));
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.RADIATION_FUNGUS));
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.ASH_SCRUB));
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.MUTATED_REEDS));
        assertFalse(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.THAL_CROP), "crop is planted via seeds");
    }

    @Test
    void skaroPlantsDoNotOverlapGallifreyPlants() {
        for (var plant : DWMBlocks.SKARO_PLANTS) {
            assertFalse(DWMBlocks.GALLIFREY_PLANTS.contains(plant));
        }
        assertFalse(DWMBlocks.GALLIFREY_PLANTS.contains(DWMBlocks.THAL_CROP));
    }

    @Test
    void idsAreRegistered() {
        for (String id : List.of("varga_plant", "radiation_fungus", "ash_scrub", "mutated_reeds", "thal_crop")) {
            assertTrue(BuiltInRegistries.BLOCK.containsKey(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("dwm", id)), id);
        }
        assertTrue(BuiltInRegistries.ITEM.containsKey(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("dwm", "thal_seeds")));
    }

    @Test
    void fungusGlowsDimlyAndOthersDoNot() {
        assertEquals(3, DWMBlocks.RADIATION_FUNGUS.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.VARGA_PLANT.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.ASH_SCRUB.defaultBlockState().getLightEmission());
    }

    @Test
    void thalCropIsACropWithSeeds() {
        assertTrue(DWMBlocks.THAL_CROP instanceof ThalCropBlock);
        assertEquals(7, ((ThalCropBlock) DWMBlocks.THAL_CROP).getMaxAge());
        assertEquals(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("dwm", "thal_seeds"),
                BuiltInRegistries.ITEM.getKey(DWMItems.THAL_SEEDS));
    }

    @Test
    void biomesGetOnlyTheirDocumentedPlants() throws Exception {
        Map<String, List<String>> expected = Map.of(
                "skaro_irradiated_wastes.json", List.of("ash_scrub_wastes", "varga_wastes"),
                "skaro_petrified_jungle.json", List.of("radiation_fungus_jungle", "varga_jungle"),
                "skaro_drammankin_mire.json", List.of("mutated_reeds_mire", "radiation_fungus_mire", "varga_mire"),
                "skaro_drammankin_mountains.json", List.of("ash_scrub_mountains"),
                "skaro_thal_plateau.json", List.of()
        );
        List<String> all = List.of(
                "ash_scrub_wastes", "ash_scrub_mountains", "varga_wastes", "varga_jungle", "varga_mire",
                "radiation_fungus_jungle", "radiation_fungus_mire", "mutated_reeds_mire");
        for (var entry : expected.entrySet()) {
            String blob = Files.readString(BIOME_DIR.resolve(entry.getKey()));
            for (String feature : all) {
                assertEquals(
                        entry.getValue().contains(feature),
                        blob.contains("\"dwm:" + feature + "\""),
                        entry.getKey() + " / " + feature
                );
            }
            assertFalse(blob.contains("thal_crop"), entry.getKey() + " must not decorate with the Thal crop");
            assertFalse(blob.contains("flower_of_remembrance") || blob.contains("saccharine"), entry.getKey());
        }
    }
}
