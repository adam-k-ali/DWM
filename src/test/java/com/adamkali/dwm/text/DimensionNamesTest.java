package com.adamkali.dwm.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DimensionNamesTest {
    @Test
    void of_knownIds_useDimensionTranslationKeyWithIdFallback() {
        assertTranslatable(DimensionNames.of("dwm:gallifrey"), "dimension.dwm.gallifrey", "dwm:gallifrey");
        assertTranslatable(DimensionNames.of("minecraft:overworld"), "dimension.minecraft.overworld", "minecraft:overworld");
        assertTranslatable(
                DimensionNames.of(Identifier.fromNamespaceAndPath("minecraft", "the_nether")),
                "dimension.minecraft.the_nether",
                "minecraft:the_nether"
        );
    }

    @Test
    void of_blankOrNull_returnsEmpty() {
        assertEquals(PlainTextContents.EMPTY, DimensionNames.of((String) null).getContents());
        assertEquals(PlainTextContents.EMPTY, DimensionNames.of("").getContents());
        assertEquals(PlainTextContents.EMPTY, DimensionNames.of("   ").getContents());
    }

    @Test
    void of_invalidId_returnsLiteral() {
        Component component = DimensionNames.of("not a valid id");
        assertInstanceOf(PlainTextContents.LiteralContents.class, component.getContents());
        assertEquals("not a valid id", ((PlainTextContents.LiteralContents) component.getContents()).text());
    }

    private static void assertTranslatable(Component component, String key, String fallback) {
        assertInstanceOf(TranslatableContents.class, component.getContents());
        TranslatableContents contents = (TranslatableContents) component.getContents();
        assertEquals(key, contents.getKey());
        assertEquals(fallback, contents.getFallback());
    }
}
