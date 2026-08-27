package com.adamkali.dwm.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

import java.util.List;

/**
 * Tagged page content block. Dispatch key is {@code type}.
 */
public sealed interface FieldGuideContent {
    Kind kind();

    Codec<FieldGuideContent> CODEC = Kind.CODEC.dispatch("type", FieldGuideContent::kind, Kind::mapCodec);

    enum Kind implements StringRepresentable {
        TEXT("text"),
        CRAFTING("crafting"),
        SMELTING("smelting"),
        STONECUTTING("stonecutting");

        public static final StringRepresentable.EnumCodec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        MapCodec<? extends FieldGuideContent> mapCodec() {
            return switch (this) {
                case TEXT -> Text.MAP_CODEC;
                case CRAFTING -> Crafting.MAP_CODEC;
                case SMELTING -> Smelting.MAP_CODEC;
                case STONECUTTING -> Stonecutting.MAP_CODEC;
            };
        }
    }

    record Text(String titleKey, String bodyKey) implements FieldGuideContent {
        public static final MapCodec<Text> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("titleKey").forGetter(Text::titleKey),
                Codec.STRING.fieldOf("bodyKey").forGetter(Text::bodyKey)
        ).apply(instance, Text::new));

        @Override
        public Kind kind() {
            return Kind.TEXT;
        }
    }

    record Crafting(List<Identifier> recipes, boolean pattern) implements FieldGuideContent {
        public static final MapCodec<Crafting> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.listOf().fieldOf("recipes").forGetter(Crafting::recipes),
                Codec.BOOL.optionalFieldOf("pattern", false).forGetter(Crafting::pattern)
        ).apply(instance, Crafting::new));

        public Crafting {
            recipes = List.copyOf(recipes);
            if (recipes.isEmpty()) {
                throw new IllegalArgumentException("crafting content requires at least one recipe");
            }
        }

        @Override
        public Kind kind() {
            return Kind.CRAFTING;
        }
    }

    record Smelting(Identifier recipe, boolean pattern) implements FieldGuideContent {
        public static final MapCodec<Smelting> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("recipe").forGetter(Smelting::recipe),
                Codec.BOOL.optionalFieldOf("pattern", false).forGetter(Smelting::pattern)
        ).apply(instance, Smelting::new));

        @Override
        public Kind kind() {
            return Kind.SMELTING;
        }
    }

    record Stonecutting(Identifier recipe, boolean pattern) implements FieldGuideContent {
        public static final MapCodec<Stonecutting> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("recipe").forGetter(Stonecutting::recipe),
                Codec.BOOL.optionalFieldOf("pattern", false).forGetter(Stonecutting::pattern)
        ).apply(instance, Stonecutting::new));

        @Override
        public Kind kind() {
            return Kind.STONECUTTING;
        }
    }
}
