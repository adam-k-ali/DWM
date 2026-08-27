package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.MinecraftTestBootstrap;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldGuideCatalogTest {
    private static final Path PROJECT_ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path GUIDE_ROOT = PROJECT_ROOT.resolve(
            "src/main/resources/data/" + DWMReference.MOD_ID + "/guide"
    );
    private static final Path RECIPE_ROOT = PROJECT_ROOT.resolve("src/main");
    private static final List<Path> RECIPE_DIRS = List.of(
            RECIPE_ROOT.resolve("resources/data/" + DWMReference.MOD_ID + "/recipe"),
            RECIPE_ROOT.resolve("generated/data/" + DWMReference.MOD_ID + "/recipe")
    );

    private static List<FieldGuideChapter> catalog;

    @BeforeAll
    static void loadProductionCatalog() throws IOException {
        MinecraftTestBootstrap.ensure();
        catalog = FieldGuideCatalog.resolve(
                FieldGuideRegistries.FIELD_GUIDE_ID,
                loadAll(GUIDE_ROOT.resolve("book"), FieldGuideBookData.CODEC),
                loadAll(GUIDE_ROOT.resolve("chapter"), FieldGuideChapterData.CODEC),
                loadAll(GUIDE_ROOT.resolve("page"), FieldGuidePageData.CODEC)
        );
    }

    @Test
    void pageIdsAreUniqueWithinCatalog() {
        Set<Identifier> seen = new HashSet<>();
        for (FieldGuidePage page : FieldGuideCatalog.allPages(catalog)) {
            assertTrue(seen.add(page.id()), "Duplicate page id: " + page.id());
        }
        assertFalse(seen.isEmpty());
    }

    @Test
    void chapterIdsAreUniqueWithinCatalog() {
        Set<Identifier> seen = new HashSet<>();
        for (FieldGuideChapter chapter : catalog) {
            assertTrue(seen.add(chapter.id()), "Duplicate chapter id: " + chapter.id());
        }
    }

    @Test
    void referencedRecipeFilesExist() throws IOException {
        for (FieldGuidePage page : FieldGuideCatalog.allPages(catalog)) {
            for (Identifier recipeId : page.craftingRecipes()) {
                assertRecipeExists(recipeId);
            }
            assertRecipeExists(page.smeltingRecipe());
            assertRecipeExists(page.stonecuttingRecipe());
        }
    }

    @Test
    void everyChapterHasAtLeastOnePage() {
        for (FieldGuideChapter chapter : catalog) {
            assertFalse(chapter.pages().isEmpty(), "Chapter has no pages: " + chapter.id());
        }
    }

    @Test
    void allPagesBelongToKnownChapters() {
        long pageCount = catalog.stream().mapToLong(chapter -> chapter.pages().size()).sum();
        assertTrue(pageCount >= 10);
        assertEquals(pageCount, FieldGuideCatalog.allPages(catalog).size());
    }

    @Test
    void roundelPageListsAllShapeVariants() {
        FieldGuidePage roundel = FieldGuideCatalog.allPages(catalog).stream()
                .filter(page -> page.id().equals(id("roundel")))
                .findFirst()
                .orElseThrow();
        assertEquals(
                List.of(
                        id("white_roundel_a"),
                        id("white_roundel_b"),
                        id("white_big_roundel_a")
                ),
                roundel.craftingRecipes()
        );
        assertTrue(roundel.patternPage());
    }

    @Test
    void consoleRoomHasSingleRoundelPage() {
        Set<Identifier> ids = catalog.stream()
                .filter(chapter -> chapter.id().equals(id("console_room")))
                .findFirst()
                .orElseThrow()
                .pages()
                .stream()
                .map(FieldGuidePage::id)
                .collect(Collectors.toSet());
        assertTrue(ids.contains(id("roundel")));
        assertFalse(ids.contains(id("roundel_a")));
        assertFalse(ids.contains(id("roundel_b")));
        assertFalse(ids.contains(id("big_roundel")));
    }

    @Test
    void productionJsonRoundTrips() throws IOException {
        roundTripDir(GUIDE_ROOT.resolve("book"), FieldGuideBookData.CODEC);
        roundTripDir(GUIDE_ROOT.resolve("chapter"), FieldGuideChapterData.CODEC);
        roundTripDir(GUIDE_ROOT.resolve("page"), FieldGuidePageData.CODEC);
    }

    @Test
    void unknownContentTypeFailsParse() {
        var json = JsonParser.parseString(
                "{\"type\":\"image\",\"titleKey\":\"x\",\"bodyKey\":\"y\"}"
        );
        assertTrue(FieldGuideContent.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }

    @Test
    void pageWithoutTextBlockIsSkipped() {
        Identifier bookId = id("field_guide");
        Identifier chapterId = id("quick_start");
        Identifier pageId = id("broken");
        FieldGuideBookData book = new FieldGuideBookData(
                new FieldGuideIdRef(bookId),
                List.of(new FieldGuideIdRef(chapterId))
        );
        FieldGuideChapterData chapter = new FieldGuideChapterData(
                new FieldGuideIdRef(chapterId),
                "dwm.guide.chapter.quick_start",
                List.of(new FieldGuideIdRef(pageId))
        );
        FieldGuidePageData page = new FieldGuidePageData(
                new FieldGuideIdRef(pageId),
                List.of(new FieldGuideContent.Crafting(List.of(id("tardis_key")), false))
        );

        List<FieldGuideChapter> resolved = FieldGuideCatalog.resolve(
                bookId,
                Map.of(bookId, book),
                Map.of(chapterId, chapter),
                Map.of(pageId, page)
        );
        assertTrue(resolved.isEmpty());
    }

    @Test
    void missingChapterRefIsSkipped() {
        Identifier bookId = id("field_guide");
        Identifier missingChapter = id("does_not_exist");
        FieldGuideBookData book = new FieldGuideBookData(
                new FieldGuideIdRef(bookId),
                List.of(new FieldGuideIdRef(missingChapter))
        );
        List<FieldGuideChapter> resolved = FieldGuideCatalog.resolve(
                bookId,
                Map.of(bookId, book),
                Map.of(),
                Map.of()
        );
        assertTrue(resolved.isEmpty());
    }

    @Test
    void nestedIdMismatchIsSkipped() {
        Identifier fileId = id("field_guide");
        Identifier jsonId = id("other_book");
        FieldGuideBookData book = new FieldGuideBookData(
                new FieldGuideIdRef(jsonId),
                List.of()
        );
        List<FieldGuideChapter> resolved = FieldGuideCatalog.resolve(
                fileId,
                Map.of(fileId, book),
                Map.of(),
                Map.of()
        );
        assertTrue(resolved.isEmpty());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, path);
    }

    private static <T> void roundTripDir(Path dir, Codec<T> codec) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                var json = JsonParser.parseString(Files.readString(path));
                T decoded = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
                var encoded = codec.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
                T roundTripped = codec.parse(JsonOps.INSTANCE, encoded).getOrThrow();
                assertEquals(decoded, roundTripped, path.getFileName().toString());
            }
        }
    }

    private static <T> Map<Identifier, T> loadAll(Path dir, Codec<T> codec) throws IOException {
        Map<Identifier, T> map = new HashMap<>();
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                String fileName = path.getFileName().toString();
                String stem = fileName.substring(0, fileName.length() - ".json".length());
                Identifier id = id(stem);
                var json = JsonParser.parseString(Files.readString(path));
                map.put(id, codec.parse(JsonOps.INSTANCE, json).getOrThrow());
            }
        }
        return map;
    }

    private static void assertRecipeExists(Identifier recipeId) throws IOException {
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
}
