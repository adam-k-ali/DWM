package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.TardisChameleonVariant;
import com.adamkali.dwm.sound.DWMSounds;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TardisBlockEntity extends BlockEntity implements BlockEntityTicker<TardisBlockEntity> {
    private DoorState doorState = new DoorState();
    private TardisChameleonVariant variant = TardisChameleonVariant.TT_CAPSULE;

    public TardisBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.TARDIS_BLOCK_ENTITY, pos, state);
    }

    public void toggleDoor() {
        if (doorState.isDoorSwinging()) {
            return; // Prevent toggling if the door is already swinging
        }

        if (Objects.requireNonNull(this.getWorld()).isClient()) {
            SoundEvent soundEvent = doorState.isOpen() ? DWMSounds.TARDIS_DOOR_CLOSE : DWMSounds.TARDIS_DOOR_OPEN;
            this.getWorld().playSoundAtBlockCenter(getPos(), soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }

        doorState.toggle();
    }

    public void setVariant(@NotNull TardisChameleonVariant variant) {
        this.variant = variant;
    }

    public TardisChameleonVariant getVariant() {
        return variant;
    }

    @Deprecated(forRemoval = true)
    public boolean isDoorOpen() {
        return doorState.isOpen;
    }

    public DoorState getDoorState() {
        return doorState;
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, TardisBlockEntity blockEntity) {
        doorState.updateDoorSwing();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.put("doorState", doorState.toNbt());

        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        doorState = DoorState.fromNbt(nbt.getCompound("doorState"));
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

    public static class DoorState {
        private final float timeToOpenSeconds = 1.0f; // Time to open the door in seconds
        private final float timeToCloseSeconds = 0.5f; // Time to close the door in seconds
        private static final int TICK_RATE = 20; // Ticks per second

        private final float doorOpenSpeed = 1.0f / (timeToOpenSeconds * TICK_RATE); // Speed of door opening
        private final float doorCloseSpeed = 1.0f / (timeToCloseSeconds * TICK_RATE); // Speed of door closing
        private boolean isOpen;

        /**
         * A value between 0 and 1 representing the door's swing angle.
         */
        private float doorSwing;

        public DoorState() {
            this.isOpen = false;
            this.doorSwing = 0.0f;
        }

        public DoorState(boolean isOpen, float doorSwing) {
            this.isOpen = isOpen;
            this.doorSwing = doorSwing;
        }

        void updateDoorSwing() {
            if (isOpen) {
                doorSwing = Math.min(doorSwing + doorOpenSpeed, 1.0f);
            } else {
                doorSwing = Math.max(doorSwing - doorCloseSpeed, 0.0f);
            }
        }

        public boolean isDoorSwinging() {
            return doorSwing > 0.0f && doorSwing < 1.0f;
        }

        public void toggle() {
            isOpen = !isOpen;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public float getDoorSwing() {
            return doorSwing;
        }


        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("isOpen", isOpen);
            nbt.putFloat("doorSwing", doorSwing);
            return nbt;
        }

        public static DoorState fromNbt(NbtCompound nbt) {
            return new DoorState(
                    nbt.getBoolean("isOpen"),
                    nbt.getFloat("doorSwing")
            );
        }
    }
}
