package com.adamkali.dwm.guide;

import com.adamkali.dwm.DWMReference;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Curated Field Guide chapters and pages. Recipe ids reference merged data/recipe resources.
 */
public final class FieldGuideCatalog {
    private static final Identifier SONIC_THIRD_DOCTOR =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_third_doctor");
    private static final Identifier SONIC_SECOND_DOCTOR =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "sonic_second_doctor");
    private static final Identifier TARDIS_KEY =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_key");
    private static final Identifier WHITE_CHRONOPLASM =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "white_chronoplasm_powder");
    private static final Identifier WHITE_TARDIS_WALL =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "white_tardis_wall");
    private static final Identifier WHITE_ROUNDEL_A =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "white_roundel_a");
    private static final Identifier WHITE_ROUNDEL_B =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "white_roundel_b");
    private static final Identifier WHITE_BIG_ROUNDEL_A =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "white_big_roundel_a");
    private static final Identifier TARDIS_CHAIR_SMALL =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "tardis_chair_small");

    private static final List<FieldGuideChapter> CHAPTERS = List.of(
            new FieldGuideChapter("quick_start", "dwm.guide.chapter.quick_start", List.of(
                    FieldGuidePage.text("find_tardis", "dwm.guide.page.find_tardis.title", "dwm.guide.page.find_tardis.body"),
                    FieldGuidePage.text("claim_tardis", "dwm.guide.page.claim_tardis.title", "dwm.guide.page.claim_tardis.body"),
                    FieldGuidePage.text("first_hop", "dwm.guide.page.first_hop.title", "dwm.guide.page.first_hop.body"),
                    FieldGuidePage.crafting("bind_key", "dwm.guide.page.bind_key.title", "dwm.guide.page.bind_key.body", TARDIS_KEY, false)
            )),
            new FieldGuideChapter("sonic", "dwm.guide.chapter.sonic", List.of(
                    FieldGuidePage.crafting("craft_sonic", "dwm.guide.page.craft_sonic.title", "dwm.guide.page.craft_sonic.body", SONIC_THIRD_DOCTOR, false),
                    FieldGuidePage.crafting("doctor_variants", "dwm.guide.page.doctor_variants.title", "dwm.guide.page.doctor_variants.body", SONIC_SECOND_DOCTOR, false),
                    FieldGuidePage.text("use_sonic", "dwm.guide.page.use_sonic.title", "dwm.guide.page.use_sonic.body")
            )),
            new FieldGuideChapter("console_room", "dwm.guide.chapter.console_room", List.of(
                    FieldGuidePage.crafting("chronoplasm", "dwm.guide.page.chronoplasm.title", "dwm.guide.page.chronoplasm.body", WHITE_CHRONOPLASM, true),
                    FieldGuidePage.smelting("tardis_wall", "dwm.guide.page.tardis_wall.title", "dwm.guide.page.tardis_wall.body", WHITE_TARDIS_WALL, true),
                    FieldGuidePage.crafting("roundel_a", "dwm.guide.page.roundel_a.title", "dwm.guide.page.roundel_a.body", WHITE_ROUNDEL_A, true),
                    FieldGuidePage.crafting("roundel_b", "dwm.guide.page.roundel_b.title", "dwm.guide.page.roundel_b.body", WHITE_ROUNDEL_B, true),
                    FieldGuidePage.crafting("big_roundel", "dwm.guide.page.big_roundel.title", "dwm.guide.page.big_roundel.body", WHITE_BIG_ROUNDEL_A, true),
                    FieldGuidePage.crafting("interior_props", "dwm.guide.page.interior_props.title", "dwm.guide.page.interior_props.body", TARDIS_CHAIR_SMALL, false)
            ))
    );

    private FieldGuideCatalog() {
    }

    public static List<FieldGuideChapter> chapters() {
        return CHAPTERS;
    }

    public static List<FieldGuidePage> allPages() {
        return CHAPTERS.stream().flatMap(chapter -> chapter.pages().stream()).toList();
    }

    public static FieldGuideChapter chapterForPage(FieldGuidePage page) {
        for (FieldGuideChapter chapter : CHAPTERS) {
            if (chapter.pages().contains(page)) {
                return chapter;
            }
        }
        throw new IllegalArgumentException("Unknown page: " + page.id());
    }

    public static int pageIndexInChapter(FieldGuideChapter chapter, FieldGuidePage page) {
        return chapter.pages().indexOf(page);
    }
}
