package com.adamkali.dwm.block.definition;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.block.DWMBlocks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.material.MapColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockDefinitionTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        BlockDefinitions.initialize();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }

    private static Map<Identifier, JsonObject> raw(String... idsAndJson) {
        Map<Identifier, JsonObject> map = new LinkedHashMap<>();
        for (int i = 0; i < idsAndJson.length; i += 2) {
            map.put(id(idsAndJson[i]), JsonParser.parseString(idsAndJson[i + 1]).getAsJsonObject());
        }
        return map;
    }

    @Test
    void productionDefinitionsRoundTripThroughCodec() {
        BlockDefinitions.all().forEach((id, decoded) -> {
            var encoded = BlockDefinition.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
            assertEquals(decoded, BlockDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow(), id.toString());
        });
    }

    @Test
    void everyProductionDefinitionIsRegistered() {
        BlockDefinitions.all().keySet().forEach(id -> assertTrue(BuiltInRegistries.BLOCK.containsKey(id), id.toString()));
        assertTrue(BlockDefinitions.all().size() > 100);
    }

    @Test
    void abstractTemplatesAreNeverRegistered() {
        assertTrue(BlockDefinitions.get(id("roundel_base")).isEmpty());
        assertFalse(BuiltInRegistries.BLOCK.containsKey(id("roundel_base")));
    }

    @Test
    void templatePropertiesReachTheRegisteredBlock() {
        Block roundelB = DWMBlocks.RED_ROUNDEL_B;
        assertEquals(10, roundelB.defaultBlockState().getLightEmission());
        assertFalse(roundelB.defaultBlockState().canOcclude());
        assertEquals(2.0F, roundelB.defaultDestroyTime());
        assertEquals(3.0F, roundelB.getExplosionResistance());
        assertEquals(SoundType.METAL, roundelB.defaultBlockState().getSoundType());

        Block stone = DWMBlocks.GALLIFREY_STONE;
        assertTrue(stone.defaultBlockState().requiresCorrectToolForDrops());
        assertEquals(1.5F, stone.defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO));
    }

    @Test
    void typesInstantiateTheRightClasses() {
        assertTrue(DWMBlocks.GALLIFREY_COBBLESTONE_STAIRS instanceof StairBlock);
        assertTrue(DWMBlocks.GALLIFREY_COBBLESTONE_SLAB instanceof SlabBlock);
        assertTrue(DWMBlocks.GALLIFREY_COBBLESTONE_WALL instanceof WallBlock);
        assertTrue(DWMBlocks.ORANGE_SAND instanceof ColoredFallingBlock);
    }

    @Test
    void childOverridesMergePropertiesPerKeyAndInheritNothingAbstract() {
        Map<Identifier, BlockDefinition> parsed = BlockDefinitions.parse(raw(
                "base", """
                        {"block":"dwm:base","abstract":true,"properties":{"sound":"metal","light_level":4}}""",
                "child", """
                        {"block":"dwm:child","parent":"dwm:base","properties":{"light_level":9}}"""));
        assertEquals(1, parsed.size());
        BlockProperties props = parsed.get(id("child")).properties();
        assertEquals(9, props.lightLevel().orElseThrow());
        assertEquals(SoundType.METAL, props.sound().orElseThrow());
    }

    @Test
    void explosionResistanceDefaultsToDestroyTime() {
        BlockDefinition definition = BlockDefinitions.parse(raw(
                "soft", """
                        {"block":"dwm:soft","properties":{"strength":{"destroy_time":0.5},"map_color":"terracotta_orange"}}""")).get(id("soft"));
        assertEquals(new BlockProperties.Strength(0.5F, 0.5F), definition.properties().strength().orElseThrow());
        assertEquals(MapColor.TERRACOTTA_ORANGE, definition.properties().mapColor().orElseThrow());
    }

    @Test
    void invalidDefinitionsFailWithClearErrors() {
        assertThrows(IllegalStateException.class, () -> BlockDefinitions.parse(raw("a", "{\"block\":\"dwm:a\",\"parent\":\"dwm:missing\"}")));
        assertThrows(IllegalStateException.class, () -> BlockDefinitions.parse(raw(
                "a", "{\"block\":\"dwm:a\",\"parent\":\"dwm:b\"}", "b", "{\"block\":\"dwm:b\",\"parent\":\"dwm:a\"}")));
        assertThrows(IllegalStateException.class, () -> BlockDefinitions.parse(raw("a", "{\"block\":\"dwm:other\"}")));
        assertThrows(IllegalStateException.class, () -> BlockDefinitions.parse(raw("a", "{\"block\":\"dwm:a\",\"type\":\"dwm:stairs\"}")));
        assertThrows(IllegalStateException.class, () -> BlockDefinitions.parse(raw("a", "{\"block\":\"dwm:a\",\"properties\":{\"sound\":\"nope\"}}")));
        assertThrows(IllegalStateException.class, () -> BlockDefinitions.parse(raw("a", "{\"block\":\"dwm:a\",\"properties\":{\"map_color\":\"nope\"}}")));
    }
}
