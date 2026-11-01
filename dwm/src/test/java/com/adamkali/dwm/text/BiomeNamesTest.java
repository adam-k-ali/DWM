package com.adamkali.dwm.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiomeNamesTest {
    @Test
    void of_biomeIds_useBiomeTranslationKeyWithHumanizedFallback() {
        assertTranslatable(BiomeNames.of("minecraft:plains"), "biome.minecraft.plains", "Plains");
        assertTranslatable(BiomeNames.of("dwm:red_desert"), "biome.dwm.red_desert", "Red Desert");
    }

    @Test
    void of_blankOrInvalid_isEmptyOrLiteral() {
        assertEquals(Component.empty(), BiomeNames.of(null));
        assertEquals(Component.empty(), BiomeNames.of(" "));
        assertEquals("Not An Id!", BiomeNames.of("Not An Id!").getString());
    }

    private static void assertTranslatable(Component component, String key, String fallback) {
        assertInstanceOf(TranslatableContents.class, component.getContents());
        TranslatableContents contents = (TranslatableContents) component.getContents();
        assertEquals(key, contents.getKey());
        assertEquals(fallback, contents.getFallback());
    }
}
