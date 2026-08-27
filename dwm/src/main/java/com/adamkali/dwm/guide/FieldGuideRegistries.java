package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Synced datapack registries for Field Guide books, chapters, and pages.
 */
public final class FieldGuideRegistries {
    public static final ResourceKey<Registry<FieldGuideBookData>> BOOK = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "guide/book")
    );
    public static final ResourceKey<Registry<FieldGuideChapterData>> CHAPTER = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "guide/chapter")
    );
    public static final ResourceKey<Registry<FieldGuidePageData>> PAGE = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "guide/page")
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
