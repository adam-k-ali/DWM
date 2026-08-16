package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the First Doctor console. Holds {@code tardisId} for control interactions,
 * a synced chameleon variant for the Panel6 hologram preview, and synced stabilisers state.
 * Server tick maintains one {@link ConsoleControlInteractionEntity} per control.
 */
public class FirstDoctorConsoleBlockEntity extends BlockEntity {
    /** Search radius so outer-deck controls (outside the 1×1 cell) are found. */
    private static final double CONTROL_SEARCH_PADDING = 2.5;

    private @Nullable UUID tardisId;
    private TardisChameleonVariant syncedVariant = TardisChameleonVariant.TT_CAPSULE;
    private boolean syncedStabilisersEnabled = true;

    public FirstDoctorConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(
            Level world,
            BlockPos pos,
            BlockState state,
            FirstDoctorConsoleBlockEntity console
    ) {
        console.maintainControlEntities(world, pos, state);
    }

    private void maintainControlEntities(Level world, BlockPos pos, BlockState state) {
        Direction facing = state.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);
        AABB search = new AABB(pos).inflate(CONTROL_SEARCH_PADDING);
        List<ConsoleControlInteractionEntity> existing = world.getEntitiesOfClass(
                ConsoleControlInteractionEntity.class,
                search,
                entity -> entity.isBoundTo(pos)
        );

        EnumMap<LookTarget, ConsoleControlInteractionEntity> byTarget = new EnumMap<>(LookTarget.class);
        for (ConsoleControlInteractionEntity entity : existing) {
            LookTarget target = entity.getLookTarget();
            if (target == LookTarget.NONE) {
                entity.discard();
                continue;
            }
            ConsoleControlInteractionEntity prior = byTarget.putIfAbsent(target, entity);
            if (prior != null) {
                entity.discard();
            }
        }

        for (LookTarget target : LookTarget.interactiveValues()) {
            FirstDoctorConsoleControls.InteractionPose pose =
                    FirstDoctorConsoleControls.interactionPose(target, pos, facing);
            if (pose == null) {
                continue;
            }
            ConsoleControlInteractionEntity entity = byTarget.get(target);
            if (entity == null || entity.isRemoved()) {
                entity = new ConsoleControlInteractionEntity(DWMEntityTypes.CONSOLE_CONTROL, world);
                entity.bind(pos, target, pose);
                world.addFreshEntity(entity);
            } else {
                entity.applyPose(pose);
            }
        }
    }

    private void discardControlEntities() {
        if (level == null || level.isClientSide()) {
            return;
        }
        AABB search = new AABB(worldPosition).inflate(CONTROL_SEARCH_PADDING);
        List<ConsoleControlInteractionEntity> existing = level.getEntitiesOfClass(
                ConsoleControlInteractionEntity.class,
                search,
                entity -> entity.isBoundTo(worldPosition)
        );
        for (ConsoleControlInteractionEntity entity : existing) {
            entity.discard();
        }
    }

    public void setTardisId(@Nullable UUID tardisId) {
        this.tardisId = tardisId;
        setChanged();
    }

    public @Nullable UUID getTardisId() {
        return tardisId;
    }

    public TardisChameleonVariant getSyncedVariant() {
        return syncedVariant == null ? TardisChameleonVariant.TT_CAPSULE : syncedVariant;
    }

    /**
     * Updates the hologram variant and notifies tracking clients when on the server.
     */
    public void setSyncedVariant(@Nullable TardisChameleonVariant variant) {
        this.syncedVariant = variant == null ? TardisChameleonVariant.TT_CAPSULE : variant;
        setChanged();
        notifyClients();
    }

    public boolean isSyncedStabilisersEnabled() {
        return syncedStabilisersEnabled;
    }

    /**
     * Updates the stabilisers dial pose and notifies tracking clients when on the server.
     */
    public void setSyncedStabilisersEnabled(boolean enabled) {
        this.syncedStabilisersEnabled = enabled;
        setChanged();
        notifyClients();
    }

    private void notifyClients() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setRemoved() {
        discardControlEntities();
        super.setRemoved();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        discardControlEntities();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (tardisId != null) {
            output.store("tardisId", UUIDUtil.CODEC, tardisId);
        }
        output.putString("syncedVariant", getSyncedVariant().getId().toString());
        output.putBoolean("syncedStabilisersEnabled", syncedStabilisersEnabled);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tardisId = input.read("tardisId", UUIDUtil.CODEC).orElse(null);
        syncedVariant = parseVariant(input.getStringOr("syncedVariant", ""));
        syncedStabilisersEnabled = input.getBooleanOr("syncedStabilisersEnabled", true);
    }

    private static TardisChameleonVariant parseVariant(String id) {
        if (id == null || id.isBlank()) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
        try {
            return TardisChameleonVariant.fromId(identifier);
        } catch (IllegalArgumentException ignored) {
            return TardisChameleonVariant.TT_CAPSULE;
        }
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
