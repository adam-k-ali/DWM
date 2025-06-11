package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class TardisBlockEntity extends BlockEntity implements BlockEntityTicker<TardisBlockEntity> {
    private UUID tardisId;

    public TardisBlockEntity(UUID tardisId, BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
        this.tardisId = tardisId;
    }

    public TardisBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
    }

    public void toggleDoor() {
        ActionResult result = TardisLogic.toggleDoor(this.tardisId);
        if (result == ActionResult.SUCCESS) {
            boolean isDoorOpen = Objects.requireNonNull(TardisLogic.getDoorState(this.tardisId)).isOpen;
            if (Objects.requireNonNull(this.getWorld()).isClient()) {
                SoundEvent soundEvent = isDoorOpen ? DWMSounds.TARDIS_DOOR_OPEN : DWMSounds.TARDIS_DOOR_CLOSE;
                this.getWorld().playSoundAtBlockCenter(getPos(), soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, TardisBlockEntity blockEntity) {
        TardisLogic.updateDoorState(this.tardisId);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (this.tardisId == null) {
            this.tardisId = TardisDataLoader.create().uuid;
        }

        nbt.putUuid("tardisId", this.tardisId);

        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        try {
            this.tardisId = nbt.getUuid("tardisId");
        } catch (IllegalArgumentException e) {
            this.tardisId = TardisDataLoader.create().uuid;
        }
    }

    public UUID getTardisId() {
        return tardisId;
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
