package com.adamkali.dwm.entity;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Invisible {@link Interaction} hitbox for one First Doctor console control.
 * Spawned and maintained by {@link com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity}.
 */
public class ConsoleControlInteractionEntity extends Interaction {
    private static final EntityDataAccessor<Byte> DATA_LOOK_TARGET =
            SynchedEntityData.defineId(ConsoleControlInteractionEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_CONSOLE_POS =
            SynchedEntityData.defineId(ConsoleControlInteractionEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public ConsoleControlInteractionEntity(EntityType<? extends ConsoleControlInteractionEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setResponse(true);
    }

    public void bind(BlockPos consolePos, LookTarget target, FirstDoctorConsoleControls.InteractionPose pose) {
        setConsolePos(consolePos);
        setLookTarget(target);
        setPos(pose.position());
        setWidth(pose.width());
        setHeight(pose.height());
    }

    public boolean isBoundTo(BlockPos pos) {
        BlockPos bound = getConsolePos();
        return bound != null && bound.equals(pos);
    }

    public @Nullable BlockPos getConsolePos() {
        return entityData.get(DATA_CONSOLE_POS).orElse(null);
    }

    public void setConsolePos(@Nullable BlockPos pos) {
        entityData.set(DATA_CONSOLE_POS, Optional.ofNullable(pos == null ? null : pos.immutable()));
    }

    public LookTarget getLookTarget() {
        return lookTargetFromId(entityData.get(DATA_LOOK_TARGET));
    }

    public void setLookTarget(LookTarget target) {
        entityData.set(DATA_LOOK_TARGET, (byte) target.ordinal());
    }

    public void applyPose(FirstDoctorConsoleControls.InteractionPose pose) {
        setPos(pose.position());
        if (Math.abs(getWidth() - pose.width()) > 1.0e-3f) {
            setWidth(pose.width());
        }
        if (Math.abs(getHeight() - pose.height()) > 1.0e-3f) {
            setHeight(pose.height());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LOOK_TARGET, (byte) LookTarget.NONE.ordinal());
        builder.define(DATA_CONSOLE_POS, Optional.empty());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos consolePos = getConsolePos();
        if (consolePos == null) {
            return InteractionResult.CONSUME;
        }
        LookTarget target = getLookTarget();
        if (target == LookTarget.NONE) {
            return InteractionResult.CONSUME;
        }
        return FirstDoctorConsoleBlock.activateControl(target, level(), consolePos, player);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        BlockPos consolePos = getConsolePos();
        if (consolePos == null) {
            discard();
            return;
        }
        if (!(level().getBlockState(consolePos).getBlock() instanceof FirstDoctorConsoleBlock)) {
            discard();
        }
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
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setConsolePos(input.read("ConsolePos", BlockPos.CODEC).orElse(null));
        setLookTarget(lookTargetFromId(input.getByteOr("LookTarget", (byte) 0)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        BlockPos consolePos = getConsolePos();
        if (consolePos != null) {
            output.store("ConsolePos", BlockPos.CODEC, consolePos);
        }
        output.putByte("LookTarget", (byte) getLookTarget().ordinal());
    }

    private static LookTarget lookTargetFromId(byte id) {
        LookTarget[] values = LookTarget.values();
        int index = id & 0xFF;
        if (index < 0 || index >= values.length) {
            return LookTarget.NONE;
        }
        return values[index];
    }
}
