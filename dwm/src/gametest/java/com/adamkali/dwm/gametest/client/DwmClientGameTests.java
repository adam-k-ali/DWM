package com.adamkali.dwm.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The single registered client GameTest entrypoint. Fabric cannot filter entrypoints, so this runs the subset
 * chosen by system properties (set from Gradle via {@code -Pgametest.tests} / {@code -Pgametest.categories}):
 * <ul>
 *   <li>{@code dwm.gametest.tests}: comma list of test names (class name minus {@code ClientGameTest}); wins over categories.</li>
 *   <li>{@code dwm.gametest.categories}: comma list of {@link GameTestCategory} ids, or {@code all} (default).</li>
 * </ul>
 * New tests must be added to {@link #entries()} and annotated with {@link ClientGameTestCategory}.
 */
public class DwmClientGameTests implements FabricClientGameTest {
    static final String PROP_TESTS = "dwm.gametest.tests";
    static final String PROP_CATEGORIES = "dwm.gametest.categories";
    private static final String SUFFIX = "ClientGameTest";

    record Entry(String name, Class<? extends FabricClientGameTest> type, Supplier<? extends FabricClientGameTest> factory) {
        Set<GameTestCategory> categories() {
            ClientGameTestCategory annotation = type.getAnnotation(ClientGameTestCategory.class);
            if (annotation == null) {
                throw new IllegalStateException(type.getName() + " is missing @ClientGameTestCategory");
            }
            Set<GameTestCategory> set = EnumSet.noneOf(GameTestCategory.class);
            set.addAll(List.of(annotation.value()));
            return set;
        }
    }

    private static <T extends FabricClientGameTest> Entry entry(Class<T> type, Supplier<T> factory) {
        String simple = type.getSimpleName();
        return new Entry(simple.endsWith(SUFFIX) ? simple.substring(0, simple.length() - SUFFIX.length()) : simple, type, factory);
    }

    /** Run order. ThalSettlement must stay after the portal tests (an earlier slot made the SOTO composite wait time out). */
    static List<Entry> entries() {
        return List.of(
                entry(TardisExteriorClientGameTest.class, TardisExteriorClientGameTest::new),
                entry(TardisVariantsClientGameTest.class, TardisVariantsClientGameTest::new),
                entry(EntitiesClientGameTest.class, EntitiesClientGameTest::new),
                entry(FieldGuideClientGameTest.class, FieldGuideClientGameTest::new),
                entry(SonicHudClientGameTest.class, SonicHudClientGameTest::new),
                entry(DalekOutpostClientGameTest.class, DalekOutpostClientGameTest::new),
                entry(DalekTrenchClientGameTest.class, DalekTrenchClientGameTest::new),
                entry(TardisBotiPortalClientGameTest.class, TardisBotiPortalClientGameTest::new),
                entry(TardisSotoPortalClientGameTest.class, TardisSotoPortalClientGameTest::new),
                entry(TardisSotoRelocateClientGameTest.class, TardisSotoRelocateClientGameTest::new),
                entry(TardisPortalParallaxClientGameTest.class, TardisPortalParallaxClientGameTest::new),
                entry(TardisPortalPerfClientGameTest.class, TardisPortalPerfClientGameTest::new),
                entry(ThalSettlementClientGameTest.class, ThalSettlementClientGameTest::new),
                entry(LaserGunClientGameTest.class, LaserGunClientGameTest::new),
                entry(SkaroFloraClientGameTest.class, SkaroFloraClientGameTest::new),
                entry(TardisGlobeClientGameTest.class, TardisGlobeClientGameTest::new),
                entry(TardisMonitorClientGameTest.class, TardisMonitorClientGameTest::new),
                entry(ConsoleAndSuitClientGameTest.class, ConsoleAndSuitClientGameTest::new));
    }

    /** Pure selection logic. Blank/{@code null} values mean "not set". Throws {@link IllegalArgumentException} on unknown names. */
    static List<Entry> select(List<Entry> all, String tests, String categories) {
        if (tests != null && !tests.isBlank()) {
            List<Entry> picked = new ArrayList<>();
            for (String raw : tests.split(",")) {
                String wanted = raw.trim();
                if (wanted.isEmpty()) {
                    continue;
                }
                String bare = wanted.toLowerCase(Locale.ROOT).endsWith(SUFFIX.toLowerCase(Locale.ROOT))
                        ? wanted.substring(0, wanted.length() - SUFFIX.length()) : wanted;
                Entry match = all.stream().filter(e -> e.name().equalsIgnoreCase(bare)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown client GameTest '" + wanted
                                + "'. Valid tests: " + all.stream().map(Entry::name).toList()));
                if (!picked.contains(match)) {
                    picked.add(match);
                }
            }
            // Keep the canonical run order regardless of how the list was typed.
            return all.stream().filter(picked::contains).toList();
        }
        if (categories == null || categories.isBlank() || categories.trim().equalsIgnoreCase("all")) {
            return all;
        }
        Set<GameTestCategory> wanted = EnumSet.noneOf(GameTestCategory.class);
        for (String raw : categories.split(",")) {
            String id = raw.trim();
            if (id.isEmpty()) {
                continue;
            }
            wanted.add(GameTestCategory.byId(id).orElseThrow(() -> new IllegalArgumentException(
                    "Unknown client GameTest category '" + id + "'. Valid categories: "
                            + java.util.Arrays.stream(GameTestCategory.values()).map(GameTestCategory::id).toList() + " or 'all'")));
        }
        return all.stream().filter(e -> e.categories().stream().anyMatch(wanted::contains)).toList();
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        List<Entry> all = entries();
        all.forEach(Entry::categories); // fail fast if a test is uncategorised
        List<Entry> selected = select(all, System.getProperty(PROP_TESTS), System.getProperty(PROP_CATEGORIES));
        System.out.println("[dwm-gametest] running " + selected.size() + "/" + all.size() + " client GameTests: "
                + selected.stream().map(Entry::name).toList());
        if (selected.isEmpty()) {
            throw new IllegalStateException("No client GameTests matched the selection");
        }
        for (Entry entry : selected) {
            System.out.println("[dwm-gametest] >>> " + entry.name());
            entry.factory().get().runTest(context);
        }
    }
}
