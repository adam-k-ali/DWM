package com.adamkali.dwm.world.structure;

import com.adamkali.dwm.entity.DalekJob;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * One garrison Dalek of a {@link GarrisonedPoolElement}. Positions are template-local (before the piece's
 * rotation); {@code facing} is the direction the Dalek looks along at its post.
 *
 * @param entity    Dalek entity type id ({@code dwm:dalek} or {@code dwm:paradigm_dalek})
 * @param job       job the Dalek performs
 * @param pos       template-local post, the block the Dalek stands in
 * @param facing    local horizontal facing at the post
 * @param chance    probability in {@code [0, 1]} that the post is manned
 * @param variant   Dalek variant id; random for the entity's family when absent
 * @param radius    patrol ring radius around the post
 * @param workSpots template-local positions a technician visits
 */
public record GarrisonPost(
        Identifier entity,
        DalekJob job,
        BlockPos pos,
        Direction facing,
        float chance,
        Optional<String> variant,
        int radius,
        List<BlockPos> workSpots
) {
    public static final int DEFAULT_PATROL_RADIUS = 12;

    private static final Codec<Direction> HORIZONTAL = Direction.CODEC.validate(direction ->
            direction.getAxis().isHorizontal()
                    ? com.mojang.serialization.DataResult.success(direction)
                    : com.mojang.serialization.DataResult.error(() -> "Garrison facing must be horizontal"));

    public static final Codec<GarrisonPost> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("entity").forGetter(GarrisonPost::entity),
            Codec.STRING.xmap(DalekJob::byId, DalekJob::getSerializedName).fieldOf("job").forGetter(GarrisonPost::job),
            BlockPos.CODEC.fieldOf("pos").forGetter(GarrisonPost::pos),
            HORIZONTAL.optionalFieldOf("facing", Direction.SOUTH).forGetter(GarrisonPost::facing),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(GarrisonPost::chance),
            Codec.STRING.optionalFieldOf("variant").forGetter(GarrisonPost::variant),
            Codec.intRange(1, 48).optionalFieldOf("radius", DEFAULT_PATROL_RADIUS).forGetter(GarrisonPost::radius),
            BlockPos.CODEC.listOf().optionalFieldOf("work_spots", List.of()).forGetter(GarrisonPost::workSpots)
    ).apply(i, GarrisonPost::new));
}
