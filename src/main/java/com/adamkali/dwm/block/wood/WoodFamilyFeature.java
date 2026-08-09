package com.adamkali.dwm.block.wood;

public enum WoodFamilyFeature {
    DOOR,
    /** Registers a 3-tall {@link TallDoorBlock} instead of vanilla {@code DoorBlock}. Mutually exclusive with {@link #DOOR}. */
    TALL_DOOR,
    /** Door uses hand-authored block JSON geometry instead of vanilla {@code door_*} parents. */
    CUSTOM_DOOR_MODEL,
    TRAPDOOR,
    /** Trapdoor uses hand-authored block JSON geometry instead of vanilla {@code template_orientable_trapdoor_*} parents. */
    CUSTOM_TRAPDOOR_MODEL
}
