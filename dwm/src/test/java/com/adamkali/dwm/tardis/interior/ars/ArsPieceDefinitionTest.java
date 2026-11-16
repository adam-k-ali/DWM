package com.adamkali.dwm.tardis.interior.ars;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArsPieceDefinitionTest {
    private static final String VALID = """
            { "kind": "room", "structure": "dwm:tardis/ars/library", "cells": [2, 2],
              "connectors": [{ "cell": [0, 1], "side": "west" }], "artron_cost": 40, "map_color": "#B08D57" }
            """;

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void parsesValidDefinition() {
        ArsPieceDefinition def = parse(VALID).getOrThrow();

        assertEquals(ArsPieceKind.ROOM, def.kind());
        assertEquals(Identifier.fromNamespaceAndPath("dwm", "tardis/ars/library"), def.structure());
        assertEquals(2, def.width());
        assertEquals(2, def.depth());
        assertEquals(List.of(new ArsConnector(new ArsCell(0, 1), Direction.WEST)), def.connectors());
        assertEquals(40, def.artronCost());
        assertEquals(0xB08D57, def.mapColor());
    }

    @Test
    void parsesCorridorKindAndLowercaseColour() {
        ArsPieceDefinition def = parse(VALID
                .replace("\"room\"", "\"corridor\"")
                .replace("#B08D57", "#b08d57")).getOrThrow();

        assertEquals(ArsPieceKind.CORRIDOR, def.kind());
        assertEquals(0xB08D57, def.mapColor());
    }

    @Test
    void encodeDecodeRoundTrips() {
        ArsPieceDefinition def = new ArsPieceDefinition(
                ArsPieceKind.CORRIDOR,
                Identifier.fromNamespaceAndPath("dwm", "tardis/ars/corridor"),
                3,
                1,
                List.of(
                        new ArsConnector(new ArsCell(0, 0), Direction.WEST),
                        new ArsConnector(new ArsCell(2, 0), Direction.EAST),
                        new ArsConnector(new ArsCell(1, 0), Direction.NORTH)
                ),
                0,
                0x0A0B0C
        );

        JsonElement json = ArsPieceDefinition.CODEC.encodeStart(JsonOps.INSTANCE, def).getOrThrow();

        assertEquals("#0A0B0C", json.getAsJsonObject().get("map_color").getAsString());
        assertEquals("corridor", json.getAsJsonObject().get("kind").getAsString());
        assertEquals(def, ArsPieceDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }

    @Test
    void rejectsCellsOutOfRange() {
        assertRejected(VALID.replace("\"cells\": [2, 2]", "\"cells\": [0, 2]"));
        assertRejected(VALID.replace("\"cells\": [2, 2]", "\"cells\": [2, 9]"));
        assertRejected(VALID.replace("\"cells\": [2, 2]", "\"cells\": [2]"));
        assertRejected(VALID.replace("\"cells\": [2, 2]", "\"cells\": [2, 2, 2]"));
    }

    @Test
    void rejectsConnectorOutsideFootprint() {
        assertRejected(VALID.replace("\"cell\": [0, 1]", "\"cell\": [0, 2]"));
        assertRejected(VALID.replace("\"cell\": [0, 1]", "\"cell\": [-1, 1]"));
    }

    @Test
    void rejectsConnectorSideNotOnBoundary() {
        assertRejected(VALID.replace("\"side\": \"west\"", "\"side\": \"east\"").replace("[0, 1]", "[0, 0]"));
        // Interior cell of a 3x3 footprint faces nothing outside the piece.
        assertRejected(VALID.replace("\"cells\": [2, 2]", "\"cells\": [3, 3]").replace("[0, 1]", "[1, 1]"));
    }

    @Test
    void rejectsVerticalSide() {
        assertRejected(VALID.replace("\"side\": \"west\"", "\"side\": \"up\""));
        assertRejected(VALID.replace("\"side\": \"west\"", "\"side\": \"down\""));
    }

    @Test
    void rejectsNegativeCost() {
        assertRejected(VALID.replace("\"artron_cost\": 40", "\"artron_cost\": -1"));
    }

    @Test
    void rejectsBadColour() {
        assertRejected(VALID.replace("#B08D57", "B08D57"));
        assertRejected(VALID.replace("#B08D57", "#B08D5"));
        assertRejected(VALID.replace("#B08D57", "#B08D57FF"));
        assertRejected(VALID.replace("#B08D57", "#GGGGGG"));
    }

    @Test
    void rejectsEmptyConnectors() {
        assertRejected(VALID.replace("[{ \"cell\": [0, 1], \"side\": \"west\" }]", "[]"));
    }

    @Test
    void rejectsDuplicateConnectors() {
        assertRejected(VALID.replace("[{ \"cell\": [0, 1], \"side\": \"west\" }]",
                "[{ \"cell\": [0, 1], \"side\": \"west\" }, { \"cell\": [0, 1], \"side\": \"west\" }]"));
        // Same cell, different side is fine.
        assertTrue(parse(VALID.replace("[{ \"cell\": [0, 1], \"side\": \"west\" }]",
                "[{ \"cell\": [0, 1], \"side\": \"west\" }, { \"cell\": [0, 1], \"side\": \"south\" }]"))
                .isSuccess());
    }

    @Test
    void rejectsUnknownKind() {
        assertRejected(VALID.replace("\"room\"", "\"hallway\""));
    }

    private static DataResult<ArsPieceDefinition> parse(String json) {
        return ArsPieceDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    private static void assertRejected(String json) {
        DataResult<ArsPieceDefinition> result = parse(json);
        assertTrue(result.isError(), () -> "Expected rejection of " + json + " but got " + result);
    }
}
