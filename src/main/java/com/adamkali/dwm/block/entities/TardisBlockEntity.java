package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.sound.DWMSounds;
import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.TardisLogic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TardisBlockEntity extends BlockEntity implements BlockEntityTicker<TardisBlockEntity> {
    private UUID tardisId;
    private boolean attemptedInit = false;

    // Client-side cache (synced from server via BE update/NBT)
    private boolean clientDoorOpen = false;
    private float clientDoorSwing = 0.0f;

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

            // Play sound on server so it gets broadcast to nearby clients.
            World w = this.getWorld();
            if (w != null && !w.isClient()) {
                SoundEvent soundEvent = isDoorOpen ? DWMSounds.TARDIS_DOOR_OPEN : DWMSounds.TARDIS_DOOR_CLOSE;
                w.playSoundAtBlockCenter(getPos(), soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

                this.markDirty();
                syncToClient();
            }
        }
    }

    public void syncToClient() {
        if (this.world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(this.getPos());
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, TardisBlockEntity blockEntity) {
        if (!world.isClient && !attemptedInit && this.tardisId == null) {
            attemptedInit = true;

            TardisDataModel tardis = TardisDataLoader.create();
            this.tardisId = tardis.uuid;
            generateInterior(tardis);
            this.markDirty();
            syncToClient();
        }

        if (!world.isClient) {
            TardisLogic.updateDoorState(this.tardisId);
        }
    }

    public boolean isDoorOpen() {
        if (this.world != null && this.world.isClient) {
            return clientDoorOpen;
        }

        if (this.tardisId == null) return false;
        var state = TardisLogic.getDoorState(this.tardisId);
        return state != null && state.isOpen;
    }

    public void teleportToInterior(ServerPlayerEntity player) {
        TardisDataModel tardis = TardisDataLoader.get(this.tardisId);
        if (tardis == null || tardis.interiorDimension == null || tardis.interiorEntrancePos == null) {
            return;
        }

        Identifier dimId = Identifier.of(tardis.interiorDimension);
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimId);
        ServerWorld interiorWorld = player.getServer().getWorld(worldKey);

        if (interiorWorld != null) {
            BlockPos entrance = tardis.interiorEntrancePos;
            player.teleport(interiorWorld, entrance.getX() + 0.5, entrance.getY(), entrance.getZ() + 0.5, Set.of(), player.getYaw(), player.getPitch(), true);
        }
    }

    private static long mix64(long z) {
        // SplitMix64 mix function (good deterministic scrambling)
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private void generateInterior(TardisDataModel tardis) {
        if (this.world == null || this.world.isClient) return;

        MinecraftServer server = this.world.getServer();
        if (server == null) return;

        // Use a unique dimension per TARDIS or shared one?
        // For now, let's use the one we defined: dwm:tardis_interior
        String dimName = "dwm:tardis_interior";
        tardis.interiorDimension = dimName;

        Identifier dimId = Identifier.of(dimName);
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimId);
        ServerWorld interiorWorld = server.getWorld(worldKey);

        if (interiorWorld == null) {
            System.err.println("Interior world not found: " + dimName);
            
            // If the world is null, we can't generate the interior structure.
            // But we must assign the dimension and a default position so the data model is valid.
            tardis.interiorEntrancePos = new BlockPos(0, 100, 0);
            tardis.markDirty();
            return;
        }

        // Generate at a unique *safe* location in that dimension.
        // Keep within a conservative range to avoid chunk bound crashes.
        final int spacing = 512;                 // blocks between interiors (adjust as you like)
        final int maxAbsBlock = 20_000_000;      // keep well inside vanilla-ish limits
        final int maxIndex = maxAbsBlock / spacing;

        long seed = tardis.uuid.getMostSignificantBits() ^ tardis.uuid.getLeastSignificantBits();
        long sx = mix64(seed);
        long sz = mix64(seed ^ 0x9E3779B97F4A7C15L);

        int ix = (int) (Math.floorMod(sx, (long) (2 * maxIndex + 1)) - maxIndex);
        int iz = (int) (Math.floorMod(sz, (long) (2 * maxIndex + 1)) - maxIndex);

        long bx = (long) ix * spacing;
        long bz = (long) iz * spacing;

        int safeX = (int) MathHelper.clamp(bx, -maxAbsBlock, maxAbsBlock);
        int safeZ = (int) MathHelper.clamp(bz, -maxAbsBlock, maxAbsBlock);

        // Ensure they are spaced out, e.g., 1000 blocks apart
        BlockPos genPos = new BlockPos(safeX, 100, safeZ);

        StructureTemplateManager manager = interiorWorld.getStructureTemplateManager();
        Optional<StructureTemplate> template = manager.getTemplate(Identifier.of(DWMReference.MOD_ID, "tardis_interior"));

        if (template.isPresent()) {
            template.get().place(interiorWorld, genPos, genPos, new StructurePlacementData(), interiorWorld.getRandom(), 2);
            // Assuming the entrance is at the center of the structure or relative to genPos
            // For now, just set it to genPos + some offset if known, or just genPos
            tardis.interiorEntrancePos = genPos.add(0, 1, 2); // Default offset
            tardis.markDirty();
        } else {
            System.err.println("TARDIS interior structure template not found: " + Identifier.of(DWMReference.MOD_ID, "tardis_interior"));
            tardis.interiorEntrancePos = genPos;
            tardis.markDirty();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        // Never do worldgen / side effects in writeNbt.
        if (this.tardisId != null) {
            nbt.putUuid("tardisId", this.tardisId);
        }

        nbt.putBoolean("doorOpen", isDoorOpen());

        if (this.tardisId != null) {
            var state = TardisLogic.getDoorState(this.tardisId);
            if (state != null) {
                nbt.putFloat("doorSwing", state.doorSwing);
            }
        }
        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.containsUuid("tardisId")) {
            try {
                this.tardisId = nbt.getUuid("tardisId");
            } catch (IllegalArgumentException e) {
                this.tardisId = null;
            }
        } else {
            this.tardisId = null;
        }

        if (nbt.contains("doorOpen")) {
            this.clientDoorOpen = nbt.getBoolean("doorOpen");
        }
        if (nbt.contains("doorSwing")) {
            this.clientDoorSwing = nbt.getFloat("doorSwing");
        }


        // If we loaded a valid ID, don't attempt to re-init in tick.
        this.attemptedInit = (this.tardisId != null);
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
