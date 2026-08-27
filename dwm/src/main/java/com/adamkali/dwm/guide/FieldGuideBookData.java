package com.adamkali.dwm.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Datapack book entry under {@code data/<ns>/guide/book/}.
 */
public record FieldGuideBookData(FieldGuideIdRef guide, List<FieldGuideIdRef> chapters) {
    public static final Codec<FieldGuideBookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FieldGuideIdRef.CODEC.fieldOf("guide").forGetter(FieldGuideBookData::guide),
            FieldGuideIdRef.CODEC.listOf().fieldOf("chapters").forGetter(FieldGuideBookData::chapters)
    ).apply(instance, FieldGuideBookData::new));

    public FieldGuideBookData {
        chapters = List.copyOf(chapters);
    }
}
