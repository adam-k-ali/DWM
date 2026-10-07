package com.adamkali.dwm.blueprint;

import org.jetbrains.annotations.Nullable;

/**
 * Failure loading a named blueprint from the world save folder.
 */
public final class BlueprintLoadException extends Exception {
    public enum Reason {
        UNSAFE_NAME,
        MISSING,
        INVALID,
        IO_ERROR
    }

    private final Reason reason;
    private final String blueprintName;

    public BlueprintLoadException(Reason reason, String blueprintName) {
        this(reason, blueprintName, null);
    }

    public BlueprintLoadException(Reason reason, String blueprintName, @Nullable Throwable cause) {
        super(messageFor(reason, blueprintName), cause);
        this.reason = reason;
        this.blueprintName = blueprintName;
    }

    public Reason reason() {
        return reason;
    }

    public String blueprintName() {
        return blueprintName;
    }

    private static String messageFor(Reason reason, String name) {
        return switch (reason) {
            case UNSAFE_NAME -> "Unsafe blueprint name: " + name;
            case MISSING -> "Blueprint not found: " + name;
            case INVALID -> "Invalid blueprint JSON: " + name;
            case IO_ERROR -> "Failed to read blueprint: " + name;
        };
    }
}
