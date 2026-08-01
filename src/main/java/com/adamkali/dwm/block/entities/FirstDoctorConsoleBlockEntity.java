package com.adamkali.dwm.block.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Block entity for the First Doctor console. Present so the BER can attach;
 * rotor animation can be added later.
 */
public class FirstDoctorConsoleBlockEntity extends BlockEntity {
    public FirstDoctorConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(DWMBlockEntities.FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY, pos, state);
    }
}
