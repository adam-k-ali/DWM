package com.adamkali.dwm.tardis.interior.ars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Datapack ARS piece entry under {@code data/<ns>/tardis/ars_piece/}.
 *
 * <pre>{@code
 * { "kind": "room", "structure": "dwm:tardis/ars/library", "cells": [2, 2],
 *   "connectors": [{ "cell": [0, 1], "side": "west" }], "artron_cost": 40, "map_color": "#B08D57" }
 * }</pre>
 *
 * <p>{@code cells} is the unrotated footprint {@code [width, depth]}, each {@code 1..}
 * {@value ArsGrid#MAX_PIECE_CELLS}. Connector cells are footprint-local and unrotated; each must lie
 * inside the footprint with a horizontal side on its outer boundary. {@code artron_cost} is
 * non-negative and {@code map_color} is {@code #RRGGBB}. {@link #CODEC} rejects anything else with a
 * {@link DataResult#error}; the constructor itself does not validate.
 */
public record ArsPieceDefinition(
        ArsPieceKind kind,
        Identifier structure,
        int width,
        int depth,
        List<ArsConnector> connectors,
        int artronCost,
        int mapColor
) {
    private static final Pattern COLOR_PATTERN = Pattern.compile("#[0-9A-Fa-f]{6}");

    static final Codec<Integer> MAP_COLOR_CODEC = Codec.STRING.comapFlatMap(
            ArsPieceDefinition::parseColor,
            rgb -> String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF)
    );

    private static final Codec<List<Integer>> CELLS_CODEC = Codec.INT.listOf().validate(list -> Util.fixedSize(list, 2));

    public static final Codec<ArsPieceDefinition> CODEC = RecordCodecBuilder.<ArsPieceDefinition>create(instance -> instance.group(
            ArsPieceKind.CODEC.fieldOf("kind").forGetter(ArsPieceDefinition::kind),
            Identifier.CODEC.fieldOf("structure").forGetter(ArsPieceDefinition::structure),
            CELLS_CODEC.fieldOf("cells").forGetter(def -> List.of(def.width(), def.depth())),
            ArsConnector.CODEC.listOf().fieldOf("connectors").forGetter(ArsPieceDefinition::connectors),
            Codec.INT.fieldOf("artron_cost").forGetter(ArsPieceDefinition::artronCost),
            MAP_COLOR_CODEC.fieldOf("map_color").forGetter(ArsPieceDefinition::mapColor)
    ).apply(instance, (kind, structure, cells, connectors, artronCost, mapColor) -> new ArsPieceDefinition(
            kind, structure, cells.get(0), cells.get(1), connectors, artronCost, mapColor
    ))).validate(ArsPieceDefinition::validate);

    public ArsPieceDefinition {
        connectors = List.copyOf(connectors);
    }

    /**
     * Checks the cross-field rules the codec enforces.
     */
    public static DataResult<ArsPieceDefinition> validate(ArsPieceDefinition def) {
        if (def.width < 1 || def.width > ArsGrid.MAX_PIECE_CELLS
                || def.depth < 1 || def.depth > ArsGrid.MAX_PIECE_CELLS) {
            return DataResult.error(() -> "ARS piece cells must each be 1.." + ArsGrid.MAX_PIECE_CELLS
                    + ", got [" + def.width + ", " + def.depth + "]");
        }
        if (def.artronCost < 0) {
            return DataResult.error(() -> "ARS piece artron_cost must be >= 0, got " + def.artronCost);
        }
        for (ArsConnector connector : def.connectors) {
            ArsCell cell = connector.cell();
            if (cell.x() < 0 || cell.x() >= def.width || cell.z() < 0 || cell.z() >= def.depth) {
                return DataResult.error(() -> "ARS connector cell " + cell + " is outside the "
                        + def.width + "x" + def.depth + " footprint");
            }
            boolean onBoundary = switch (connector.side()) {
                case NORTH -> cell.z() == 0;
                case SOUTH -> cell.z() == def.depth - 1;
                case WEST -> cell.x() == 0;
                case EAST -> cell.x() == def.width - 1;
                case UP, DOWN -> false;
            };
            if (!onBoundary) {
                return DataResult.error(() -> "ARS connector " + connector
                        + " must face a horizontal side on the footprint boundary");
            }
        }
        return DataResult.success(def);
    }

    private static DataResult<Integer> parseColor(String value) {
        if (!COLOR_PATTERN.matcher(value).matches()) {
            return DataResult.error(() -> "ARS map_color must be #RRGGBB, got '" + value + "'");
        }
        return DataResult.success(Integer.parseInt(value.substring(1), 16));
    }
}
