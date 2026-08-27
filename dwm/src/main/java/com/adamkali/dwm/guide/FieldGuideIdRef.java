package com.adamkali.dwm.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * JSON wrapper {@code {"id": "dwm:..."}} used by book, chapter, and page files.
 */
public record FieldGuideIdRef(Identifier id) {
    public static final Codec<FieldGuideIdRef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(FieldGuideIdRef::id)
    ).apply(instance, FieldGuideIdRef::new));
}
