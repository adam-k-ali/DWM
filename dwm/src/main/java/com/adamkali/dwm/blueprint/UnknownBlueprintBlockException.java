package com.adamkali.dwm.blueprint;

/**
 * Raised when a blueprint references a block id that is not in the registry.
 */
public final class UnknownBlueprintBlockException extends RuntimeException {
    private final String blockId;

    public UnknownBlueprintBlockException(String blockId) {
        super("Unknown block id: " + blockId);
        this.blockId = blockId;
    }

    public String blockId() {
        return blockId;
    }
}
