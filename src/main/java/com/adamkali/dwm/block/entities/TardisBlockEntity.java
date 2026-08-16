package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TardisBlockEntity extends BlockEntity implements BlockEntityTicker<TardisBlockEntity> {
    private UUID tardisId;
    private @Nullable BlockPos interiorEntrance;
    private boolean interiorGenerated;
    private boolean syncedCloaked;

    public TardisBlockEntity(UUID tardisId, BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
        this.tardisId = tardisId;
    }

    public TardisBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
    }

    public InteractionResult toggleDoor() {
        InteractionResult result = TardisLogic.toggleDoor(this.getTardisId());
        if (result == InteractionResult.SUCCESS) {
            boolean isDoorOpen = Objects.requireNonNull(TardisLogic.getDoorState(this.getTardisId())).isOpen;
            Level world = this.getLevel();
            if (world != null && !world.isClientSide()) {
                SoundEvent soundEvent = isDoorOpen ? DWMSounds.TARDIS_DOOR_OPEN : DWMSounds.TARDIS_DOOR_CLOSE;
                world.playSound(null, getBlockPos(), soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return result;
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, TardisBlockEntity blockEntity) {
        if (this.tardisId != null) {
            TardisLogic.updateDoorState(this.tardisId);
            if (!world.isClientSide()) {
                boolean cloaked = TardisLogic.isCloaked(this.tardisId);
                if (cloaked != syncedCloaked) {
                    setSyncedCloaked(cloaked);
                }
                if (this.interiorGenerated && !BotiPlotIndex.isRegistered(this.tardisId)) {
                    BotiPlotIndex.register(this.tardisId);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        if (this.tardisId == null) {
            this.tardisId = TardisDataLoader.create().uuid;
        }

        output.store("tardisId", UUIDUtil.CODEC, this.tardisId);
        output.putBoolean("interiorGenerated", this.interiorGenerated);
        output.putBoolean("syncedCloaked", this.syncedCloaked);
        if (this.interiorEntrance != null) {
            output.putInt("interiorEntranceX", this.interiorEntrance.getX());
            output.putInt("interiorEntranceY", this.interiorEntrance.getY());
            output.putInt("interiorEntranceZ", this.interiorEntrance.getZ());
        }

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.tardisId = input.read("tardisId", UUIDUtil.CODEC).orElse(null);
        this.interiorGenerated = input.getBooleanOr("interiorGenerated", false);
        this.syncedCloaked = input.getBooleanOr("syncedCloaked", false);
        if (input.getInt("interiorEntranceX").isPresent()) {
            this.interiorEntrance = new BlockPos(
                    input.getIntOr("interiorEntranceX", 0),
                    input.getIntOr("interiorEntranceY", 0),
                    input.getIntOr("interiorEntranceZ", 0)
            );
        } else {
            this.interiorEntrance = null;
        }
    }

    public UUID getTardisId() {
        if (tardisId == null) {
            tardisId = TardisDataLoader.create().uuid;
            setChanged();
        }
        return tardisId;
    }

    public @Nullable UUID getTardisIdOrNull() {
        return tardisId;
    }

    public @Nullable BlockPos getInteriorEntrance() {
        return interiorEntrance;
    }

    public void setInteriorEntrance(@Nullable BlockPos interiorEntrance) {
        this.interiorEntrance = interiorEntrance;
        setChanged();
    }

    public boolean isSyncedCloaked() {
        return syncedCloaked;
    }

    public void setSyncedCloaked(boolean cloaked) {
        this.syncedCloaked = cloaked;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    public boolean isInteriorGenerated() {
        return interiorGenerated;
    }

    public void setInteriorGenerated(boolean interiorGenerated) {
        this.interiorGenerated = interiorGenerated;
        setChanged();
    }

    /**
     * Restores identity fields after exterior relocation (dematerialise / materialise).
     */
    public void restoreTravelIdentity(UUID tardisId, @Nullable BlockPos interiorEntrance, boolean interiorGenerated) {
        this.tardisId = tardisId;
        this.interiorEntrance = interiorEntrance;
        this.interiorGenerated = interiorGenerated;
        setChanged();
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
