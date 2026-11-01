package com.adamkali.dwm.render.state;

import com.adamkali.dwm.tardis.logic.MonitorPage;
import com.adamkali.dwm.tardis.logic.TardisMonitorReadout;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;

/**
 * Extracted facing and block identity for interior-decor BER submit, plus the globe's animation clock
 * and the synced screen state for TARDIS monitors.
 */
public class TardisDecorBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public Block block = Blocks.AIR;
    /** Globe ambient clock in ticks (wrapped, phase-offset per position). */
    public float globeAge;
    public float globeSpin;
    public float globeEnergy;
    public boolean monitor;
    public MonitorPage page = MonitorPage.LOCATION;
    public TardisMonitorReadout readout = TardisMonitorReadout.none();
}
