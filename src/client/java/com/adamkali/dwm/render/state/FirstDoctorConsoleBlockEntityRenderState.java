package com.adamkali.dwm.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import com.adamkali.dwm.tardis.data.model.TardisChameleonVariant;

/**
 * Extracted First Doctor console BER state for the MC 26.2 submit pipeline.
 */
public class FirstDoctorConsoleBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float rotorBobOffset;
    /** Synced chameleon variant for the Panel6 hologram shell. */
    public TardisChameleonVariant variant = TardisChameleonVariant.TT_CAPSULE;
    /** Synced stabilisers toggle for Panel6 dial pose and unstabilised flight FX. */
    public boolean stabilisersEnabled = true;
    /** Whether the TARDIS is currently traveling (for rotor smoke FX). */
    public boolean traveling;
    /** Continuous turntable yaw for the chameleon hologram (degrees). */
    public float hologramYawDegrees;
    /** Vertical bob offset for the chameleon hologram (blocks, deck-local Y). */
    public float hologramBobOffset;
    public boolean cloaked;
    public boolean readerNoSignal = true;
    public float oxygen;
    public float pressure;
    public float temperature;
    public float radiation;
}
