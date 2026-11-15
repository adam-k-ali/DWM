package com.adamkali.dwm.tardis.interior.ars;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;

import java.util.List;

/**
 * A grid cell coordinate: either plot-grid absolute or piece-footprint local, depending on use.
 * Serialised as {@code [x, z]}.
 */
public record ArsCell(int x, int z) {
    public static final Codec<ArsCell> CODEC = Codec.INT.listOf().comapFlatMap(
            list -> Util.fixedSize(list, 2).map(xz -> new ArsCell(xz.get(0), xz.get(1))),
            cell -> List.of(cell.x, cell.z)
    );

    /** The neighbouring cell across {@code side} (horizontal directions only). */
    public ArsCell relative(Direction side) {
        return new ArsCell(x + side.getStepX(), z + side.getStepZ());
    }
}
