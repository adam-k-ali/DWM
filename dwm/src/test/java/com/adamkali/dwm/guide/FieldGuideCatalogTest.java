package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldGuideCatalogTest {
    private static final Path PROJECT_ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path RECIPE_ROOT = PROJECT_ROOT.resolve("src/main");
    private static final List<Path> RECIPE_DIRS = List.of(
            RECIPE_ROOT.resolve("resources/data/" + DWMReference.MOD_ID + "/recipe"),
            RECIPE_ROOT.resolve("generated/data/" + DWMReference.MOD_ID + "/recipe")
    );

    @Test
    void pageIdsAreUniqueWithinCatalog() {
        Set<String> seen = new HashSet<>();
        for (FieldGuidePage page : FieldGuideCatalog.allPages()) {
            assertTrue(seen.add(page.id()), "Duplicate page id: " + page.id());
        }
        assertFalse(seen.isEmpty());
    }

    @Test
    void chapterIdsAreUniqueWithinCatalog() {
        Set<String> seen = new HashSet<>();
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
            assertTrue(seen.add(chapter.id()), "Duplicate chapter id: " + chapter.id());
        }
    }

    @Test
    void referencedRecipeFilesExist() throws IOException {
        for (FieldGuidePage page : FieldGuideCatalog.allPages()) {
            assertRecipeExists(page.craftingRecipe());
            assertRecipeExists(page.smeltingRecipe());
            assertRecipeExists(page.stonecuttingRecipe());
        }
    }

    private static void assertRecipeExists(net.minecraft.resources.Identifier recipeId) throws IOException {
        if (recipeId == null) {
            return;
        }
        assertTrue(DWMReference.MOD_ID.equals(recipeId.getNamespace()), "Recipe must be dwm namespace: " + recipeId);
        String fileName = recipeId.getPath() + ".json";
        boolean found = false;
        for (Path dir : RECIPE_DIRS) {
            if (Files.isDirectory(dir) && Files.exists(dir.resolve(fileName))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Missing recipe JSON for " + recipeId + " (expected " + fileName + " under recipe dirs)");
    }

    @Test
    void everyChapterHasAtLeastOnePage() {
        for (FieldGuideChapter chapter : FieldGuideCatalog.chapters()) {
            assertFalse(chapter.pages().isEmpty(), "Chapter has no pages: " + chapter.id());
        }
    }

    @Test
    void allPagesBelongToKnownChapters() {
        long pageCount = FieldGuideCatalog.chapters().stream()
                .mapToLong(chapter -> chapter.pages().size())
                .sum();
        assertTrue(pageCount >= 10);
        assertTrue(FieldGuideCatalog.allPages().size() == pageCount);
    }
}
