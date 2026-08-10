package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Crosshair tooltip for First Doctor console controls.
 */
public final class ConsoleControlHud {
    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "console_control_hud");

    private ConsoleControlHud() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, ELEMENT_ID, ConsoleControlHud::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }
        HitResult hit = client.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = client.level.getBlockState(pos);
        if (!(state.getBlock() instanceof FirstDoctorConsoleBlock)) {
            return;
        }

        Direction facing = state.getValueOrElse(FirstDoctorConsoleBlock.FACING, Direction.NORTH);
        Component label;
        if (FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, client.player)) {
            label = Component.translatable("dwm.console.materialisation_lever");
        } else {
            boolean biomeHit = FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, client.player);
            boolean planetHit = FirstDoctorConsoleControls.isPlanetLocatorLookHit(facing, pos, client.player);
            if (biomeHit && planetHit) {
                label = FirstDoctorConsoleControls.preferBiomeOverPlanet(facing, pos, client.player)
                        ? Component.translatable("dwm.console.biome_selector")
                        : Component.translatable("dwm.console.planet_locator");
            } else if (planetHit) {
                label = Component.translatable("dwm.console.planet_locator");
            } else if (biomeHit) {
                label = Component.translatable("dwm.console.biome_selector");
            } else {
                return;
            }
        }

        int textWidth = client.font.width(label);
        int x = (graphics.guiWidth() - textWidth) / 2;
        int y = graphics.guiHeight() / 2 - 15;
        graphics.text(client.font, label, x, y, 0xFFFFFF);
    }
}
