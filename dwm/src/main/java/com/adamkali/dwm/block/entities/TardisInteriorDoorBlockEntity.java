package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.tardis.data.model.TardisDoorState;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Origin-cell state for a 3×2 interior door bank. Linked doors mirror
 * {@code TardisDataModel.doorState.isOpen} onto {@link TardisInteriorDoorBlock#OPEN};
 * this BE owns local swing animation and tardisId.
 */
public class TardisInteriorDoorBlockEntity extends BlockEntity implements BlockEntityTicker<TardisInteriorDoorBlockEntity> {
    private @Nullable UUID tardisId;
    private boolean open = true;
    private float doorSwing = 1.0f;

    public TardisInteriorDoorBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY, pos, state);
        if (state.hasProperty(TardisInteriorDoorBlock.OPEN)) {
            this.open = state.getValue(TardisInteriorDoorBlock.OPEN);
            this.doorSwing = this.open ? 1.0f : 0.0f;
        }
    }

    public void setTardisId(@Nullable UUID tardisId) {
        this.tardisId = tardisId;
        setChanged();
    }

    public @Nullable UUID getTardisId() {
        return tardisId;
    }

    public boolean isOpen() {
        return open;
    }

    public float getDoorSwing() {
        return doorSwing;
    }

    public boolean isSwingInProgress() {
        return doorSwing > 0.0f && doorSwing < 1.0f;
    }

    public boolean isOpenEnoughForExit() {
        return open && doorSwing >= TardisDimensions.ENTRY_DOOR_SWING_THRESHOLD;
    }

    public void setOpen(boolean open) {
        setOpen(open, false);
    }

    public void setOpen(boolean open, boolean snapSwing) {
        this.open = open;
        if (snapSwing) {
            this.doorSwing = open ? 1.0f : 0.0f;
        }
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, TardisInteriorDoorBlockEntity blockEntity) {
        if (tardisId != null && !world.isClientSide()) {
            TardisLogic.updateDoorState(tardisId, world);
            TardisDoorState doorState = TardisLogic.getDoorState(tardisId);
            if (doorState != null
                    && state.hasProperty(TardisInteriorDoorBlock.OPEN)
                    && state.getValue(TardisInteriorDoorBlock.OPEN) != doorState.isOpen) {
                TardisInteriorDoorBlock.setOpen(world, pos, state, doorState.isOpen, true);
                state = world.getBlockState(pos);
            }
        }
        if (state.hasProperty(TardisInteriorDoorBlock.OPEN)) {
            open = state.getValue(TardisInteriorDoorBlock.OPEN);
        }
        if (open) {
            doorSwing = Math.min(doorSwing + 0.05f, 1f);
        } else {
            doorSwing = Math.max(doorSwing - 0.05f, 0f);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (tardisId != null) {
            output.store("tardisId", UUIDUtil.CODEC, tardisId);
        }
        output.putBoolean("open", open);
        output.putFloat("doorSwing", doorSwing);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tardisId = input.read("tardisId", UUIDUtil.CODEC).orElse(null);
        open = input.getBooleanOr("open", true);
        doorSwing = input.getFloatOr("doorSwing", 1.0f);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
