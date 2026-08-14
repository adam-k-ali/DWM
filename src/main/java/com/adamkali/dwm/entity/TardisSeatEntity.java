package com.adamkali.dwm.entity;

import com.adamkali.dwm.block.TardisChairBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Invisible rideable seat spawned by {@link TardisChairBlock}.
 * Must be serializable ({@code EntityType} without {@code noSave}) so server {@code startRiding} accepts it.
 */
public class TardisSeatEntity extends Entity {
    private BlockPos chairPos = BlockPos.ZERO;
    private Direction chairFacing = Direction.NORTH;
    private boolean hadPassenger;

    public TardisSeatEntity(EntityType<? extends TardisSeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void bindToChair(BlockPos chairPos, Direction facing) {
        this.chairPos = chairPos.immutable();
        this.chairFacing = facing;
    }

    public BlockPos getChairPos() {
        return chairPos;
    }

    public boolean isSeatFor(BlockPos pos) {
        return chairPos.equals(pos);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (isVehicle()) {
            hadPassenger = true;
        }
        if (!isChairPresent() || (hadPassenger && !isVehicle())) {
            discard();
        }
    }

    private boolean isChairPresent() {
        BlockState state = level().getBlockState(chairPos);
        return state.getBlock() instanceof TardisChairBlock;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos inFront = chairPos.relative(chairFacing);
        return Vec3.atBottomCenterOf(inFront);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return !isVehicle();
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return true;
    }

    @Override
    public final boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        chairPos = input.read("ChairPos", BlockPos.CODEC).orElse(BlockPos.ZERO);
        chairFacing = input.read("ChairFacing", Direction.CODEC).orElse(Direction.NORTH);
        hadPassenger = input.getBooleanOr("HadPassenger", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("ChairPos", BlockPos.CODEC, chairPos);
        output.store("ChairFacing", Direction.CODEC, chairFacing);
        output.putBoolean("HadPassenger", hadPassenger);
    }
}
