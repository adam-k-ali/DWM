package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.boti.BotiPlotIndex;
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
    private @Nullable BlockPos interiorEntrance;
    private boolean interiorGenerated;

    public TardisBlockEntity(UUID tardisId, BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
        this.tardisId = tardisId;
    }

    public TardisBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
    }

    public void toggleDoor() {
        ActionResult result = TardisLogic.toggleDoor(this.getTardisId());
        if (result == ActionResult.SUCCESS) {
            boolean isDoorOpen = Objects.requireNonNull(TardisLogic.getDoorState(this.getTardisId())).isOpen;
            World world = this.getWorld();
            if (world != null && !world.isClient()) {
                SoundEvent soundEvent = isDoorOpen ? DWMSounds.TARDIS_DOOR_OPEN : DWMSounds.TARDIS_DOOR_CLOSE;
                world.playSound(null, getPos(), soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, TardisBlockEntity blockEntity) {
        if (this.tardisId != null) {
            TardisLogic.updateDoorState(this.tardisId);
            if (!world.isClient() && this.interiorGenerated && !BotiPlotIndex.isRegistered(this.tardisId)) {
                BotiPlotIndex.register(this.tardisId);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (this.tardisId == null) {
            this.tardisId = TardisDataLoader.create().uuid;
        }

        nbt.putUuid("tardisId", this.tardisId);
        nbt.putBoolean("interiorGenerated", this.interiorGenerated);
        if (this.interiorEntrance != null) {
            nbt.putInt("interiorEntranceX", this.interiorEntrance.getX());
            nbt.putInt("interiorEntranceY", this.interiorEntrance.getY());
            nbt.putInt("interiorEntranceZ", this.interiorEntrance.getZ());
        }

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

        this.interiorGenerated = nbt.getBoolean("interiorGenerated");
        if (nbt.contains("interiorEntranceX")) {
            this.interiorEntrance = new BlockPos(
                    nbt.getInt("interiorEntranceX"),
                    nbt.getInt("interiorEntranceY"),
                    nbt.getInt("interiorEntranceZ")
            );
        } else {
            this.interiorEntrance = null;
        }
    }

    public UUID getTardisId() {
        if (tardisId == null) {
            tardisId = TardisDataLoader.create().uuid;
            markDirty();
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
        markDirty();
    }

    public boolean isInteriorGenerated() {
        return interiorGenerated;
    }

    public void setInteriorGenerated(boolean interiorGenerated) {
        this.interiorGenerated = interiorGenerated;
        markDirty();
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
