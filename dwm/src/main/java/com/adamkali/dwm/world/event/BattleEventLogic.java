package com.adamkali.dwm.world.event;

/**
 * Pure decisions for the Dalek/Thal skirmish event: when one may start, how big each side is, where the
 * Daleks appear and when it is over. No world access, so everything is unit-testable.
 */
public final class BattleEventLogic {
    /** Ticks between trigger checks. */
    public static final int TICK_INTERVAL = 200;
    /** Chance per check, per eligible player, that a skirmish starts. */
    public static final double TRIGGER_CHANCE = 0.1;
    /** Minimum ticks between the end of one skirmish and the start of the next. */
    public static final long COOLDOWN_TICKS = 12_000L;
    /** A skirmish is called off after this many ticks. */
    public static final long TIMEOUT_TICKS = 3_600L;
    /** Resident Thals within this many blocks of the battle centre are armed. */
    public static final double ARM_RADIUS = 48.0;
    /** Radius within which a player counts as being near a settlement for a random trigger. */
    public static final double SETTLEMENT_SEARCH_RADIUS = 48.0;
    public static final int MIN_SPAWN_DISTANCE = 40;
    public static final int MAX_SPAWN_DISTANCE = 60;
    public static final int MIN_DALEKS = 2;
    public static final int MAX_DALEKS = 4;
    public static final int MIN_REINFORCEMENTS = 2;
    public static final int MAX_REINFORCEMENTS = 3;

    private BattleEventLogic() {
    }

    public enum Outcome {
        ONGOING,
        THALS_WON,
        DALEKS_WON,
        TIMED_OUT
    }

    /** Horizontal offset from the battle centre, in blocks. */
    public record Offset(int dx, int dz) {
    }

    /**
     * @param roll uniform random value in {@code [0, 1)}
     */
    public static boolean shouldTrigger(boolean eligible, boolean battleActive, long now, long lastEndTick, double roll) {
        if (!eligible || battleActive) {
            return false;
        }
        if (now - lastEndTick < COOLDOWN_TICKS) {
            return false;
        }
        return roll < TRIGGER_CHANCE;
    }

    public static int daleks(double roll) {
        return scale(MIN_DALEKS, MAX_DALEKS, roll);
    }

    public static int reinforcements(double roll) {
        return scale(MIN_REINFORCEMENTS, MAX_REINFORCEMENTS, roll);
    }

    /**
     * Spawn offset on a ring {@link #MIN_SPAWN_DISTANCE}..{@link #MAX_SPAWN_DISTANCE} blocks from the centre.
     *
     * @param angleRoll uniform in {@code [0, 1)}; the full circle
     * @param distRoll uniform in {@code [0, 1)}; the ring width
     */
    public static Offset spawnOffset(double angleRoll, double distRoll) {
        double angle = angleRoll * Math.PI * 2.0;
        double distance = MIN_SPAWN_DISTANCE + distRoll * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
        return new Offset((int) Math.round(Math.cos(angle) * distance), (int) Math.round(Math.sin(angle) * distance));
    }

    /** A side wiped out ends the skirmish (Daleks falling first wins if both are gone); otherwise the timeout does. */
    public static Outcome evaluate(int daleksAlive, int armedThalsAlive, long elapsedTicks) {
        if (daleksAlive <= 0) {
            return Outcome.THALS_WON;
        }
        if (armedThalsAlive <= 0) {
            return Outcome.DALEKS_WON;
        }
        if (elapsedTicks >= TIMEOUT_TICKS) {
            return Outcome.TIMED_OUT;
        }
        return Outcome.ONGOING;
    }

    private static int scale(int min, int max, double roll) {
        int span = max - min + 1;
        return Math.min(max, min + (int) (Math.max(0.0, roll) * span));
    }
}
