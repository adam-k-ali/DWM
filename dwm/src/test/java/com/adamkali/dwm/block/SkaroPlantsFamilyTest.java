package com.adamkali.dwm.block;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.plant.AshwheatBlock;
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
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.GRAVELIGHT_FUNGUS));
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.SCORCHBRIAR));
        assertTrue(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.WRAITHREED));
        assertFalse(DWMBlocks.SKARO_PLANTS.contains(DWMBlocks.ASHWHEAT), "crop is planted via seeds");
    }

    @Test
    void skaroPlantsDoNotOverlapGallifreyPlants() {
        for (var plant : DWMBlocks.SKARO_PLANTS) {
            assertFalse(DWMBlocks.GALLIFREY_PLANTS.contains(plant));
        }
        assertFalse(DWMBlocks.GALLIFREY_PLANTS.contains(DWMBlocks.ASHWHEAT));
    }

    @Test
    void idsAreRegistered() {
        for (String id : List.of("varga_plant", "gravelight_fungus", "scorchbriar", "wraithreed", "ashwheat")) {
            assertTrue(BuiltInRegistries.BLOCK.containsKey(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("dwm", id)), id);
        }
        assertTrue(BuiltInRegistries.ITEM.containsKey(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("dwm", "ashwheat_seeds")));
    }

    @Test
    void fungusGlowsDimlyAndOthersDoNot() {
        assertEquals(3, DWMBlocks.GRAVELIGHT_FUNGUS.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.VARGA_PLANT.defaultBlockState().getLightEmission());
        assertEquals(0, DWMBlocks.SCORCHBRIAR.defaultBlockState().getLightEmission());
    }

    @Test
    void ashwheatIsACropWithSeeds() {
        assertTrue(DWMBlocks.ASHWHEAT instanceof AshwheatBlock);
        assertEquals(7, ((AshwheatBlock) DWMBlocks.ASHWHEAT).getMaxAge());
        assertEquals(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("dwm", "ashwheat_seeds"),
                BuiltInRegistries.ITEM.getKey(DWMItems.ASHWHEAT_SEEDS));
    }

    @Test
    void biomesGetOnlyTheirDocumentedPlants() throws Exception {
        Map<String, List<String>> expected = Map.of(
                "skaro_irradiated_wastes.json", List.of("scorchbriar_wastes", "varga_wastes"),
                "skaro_petrified_jungle.json", List.of("gravelight_fungus_jungle", "varga_jungle"),
                "skaro_drammankin_mire.json", List.of("wraithreed_mire", "gravelight_fungus_mire", "varga_mire"),
                "skaro_drammankin_mountains.json", List.of("scorchbriar_mountains"),
                "skaro_thal_plateau.json", List.of()
        );
        List<String> all = List.of(
                "scorchbriar_wastes", "scorchbriar_mountains", "varga_wastes", "varga_jungle", "varga_mire",
                "gravelight_fungus_jungle", "gravelight_fungus_mire", "wraithreed_mire");
        for (var entry : expected.entrySet()) {
            String blob = Files.readString(BIOME_DIR.resolve(entry.getKey()));
            for (String feature : all) {
                assertEquals(
                        entry.getValue().contains(feature),
                        blob.contains("\"dwm:" + feature + "\""),
                        entry.getKey() + " / " + feature
                );
            }
            assertFalse(blob.contains("ashwheat"), entry.getKey() + " must not decorate with the Ashwheat");
            assertFalse(blob.contains("flower_of_remembrance") || blob.contains("saccharine"), entry.getKey());
        }
    }
}
