package com.adamkali.dwm.guide;

import net.minecraft.resources.Identifier;

import java.util.List;

public record FieldGuideChapter(Identifier id, String titleKey, List<FieldGuidePage> pages) {
    public FieldGuideChapter {
        pages = List.copyOf(pages);
    }
}
