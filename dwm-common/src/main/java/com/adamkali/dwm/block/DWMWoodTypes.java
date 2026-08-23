package com.adamkali.dwm.block;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.platform.DwmServices;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Custom wood / block-set types via {@link DwmServices}.
 * Must be registered early so client sign atlases see them when
 * {@code TexturedRenderLayers} initializes.
 *
 * <p>Fields are assigned in {@link #initialize()} — call that after
 * {@link DwmServices#set} and before any code that loads {@link DWMBlocks}
 * (whose static wood-family fields read these types).
 */
public final class DWMWoodTypes {
    public static BlockSetType ASH_SET;
    public static WoodType ASH;

    public static BlockSetType DARK_ASH_SET;
    public static WoodType DARK_ASH;

    public static BlockSetType CARDINAL_SET;
    public static WoodType CARDINAL;

    private DWMWoodTypes() {
    }

    /** Registers wood / block-set types. Idempotent; safe to call more than once. */
    public static void initialize() {
        if (ASH_SET != null) {
            return;
        }
        var platform = DwmServices.get();
        ASH_SET = platform.registerBlockSetType(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash"));
        ASH = platform.registerWoodType(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "ash"), ASH_SET);

        DARK_ASH_SET = platform.registerBlockSetType(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash"));
        DARK_ASH = platform.registerWoodType(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "dark_ash"), DARK_ASH_SET);

        CARDINAL_SET = platform.registerBlockSetType(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal"));
        CARDINAL = platform.registerWoodType(Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "cardinal"), CARDINAL_SET);
    }
}
