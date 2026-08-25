package com.adamkali.dwm.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DimensionNamesTest {
    @Test
    void of_knownIds_useDimensionTranslationKeyWithHumanizedFallback() {
        assertTranslatable(DimensionNames.of("dwm:gallifrey"), "dimension.dwm.gallifrey", "Gallifrey");
        assertTranslatable(DimensionNames.of("minecraft:overworld"), "dimension.minecraft.overworld", "Overworld");
        assertTranslatable(
                DimensionNames.of(Identifier.fromNamespaceAndPath("minecraft", "the_nether")),
                "dimension.minecraft.the_nether",
                "The Nether"
        );
        assertTranslatable(
                DimensionNames.of("minecraft:the_end"),
                "dimension.minecraft.the_end",
                "The End"
        );
        assertTranslatable(
                DimensionNames.of("ad_astra:glacio"),
                "dimension.ad_astra.glacio",
                "Glacio"
        );
        assertTranslatable(
                DimensionNames.of("some_mod:my_custom_dim"),
                "dimension.some_mod.my_custom_dim",
                "My Custom Dim"
        );
        assertTranslatable(
                DimensionNames.of(Identifier.fromNamespaceAndPath("mod", "foo/bar_baz")),
                "dimension.mod.foo/bar_baz",
                "Foo Bar Baz"
        );
    }

    @Test
    void fallbackName_titleCasesPathSegments() {
        assertEquals("Overworld", DimensionNames.fallbackName(Identifier.parse("minecraft:overworld")));
        assertEquals("The Nether", DimensionNames.fallbackName(Identifier.parse("minecraft:the_nether")));
        assertEquals("Foo Bar Baz", DimensionNames.fallbackName(Identifier.fromNamespaceAndPath("mod", "foo/bar_baz")));
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
