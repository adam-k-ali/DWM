package com.adamkali.dwm.block;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.TardisSeatEntity;
import com.adamkali.dwm.entity.TardisSeatPoses;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

/**
 * Horizontally facing TARDIS chair that spawns an invisible {@link TardisSeatEntity} on empty-hand use.
 */
public class TardisChairBlock extends TardisDecorBlock {
    private final MapCodec<TardisChairBlock> codec;
    private final double seatHeight;

    public TardisChairBlock(Properties settings, VoxelShape northShape, double seatHeight) {
        this(settings, facing -> TardisDecorShapes.rotateHorizontal(northShape, facing), seatHeight);
    }

    public TardisChairBlock(Properties settings, Function<Direction, VoxelShape> shapeFactory, double seatHeight) {
        super(settings, shapeFactory);
        this.seatHeight = seatHeight;
        this.codec = RecordCodecBuilder.mapCodec(instance ->
                instance.group(propertiesCodec()).apply(instance, props -> new TardisChairBlock(props, shapeFactory, seatHeight)));
    }

    public double seatHeight() {
        return seatHeight;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return codec;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (player.isPassenger()) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        List<TardisSeatEntity> existing = world.getEntitiesOfClass(
                TardisSeatEntity.class,
                new AABB(pos),
                seat -> seat.isSeatFor(pos) && seat.isVehicle());
        if (!existing.isEmpty()) {
            return InteractionResult.CONSUME;
        }

        Direction facing = state.getValue(FACING);
        TardisSeatPoses.SeatPose pose = TardisSeatPoses.seatPose(pos, facing, seatHeight);
        TardisSeatEntity seat = new TardisSeatEntity(DWMEntityTypes.TARDIS_SEAT, world);
        seat.bindToChair(pos, facing);
        seat.setPos(pose.position());
        seat.setYRot(pose.yaw());
        world.addFreshEntity(seat);
        if (!player.startRiding(seat)) {
            seat.discard();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }
}
