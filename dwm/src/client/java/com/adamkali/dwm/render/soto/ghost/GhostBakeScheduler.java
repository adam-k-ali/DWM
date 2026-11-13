package com.adamkali.dwm.render.soto.ghost;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;

/**
 * Pure, deduplicating queue of chunk columns waiting to be baked, drained under a per-frame budget.
 *
 * <p>{@link Priority#PRIMARY} entries (new or changed chunks) always drain before
 * {@link Priority#REFRESH} entries (neighbour border refreshes), so refreshes coalesce: a chunk
 * that is still waiting for its first bake is baked once with full neighbour data, and a chunk
 * needing several refreshes is baked once. Within a priority, nearest-to-viewer first.
 */
final class GhostBakeScheduler {
    enum Priority { PRIMARY, REFRESH }

    private record Entry(Priority priority, long seq) {
    }

    private final Map<Long, Entry> pending = new HashMap<>();
    private long nextSeq;

    synchronized void enqueue(long chunkKey, Priority priority) {
        Entry existing = pending.get(chunkKey);
        if (existing != null) {
            if (existing.priority == Priority.PRIMARY || priority == Priority.REFRESH) {
                return; // already at least as urgent
            }
            pending.put(chunkKey, new Entry(Priority.PRIMARY, existing.seq));
            return;
        }
        pending.put(chunkKey, new Entry(priority, nextSeq++));
    }

    synchronized boolean remove(long chunkKey) {
        return pending.remove(chunkKey) != null;
    }

    synchronized void clear() {
        pending.clear();
    }

    synchronized int size() {
        return pending.size();
    }

    synchronized boolean contains(long chunkKey) {
        return pending.containsKey(chunkKey);
    }

    /**
     * Bakes queued chunks until {@code maxChunks} were handled or {@code maxNanos} elapsed
     * (at least one chunk is always handled when the queue is non-empty).
     *
     * @param distance smaller = baked earlier within the same priority
     * @return number of chunks handed to {@code baker}
     */
    int drain(int maxChunks, long maxNanos, LongSupplier clock, LongToDoubleFunction distance, LongConsumer baker) {
        long start = clock.getAsLong();
        int handled = 0;
        while (handled < Math.max(1, maxChunks)) {
            if (handled > 0 && clock.getAsLong() - start >= maxNanos) {
                break;
            }
            long next = pollBest(distance);
            if (next == Long.MIN_VALUE) {
                break;
            }
            baker.accept(next);
            handled++;
        }
        return handled;
    }

    private synchronized long pollBest(LongToDoubleFunction distance) {
        Long bestKey = null;
        Entry best = null;
        double bestDist = 0.0;
        for (Map.Entry<Long, Entry> e : pending.entrySet()) {
            Entry entry = e.getValue();
            double dist = distance.applyAsDouble(e.getKey());
            if (best == null || better(entry, dist, best, bestDist)) {
                best = entry;
                bestKey = e.getKey();
                bestDist = dist;
            }
        }
        if (bestKey == null) {
            return Long.MIN_VALUE;
        }
        pending.remove(bestKey);
        return bestKey;
    }

    private static boolean better(Entry a, double distA, Entry b, double distB) {
        if (a.priority != b.priority) {
            return a.priority == Priority.PRIMARY;
        }
        int cmp = Double.compare(distA, distB);
        if (cmp != 0) {
            return cmp < 0;
        }
        return a.seq < b.seq;
    }
}
