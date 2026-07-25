package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.tardis.interior.TardisDimensions;
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

public class TardisInteriorDoorBlockEntity extends BlockEntity implements BlockEntityTicker<TardisInteriorDoorBlockEntity> {
    private @Nullable UUID tardisId;
    private boolean open = true;
    private float doorSwing = 1.0f;

    public TardisInteriorDoorBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_INTERIOR_DOOR_BLOCK_ENTITY, pos, state);
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

    public boolean isOpenEnoughForExit() {
        return open && doorSwing >= TardisDimensions.ENTRY_DOOR_SWING_THRESHOLD;
    }

    public void toggleDoor() {
        if (doorSwing > 0.0f && doorSwing < 1.0f) {
            return;
        }
        open = !open;
        markDirty();
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, TardisInteriorDoorBlockEntity blockEntity) {
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
