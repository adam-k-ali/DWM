package com.adamkali.dwm.tardis.interior.ars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

/**
 * A doorway on one face of a cell. In an {@link ArsPieceDefinition} the cell is footprint-local
 * (unrotated); after {@code ArsLayoutLogic} places a piece it is plot-grid absolute.
 *
 * <p>JSON: {@code { "cell": [x, z], "side": "west" }}. The definition codec rejects vertical sides
 * and cells or sides that do not sit on the footprint's outer boundary.
 */
public record ArsConnector(ArsCell cell, Direction side) {
    public static final Codec<ArsConnector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ArsCell.CODEC.fieldOf("cell").forGetter(ArsConnector::cell),
            Direction.CODEC.fieldOf("side").forGetter(ArsConnector::side)
    ).apply(instance, ArsConnector::new));

    /** The cell on the other side of this connector's face. */
    public ArsCell facingCell() {
        return cell.relative(side);
    }

    /** The connector that would mate with this one: on {@link #facingCell()}, facing back. */
    public ArsConnector mate() {
        return new ArsConnector(facingCell(), side.getOpposite());
    }
}
