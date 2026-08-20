package com.adamkali.dwm.scenariotest;

public final class ScenarioException extends RuntimeException {
    public ScenarioException(String message) {
        super(message);
    }

    public ScenarioException(String message, Throwable cause) {
        super(message, cause);
    }
}
