package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.block.TardisInteriorDoorBlock;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Origin-cell state for a 3×2 interior door bank. {@link TardisInteriorDoorBlock#OPEN} on the
 * blockstates is the source of truth for open/closed; this BE owns swing animation and tardisId.
 */
public class TardisInteriorDoorBlockEntity extends BlockEntity implements BlockEntityTicker<TardisInteriorDoorBlockEntity> {
    private @Nullable UUID tardisId;
    private boolean open = true;
    private float doorSwing = 1.0f;

    public TardisInteriorDoorBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY, pos, state);
        if (state.contains(TardisInteriorDoorBlock.OPEN)) {
            this.open = state.get(TardisInteriorDoorBlock.OPEN);
            this.doorSwing = this.open ? 1.0f : 0.0f;
        }
    }

    public void setTardisId(@Nullable UUID tardisId) {
        this.tardisId = tardisId;
        markDirty();
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
        this.open = open;
        markDirty();
        if (world != null) {
            BlockState state = getCachedState();
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, TardisInteriorDoorBlockEntity blockEntity) {
        if (state.contains(TardisInteriorDoorBlock.OPEN)) {
            open = state.get(TardisInteriorDoorBlock.OPEN);
        }
        if (open) {
            doorSwing = Math.min(doorSwing + 0.05f, 1f);
        } else {
            doorSwing = Math.max(doorSwing - 0.05f, 0f);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (tardisId != null) {
            nbt.putUuid("tardisId", tardisId);
        }
        nbt.putBoolean("open", open);
        nbt.putFloat("doorSwing", doorSwing);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.containsUuid("tardisId")) {
            tardisId = nbt.getUuid("tardisId");
        } else {
            tardisId = null;
        }
        open = nbt.getBoolean("open");
        doorSwing = nbt.getFloat("doorSwing");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
