package com.adamkali.dwm.block.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity for the First Doctor console. Holds {@code tardisId} for control interactions;
 * mesh is drawn by the BER (rotor animation can be added later).
 */
public class FirstDoctorConsoleBlockEntity extends BlockEntity {
    private @Nullable UUID tardisId;

    public FirstDoctorConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY, pos, state);
    }

    public void setTardisId(@Nullable UUID tardisId) {
        this.tardisId = tardisId;
        markDirty();
    }

    public @Nullable UUID getTardisId() {
        return tardisId;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        if (tardisId != null) {
            nbt.putUuid("tardisId", tardisId);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.containsUuid("tardisId")) {
            tardisId = nbt.getUuid("tardisId");
        } else {
            tardisId = null;
        }
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
