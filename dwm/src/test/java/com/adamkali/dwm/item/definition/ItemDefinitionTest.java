package com.adamkali.dwm.item.definition;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.item.DWMArmorMaterials;
import com.adamkali.dwm.item.DWMItems;
import com.adamkali.dwm.item.DWMToolMaterials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemDefinitionTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
        ItemDefinitions.initialize();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dwm", path);
    }

    private static Map<Identifier, JsonObject> raw(String path, String json) {
        Map<Identifier, JsonObject> map = new LinkedHashMap<>();
        map.put(id(path), JsonParser.parseString(json).getAsJsonObject());
        return map;
    }

    @Test
    void productionDefinitionsRoundTripAndAreRegistered() {
        assertTrue(ItemDefinitions.all().size() >= 30);
        ItemDefinitions.all().forEach((id, decoded) -> {
            var encoded = ItemDefinition.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
            assertEquals(decoded, ItemDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow(), id.toString());
            assertTrue(BuiltInRegistries.ITEM.containsKey(id), id.toString());
        });
    }

    @Test
    void toolStatsMatchShippedValues() {
        ItemProperties.Tool sword = ItemDefinitions.get(id("steel_sword")).orElseThrow().properties().tool().orElseThrow();
        assertSame(DWMToolMaterials.STEEL, sword.material());
        assertEquals(3.0F, sword.attackDamage());
        assertEquals(-2.4F, sword.attackSpeed());
        ItemProperties.Tool pickaxe = ItemDefinitions.get(id("azbantium_pickaxe")).orElseThrow().properties().tool().orElseThrow();
        assertSame(DWMToolMaterials.AZBANTIUM, pickaxe.material());
        assertEquals(1.0F, pickaxe.attackDamage());
        assertEquals(-2.8F, pickaxe.attackSpeed());
        ItemProperties.Tool hoe = ItemDefinitions.get(id("silver_dalekanium_hoe")).orElseThrow().properties().tool().orElseThrow();
        assertEquals(-3.0F, hoe.attackDamage());
        assertEquals(0.0F, hoe.attackSpeed());
    }

    @Test
    void typesInstantiateTheRightClasses() {
        assertTrue(DWMItems.STEEL_SHOVEL instanceof ShovelItem);
        assertTrue(DWMItems.STEEL_AXE instanceof AxeItem);
        assertTrue(DWMItems.STEEL_HOE instanceof HoeItem);
    }

    @Test
    void armourBindsMaterialAndSlot() {
        ItemProperties.Armor boots = ItemDefinitions.get(id("azbantium_boots")).orElseThrow().properties().armor().orElseThrow();
        assertSame(DWMArmorMaterials.AZBANTIUM, boots.material());
        assertEquals(ArmorType.BOOTS, boots.slot());
        assertSame(DWMArmorMaterials.EVA_SUIT, ItemDefinitions.get(id("eva_suit_leggings")).orElseThrow().properties().armor().orElseThrow().material());
    }

    @Test
    void invalidDefinitionsFailWithClearErrors() {
        assertThrows(IllegalStateException.class, () -> ItemDefinitions.parse(raw("a", "{\"item\":\"dwm:a\",\"type\":\"dwm:sword\"}")));
        assertThrows(IllegalStateException.class, () -> ItemDefinitions.parse(raw("a", "{\"item\":\"dwm:a\",\"type\":\"dwm:armor\"}")));
        assertThrows(IllegalStateException.class, () -> ItemDefinitions.parse(raw("a", "{\"item\":\"dwm:b\"}")));
        assertThrows(IllegalStateException.class, () -> ItemDefinitions.parse(raw("a",
                "{\"item\":\"dwm:a\",\"type\":\"dwm:sword\",\"properties\":{\"tool\":{\"material\":\"nope\",\"attack_damage\":1,\"attack_speed\":1}}}")));
    }

    @Test
    void applyingAToolTypeWithoutAToolFailsClearly() {
        for (ItemType type : new ItemType[]{ItemType.SWORD, ItemType.PICKAXE}) {
            assertThrows(java.util.NoSuchElementException.class,
                    () -> ItemProperties.EMPTY.apply(new net.minecraft.world.item.Item.Properties(), type), type.toString());
        }
        assertThrows(java.util.NoSuchElementException.class,
                () -> ItemProperties.EMPTY.apply(new net.minecraft.world.item.Item.Properties(), ItemType.ARMOR));
    }
}
