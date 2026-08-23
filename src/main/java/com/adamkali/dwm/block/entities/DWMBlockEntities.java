package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import com.adamkali.dwm.platform.DwmPlatform;
import com.adamkali.dwm.platform.DwmServices;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Block entity types. Fields are assigned in {@link #initialize()} after
 * {@link DwmServices#set} so registration can use the platform SPI.
 */
public class DWMBlockEntities {
    public static BlockEntityType<TardisBlockEntity> TARDIS_BLOCK_ENTITY;
    public static BlockEntityType<TardisInteriorDoorBlockEntity> TARDIS_INTERIOR_DOOR_BLOCK_ENTITY;
    public static BlockEntityType<FirstDoctorConsoleBlockEntity> FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY;
    public static BlockEntityType<TardisDecorBlockEntity> TARDIS_DECOR_BLOCK_ENTITY;

    public static void initialize() {
        if (TARDIS_BLOCK_ENTITY != null) {
            return;
        }
        TARDIS_BLOCK_ENTITY = register("tardis", TardisBlockEntity::new, DWMBlocks.TARDIS_BLOCK);
        TARDIS_INTERIOR_DOOR_BLOCK_ENTITY = register(
                "tardis_interior_door", TardisInteriorDoorBlockEntity::new, DWMBlocks.TARDIS_INTERIOR_DOOR);
        FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY = register(
                "first_doctor_console", FirstDoctorConsoleBlockEntity::new, DWMBlocks.FIRST_DOCTOR_CONSOLE);
        TARDIS_DECOR_BLOCK_ENTITY = register(
                "tardis_decor",
                TardisDecorBlockEntity::new,
                DWMBlocks.TARDIS_GLOBE,
                DWMBlocks.TARDIS_COMPACT_SCANNER,
                DWMBlocks.TARDIS_FULL_SCANNER);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            DwmPlatform.BlockEntityFactory<T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, name);
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                DwmServices.get().buildBlockEntityType(entityFactory, blocks)
        );
    }
}
