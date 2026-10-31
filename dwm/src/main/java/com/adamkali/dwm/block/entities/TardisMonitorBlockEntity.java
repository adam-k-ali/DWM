package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.tardis.data.TardisDataLoader;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.interior.TardisDimensions;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import com.adamkali.dwm.tardis.logic.TardisTravelService;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for TARDIS monitors (compact / full scanner). Shares the {@code dwm:tardis_decor}
 * type with the globe so existing saves and the console-room template keep loading; the type
 * factory picks this class for monitor blocks.
 *
 * <p>Server tick links the monitor to the TARDIS whose interior plot contains it, then refreshes a
 * synced {@link TardisMonitorReadout} about once a second. The selected {@link MonitorPage} is
 * per-monitor and shared by every viewer.
 */
public class TardisMonitorBlockEntity extends TardisDecorBlockEntity {
    static final int REFRESH_INTERVAL_TICKS = 20;
    /** Unlinked monitors retry the (disk-scanning) plot lookup less often. */
    static final int LINK_RETRY_INTERVAL_TICKS = 100;

    private @Nullable UUID tardisId;
    private MonitorPage page = MonitorPage.LOCATION;
    private TardisMonitorReadout readout = TardisMonitorReadout.none();

    public TardisMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, TardisMonitorBlockEntity monitor) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        long time = world.getGameTime();
        if (monitor.tardisId == null) {
            if (time % LINK_RETRY_INTERVAL_TICKS != 0) {
                return;
            }
            monitor.linkToInteriorPlot(serverWorld);
            if (monitor.tardisId == null) {
                monitor.setReadout(TardisMonitorReadout.none());
                return;
            }
        } else if (time % REFRESH_INTERVAL_TICKS != 0) {
            return;
        }
        monitor.refresh(serverWorld.getServer());
    }

    /** Links to the TARDIS whose interior plot contains this monitor, if any (server only). */
    public void linkToInteriorPlot(ServerLevel world) {
        if (!world.dimension().equals(TardisDimensions.TARDIS_WORLD_KEY)) {
            return;
        }
        TardisDataLoader.findAtInteriorPos(worldPosition).ifPresent(model -> setTardisId(model.uuid));
    }

    private void refresh(MinecraftServer server) {
        TardisDataModel model = tardisId == null ? null : TardisDataLoader.get(tardisId);
        boolean inFlight = tardisId != null && TardisTravelService.isTraveling(tardisId);
        ExteriorEnvironmentReadout.Sample sample = ExteriorEnvironmentReadout.sampleLinked(server, model, inFlight);
        setReadout(TardisMonitorReadout.from(model, sample, uuid -> playerName(server, uuid)));
    }

    private static @Nullable String playerName(MinecraftServer server, UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        return player == null ? null : player.getName().getString();
    }

    public void setTardisId(@Nullable UUID tardisId) {
        this.tardisId = tardisId;
        setChanged();
    }

    public @Nullable UUID getTardisId() {
        return tardisId;
    }

    public MonitorPage page() {
        return page;
    }

    public TardisMonitorReadout readout() {
        return readout;
    }

    /** Advances (or with {@code backwards}, rewinds) the page and syncs it immediately. */
    public MonitorPage cyclePage(boolean backwards) {
        page = backwards ? page.previous() : page.next();
        setChanged();
        notifyClients();
        return page;
    }

    /** Replaces the synced readout; no-op (no packet) when unchanged. */
    public void setReadout(TardisMonitorReadout next) {
        TardisMonitorReadout safe = next == null ? TardisMonitorReadout.none() : next;
        if (Objects.equals(readout, safe)) {
            return;
        }
        readout = safe;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (tardisId != null) {
            output.store("tardisId", UUIDUtil.CODEC, tardisId);
        }
        output.putString("monitorPage", page.serializedName());
        readout.write(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tardisId = input.read("tardisId", UUIDUtil.CODEC).orElse(null);
        page = MonitorPage.fromSerializedName(input.getStringOr("monitorPage", ""));
        readout = TardisMonitorReadout.read(input);
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
