package com.adamkali.dwm.render.state;

import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;

/**
 * Extracted facing and block identity for interior-decor BER submit, plus the synced screen state
 * for TARDIS monitors.
 */
public class TardisDecorBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public Block block = Blocks.AIR;
    public boolean monitor;
    public MonitorPage page = MonitorPage.LOCATION;
    public TardisMonitorReadout readout = TardisMonitorReadout.none();
}
