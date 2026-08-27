package com.adamkali.dwm.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Datapack page entry under {@code data/<ns>/guide/page/}.
 */
public record FieldGuidePageData(FieldGuideIdRef page, List<FieldGuideContent> content) {
    public static final Codec<FieldGuidePageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FieldGuideIdRef.CODEC.fieldOf("page").forGetter(FieldGuidePageData::page),
            FieldGuideContent.CODEC.listOf().fieldOf("content").forGetter(FieldGuidePageData::content)
    ).apply(instance, FieldGuidePageData::new));

    public FieldGuidePageData {
        content = List.copyOf(content);
    }

    public long textBlockCount() {
        return content.stream().filter(FieldGuideContent.Text.class::isInstance).count();
    }
}
