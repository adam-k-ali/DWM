package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Synced datapack registries for Field Guide books, chapters, and pages.
 *
 * <p>Keys use the vanilla {@code minecraft} namespace so Fabric does not prepend a registry
 * namespace folder. Entries therefore load from {@code data/<entry ns>/guide/...} — for example
 * {@code data/dwm/guide/book/field_guide.json}. A {@code dwm:} registry id would load from
 * {@code data/<entry ns>/dwm/guide/...} instead.
 */
public final class FieldGuideRegistries {
    public static final ResourceKey<Registry<FieldGuideBookData>> BOOK = ResourceKey.createRegistryKey(
            Identifier.withDefaultNamespace("guide/book")
    );
    public static final ResourceKey<Registry<FieldGuideChapterData>> CHAPTER = ResourceKey.createRegistryKey(
            Identifier.withDefaultNamespace("guide/chapter")
    );
    public static final ResourceKey<Registry<FieldGuidePageData>> PAGE = ResourceKey.createRegistryKey(
            Identifier.withDefaultNamespace("guide/page")
    );

    public static final Identifier FIELD_GUIDE_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "field_guide");

    private FieldGuideRegistries() {
    }

    public static void initialize() {
        DynamicRegistries.registerSynced(BOOK, FieldGuideBookData.CODEC);
        DynamicRegistries.registerSynced(CHAPTER, FieldGuideChapterData.CODEC);
        DynamicRegistries.registerSynced(PAGE, FieldGuidePageData.CODEC);
    }
}
