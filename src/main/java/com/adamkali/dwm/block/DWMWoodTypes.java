package com.adamkali.dwm.block;

import com.adamkali.dwm.DWMReference;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Custom wood / block-set types via Fabric builders (no mod access widener).
 * Must be registered early so client sign atlases see them when
 * {@code TexturedRenderLayers} initializes.
 */
public final class DWMWoodTypes {
    public static final BlockSetType ASH_SET = new BlockSetTypeBuilder()
            .register(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash"));
    public static final WoodType ASH = new WoodTypeBuilder()
            .register(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash"), ASH_SET);

    public static final BlockSetType DARK_ASH_SET = new BlockSetTypeBuilder()
            .register(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash"));
    public static final WoodType DARK_ASH = new WoodTypeBuilder()
            .register(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash"), DARK_ASH_SET);

    public static final BlockSetType CARDINAL_SET = new BlockSetTypeBuilder()
            .register(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal"));
    public static final WoodType CARDINAL = new WoodTypeBuilder()
            .register(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal"), CARDINAL_SET);

    private DWMWoodTypes() {
    }

    /** Forces class initialization / registration. */
    public static void initialize() {
        // no-op — static fields register on class load
    }
}
