package com.adamkali.dwm.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Datapack chapter entry under {@code data/<ns>/guide/chapter/}.
 */
public record FieldGuideChapterData(FieldGuideIdRef chapter, String titleKey, List<FieldGuideIdRef> pages) {
    public static final Codec<FieldGuideChapterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FieldGuideIdRef.CODEC.fieldOf("chapter").forGetter(FieldGuideChapterData::chapter),
            Codec.STRING.fieldOf("titleKey").forGetter(FieldGuideChapterData::titleKey),
            FieldGuideIdRef.CODEC.listOf().fieldOf("pages").forGetter(FieldGuideChapterData::pages)
    ).apply(instance, FieldGuideChapterData::new));

    public FieldGuideChapterData {
        pages = List.copyOf(pages);
    }
}
