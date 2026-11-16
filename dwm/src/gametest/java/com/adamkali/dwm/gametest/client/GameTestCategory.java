package com.adamkali.dwm.gametest.client;

import java.util.Arrays;
import java.util.Optional;

/** Groups of client GameTests, selectable with {@code -Pgametest.categories=<id>[,<id>...]}. */
public enum GameTestCategory {
    TARDIS_EXTERIOR("tardis-exterior", "TARDIS exterior, variants, globes and monitors"),
    PORTAL("portal", "BOTI/SOTO door portals: rendering, parallax, relocation, performance (slow)"),
    ENTITIES("entities", "Mob models and line-ups"),
    WORLDGEN("worldgen", "Structures and flora (Skaro outpost/trench/flora, Thal settlement)"),
    ITEMS_UI("items-ui", "Items, HUD, screens and JSON-model meshes");

    private final String id;
    private final String description;

    GameTestCategory(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public static Optional<GameTestCategory> byId(String id) {
        return Arrays.stream(values()).filter(c -> c.id.equalsIgnoreCase(id)).findFirst();
    }
}
