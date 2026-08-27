package com.adamkali.dwm.guide;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the Field Guide catalog from datapack book/chapter/page registries.
 */
public final class FieldGuideCatalog {
    private static final Logger LOGGER = LogUtils.getLogger();

    private FieldGuideCatalog() {
    }

    public static List<FieldGuideChapter> chapters(RegistryAccess access) {
        return resolve(
                FieldGuideRegistries.FIELD_GUIDE_ID,
                registryMap(access, FieldGuideRegistries.BOOK),
                registryMap(access, FieldGuideRegistries.CHAPTER),
                registryMap(access, FieldGuideRegistries.PAGE)
        );
    }

    public static List<FieldGuidePage> allPages(RegistryAccess access) {
        return allPages(chapters(access));
    }

    public static List<FieldGuidePage> allPages(List<FieldGuideChapter> chapters) {
        return chapters.stream().flatMap(chapter -> chapter.pages().stream()).toList();
    }

    public static FieldGuideChapter chapterForPage(List<FieldGuideChapter> chapters, FieldGuidePage page) {
        for (FieldGuideChapter chapter : chapters) {
            if (chapter.pages().contains(page)) {
                return chapter;
            }
        }
        throw new IllegalArgumentException("Unknown page: " + page.id());
    }

    public static FieldGuideChapter chapterForPage(RegistryAccess access, FieldGuidePage page) {
        return chapterForPage(chapters(access), page);
    }

    public static int pageIndexInChapter(FieldGuideChapter chapter, FieldGuidePage page) {
        return chapter.pages().indexOf(page);
    }

    /**
     * Assembles a catalog from in-memory datapack entries. Used by the live registries and unit tests.
     * Missing refs and id mismatches are skipped with an error log.
     */
    public static List<FieldGuideChapter> resolve(
            Identifier bookId,
            Map<Identifier, FieldGuideBookData> books,
            Map<Identifier, FieldGuideChapterData> chapters,
            Map<Identifier, FieldGuidePageData> pages
    ) {
        FieldGuideBookData book = books.get(bookId);
        if (book == null) {
            LOGGER.error("Missing field guide book {}", bookId);
            return List.of();
        }
        if (!bookId.equals(book.guide().id())) {
            LOGGER.error("Field guide book id mismatch: file {} vs json {}", bookId, book.guide().id());
            return List.of();
        }

        List<FieldGuideChapter> resolved = new ArrayList<>();
        for (FieldGuideIdRef chapterRef : book.chapters()) {
            Identifier chapterId = chapterRef.id();
            FieldGuideChapterData chapterData = chapters.get(chapterId);
            if (chapterData == null) {
                LOGGER.error("Missing field guide chapter {}", chapterId);
                continue;
            }
            if (!chapterId.equals(chapterData.chapter().id())) {
                LOGGER.error("Field guide chapter id mismatch: file {} vs json {}", chapterId, chapterData.chapter().id());
                continue;
            }

            List<FieldGuidePage> chapterPages = new ArrayList<>();
            for (FieldGuideIdRef pageRef : chapterData.pages()) {
                Identifier pageId = pageRef.id();
                FieldGuidePageData pageData = pages.get(pageId);
                if (pageData == null) {
                    LOGGER.error("Missing field guide page {}", pageId);
                    continue;
                }
                if (!pageId.equals(pageData.page().id())) {
                    LOGGER.error("Field guide page id mismatch: file {} vs json {}", pageId, pageData.page().id());
                    continue;
                }
                if (pageData.textBlockCount() != 1) {
                    LOGGER.error("Field guide page {} must have exactly one text content block", pageId);
                    continue;
                }
                chapterPages.add(new FieldGuidePage(pageId, pageData.content()));
            }
            if (chapterPages.isEmpty()) {
                LOGGER.error("Field guide chapter {} has no valid pages", chapterId);
                continue;
            }
            resolved.add(new FieldGuideChapter(chapterId, chapterData.titleKey(), chapterPages));
        }
        return List.copyOf(resolved);
    }

    private static <T> Map<Identifier, T> registryMap(
            RegistryAccess access,
            ResourceKey<Registry<T>> key
    ) {
        return access.lookup(key).map(registry -> {
            Map<Identifier, T> map = new HashMap<>();
            for (var entry : registry.entrySet()) {
                map.put(entry.getKey().identifier(), entry.getValue());
            }
            return map;
        }).orElseGet(Map::of);
    }
}
