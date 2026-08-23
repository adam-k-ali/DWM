package com.adamkali.screenplay;

import com.adamkali.screenplay.primitive.RunCommandPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunCommandPrimitiveTest {
    @Test
    void normalizeCommandStripsASingleLeadingSlash() {
        assertEquals("give @s minecraft:diamond 1",
                RunCommandPrimitive.normalizeCommand("/give @s minecraft:diamond 1"));
        assertEquals("give @s minecraft:diamond 1",
                RunCommandPrimitive.normalizeCommand("give @s minecraft:diamond 1"));
        assertEquals("give @s diamond", RunCommandPrimitive.normalizeCommand("  /give @s diamond  "));
    }

    @Test
    void normalizeCommandRejectsBlankAndSlashOnly() {
        ScenarioException blank = assertThrows(
                ScenarioException.class,
                () -> RunCommandPrimitive.normalizeCommand("  ")
        );
        ScenarioException slash = assertThrows(
                ScenarioException.class,
                () -> RunCommandPrimitive.normalizeCommand("/")
        );
        ScenarioException missing = assertThrows(
                ScenarioException.class,
                () -> RunCommandPrimitive.normalizeCommand(null)
        );

        assertTrue(blank.getMessage().contains("runCommand requires a non-empty string 'command'"));
        assertTrue(slash.getMessage().contains("runCommand requires a non-empty string 'command'"));
        assertTrue(missing.getMessage().contains("runCommand requires a non-empty string 'command'"));
    }

    @Test
    void normalizeCommandRejectsOverlongPayload() {
        String allowed = "a".repeat(RunCommandPrimitive.MAX_COMMAND_LENGTH);
        String tooLong = "a".repeat(RunCommandPrimitive.MAX_COMMAND_LENGTH + 1);

        assertEquals(allowed, RunCommandPrimitive.normalizeCommand(allowed));
        assertEquals(allowed, RunCommandPrimitive.normalizeCommand("/" + allowed));

        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> RunCommandPrimitive.normalizeCommand(tooLong)
        );

        assertTrue(exception.getMessage().contains("runCommand must be at most 256 characters"));
    }

    @Test
    void normalizeRemapsTextToCommand() {
        Map<String, Object> normalized = RunCommandPrimitive.normalize(
                Map.of("text", "/give @s diamond"));

        assertEquals("/give @s diamond", normalized.get("command"));
        assertFalse(normalized.containsKey("text"));
    }

    @Test
    void normalizeRejectsBothCommandAndText() {
        ScenarioException exception = assertThrows(
                ScenarioException.class,
                () -> RunCommandPrimitive.normalize(Map.of(
                        "command", "/give @s diamond",
                        "text", "/give @s diamond"))
        );

        assertTrue(exception.getMessage().contains("runCommand requires a non-empty string 'command'"));
    }
}
