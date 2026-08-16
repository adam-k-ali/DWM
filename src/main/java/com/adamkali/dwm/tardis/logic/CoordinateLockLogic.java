package com.adamkali.dwm.tardis.logic;

import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Axis locks on Panel3. After landing resolve + scatter, pin locked axes to the
 * current exterior coordinate, then re-validate.
 */
public final class CoordinateLockLogic {
    public enum Axis {
        X,
        Y,
        Z
    }

    private CoordinateLockLogic() {
    }

    public static boolean isLocked(@Nullable TardisDataModel model, Axis axis) {
        if (model == null || axis == null) {
            return false;
        }
        return switch (axis) {
            case X -> model.lockX;
            case Y -> model.lockY;
            case Z -> model.lockZ;
        };
    }

    public static boolean anyLocked(@Nullable TardisDataModel model) {
        return isLocked(model, Axis.X) || isLocked(model, Axis.Y) || isLocked(model, Axis.Z);
    }

    /**
     * Flips one axis lock.
     *
     * @return the new locked state, or {@code false} when {@code model} is null
     */
    public static boolean toggle(@Nullable TardisDataModel model, Axis axis) {
        if (model == null || axis == null) {
            return false;
        }
        boolean next = !isLocked(model, axis);
        switch (axis) {
            case X -> model.lockX = next;
            case Y -> model.lockY = next;
            case Z -> model.lockZ = next;
        }
        model.setChanged();
        return next;
    }

    /**
     * Pins locked axes of {@code resolved} to the model's current exterior.
     * When the model has no exterior, returns {@code resolved} unchanged.
     */
    public static BlockPos apply(@Nullable BlockPos resolved, @Nullable TardisDataModel model) {
        if (resolved == null) {
            return null;
        }
        if (model == null || !model.hasExteriorLocation || !anyLocked(model)) {
            return resolved;
        }
        int x = model.lockX ? model.exteriorX : resolved.getX();
        int y = model.lockY ? model.exteriorY : resolved.getY();
        int z = model.lockZ ? model.exteriorZ : resolved.getZ();
        return new BlockPos(x, y, z);
    }
}
