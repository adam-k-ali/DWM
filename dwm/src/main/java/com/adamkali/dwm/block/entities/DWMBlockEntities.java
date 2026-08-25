package com.adamkali.dwm.block.entities;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.DWMBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DWMBlockEntities {
    public static final BlockEntityType<TardisBlockEntity> TARDIS_BLOCK_ENTITY =
            register("tardis", TardisBlockEntity::new, DWMBlocks.TARDIS_BLOCK);

    public static final BlockEntityType<TardisInteriorDoorBlockEntity> TARDIS_INTERIOR_DOOR_BLOCK_ENTITY =
            register("tardis_interior_door", TardisInteriorDoorBlockEntity::new, DWMBlocks.TARDIS_INTERIOR_DOOR);

    public static final BlockEntityType<FirstDoctorConsoleBlockEntity> FIRST_DOCTOR_CONSOLE_BLOCK_ENTITY =
            register("first_doctor_console", FirstDoctorConsoleBlockEntity::new, DWMBlocks.FIRST_DOCTOR_CONSOLE);

    public static final BlockEntityType<TardisDecorBlockEntity> TARDIS_DECOR_BLOCK_ENTITY = register(
            "tardis_decor",
            TardisDecorBlockEntity::new,
            DWMBlocks.TARDIS_GLOBE,
            DWMBlocks.TARDIS_COMPACT_SCANNER,
            DWMBlocks.TARDIS_FULL_SCANNER);

    public static void initialize() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }
}
