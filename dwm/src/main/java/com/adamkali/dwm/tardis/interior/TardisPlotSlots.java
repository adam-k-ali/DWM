package com.adamkali.dwm.tardis.interior;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Collision-free interior plot slots. Every TARDIS gets a {@link PlotSlot} the first time one is needed: the lowest
 * {@link TardisPlotAllocator#spiralSlot spiral} index whose slot no other TARDIS uses. It is stored on the
 * {@link TardisDataModel} ({@code plotSlotX}/{@code plotSlotZ}) and saved straight away, so it never changes and is
 * never handed out twice.
 *
 * <p>A TARDIS id with no model yet gets a slot held in memory ({@code UNSAVED}), reserved the same way, so nothing
 * else is given it. Every way a model comes to exist keeps the slots consistent:
 * <ul>
 *     <li>{@link TardisDataLoader#getOrCreate} for that id adopts the reserved slot and saves it;</li>
 *     <li>{@link TardisDataLoader#create} and {@code createFoundUnfinished} use fresh random ids, so never have a
 *     reservation, and allocate around every reserved slot;</li>
 *     <li>a model loaded from disk that already stores a slot keeps it, and any reservation for its id is dropped.
 *     Reservations never equal a stored slot, because allocation counts every model on disk.</li>
 * </ul>
 * Server code only asks for the plots of TARDISes that have a model (client requests are checked against the model
 * and its owner first), so reservations are rare; they are forgotten when a server starts ({@link #reset}).
 *
 * <p>Allocation and {@link #ownerAt} scan the cached models: O(TARDIS count) per call. Allocation happens once per
 * TARDIS and {@code ownerAt} only on monitor placement and {@code /tardis claim}, so no index is kept.
 */
public final class TardisPlotSlots {
    private static final Map<UUID, PlotSlot> UNSAVED = new HashMap<>();

    private TardisPlotSlots() {
    }

    /** The slot of {@code tardisId}, allocating and persisting one on first use. */
    public static synchronized PlotSlot slotOf(UUID tardisId) {
        if (tardisId == null) {
            throw new IllegalArgumentException("tardisId must not be null");
        }
        TardisDataModel model = TardisDataLoader.get(tardisId);
        PlotSlot stored = storedSlot(model);
        if (stored != null) {
            // A saved slot always wins over a reservation made before the model was known.
            UNSAVED.remove(tardisId);
            return stored;
        }
        PlotSlot pending = UNSAVED.get(tardisId);
        if (pending != null) {
            if (model != null) {
                store(model, pending);
                UNSAVED.remove(tardisId);
            }
            return pending;
        }
        PlotSlot slot = lowestFree(usedSlots());
        if (model != null) {
            store(model, slot);
        } else {
            UNSAVED.put(tardisId, slot);
        }
        return slot;
    }

    /** Allocates {@code model}'s slot now if it has none (used when a model is created). */
    public static synchronized PlotSlot ensureSlot(TardisDataModel model) {
        PlotSlot stored = storedSlot(model);
        if (stored != null) {
            return stored;
        }
        return slotOf(model.uuid);
    }

    /** The slot stored on {@code model}, or null. */
    public static @Nullable PlotSlot storedSlot(@Nullable TardisDataModel model) {
        if (model == null || model.plotSlotX == null || model.plotSlotZ == null) {
            return null;
        }
        return new PlotSlot(model.plotSlotX, model.plotSlotZ);
    }

    /** The TARDIS whose stored slot is {@code slot}, or null. Never allocates. */
    public static synchronized @Nullable UUID ownerAt(PlotSlot slot) {
        TardisDataLoader.ensureAllLoaded();
        for (TardisDataModel model : TardisDataLoader.cachedModels()) {
            if (slot.equals(storedSlot(model))) {
                return model.uuid;
            }
        }
        for (Map.Entry<UUID, PlotSlot> entry : UNSAVED.entrySet()) {
            if (entry.getValue().equals(slot)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Lowest spiral slot not in {@code used}. */
    static PlotSlot lowestFree(Set<PlotSlot> used) {
        for (long index = 0; index < TardisPlotAllocator.SLOT_COUNT; index++) {
            PlotSlot candidate = TardisPlotAllocator.spiralSlot(index);
            if (!used.contains(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Every TARDIS interior plot slot is in use");
    }

    private static Set<PlotSlot> usedSlots() {
        TardisDataLoader.ensureAllLoaded();
        Set<PlotSlot> used = new HashSet<>(UNSAVED.values());
        for (TardisDataModel model : TardisDataLoader.cachedModels()) {
            PlotSlot slot = storedSlot(model);
            if (slot != null) {
                used.add(slot);
            }
        }
        return used;
    }

    private static void store(TardisDataModel model, PlotSlot slot) {
        model.plotSlotX = slot.x();
        model.plotSlotZ = slot.z();
        TardisDataLoader.persist(model);
    }

    /**
     * Forgets the in-memory reservations of ids that have no model. Called when a server starts (they belong to the
     * world that was running before, e.g. after switching worlds in singleplayer) and by tests.
     */
    public static synchronized void reset() {
        UNSAVED.clear();
    }

    /** Number of in-memory reservations (for tests). */
    static synchronized int reservationCount() {
        return UNSAVED.size();
    }
}
