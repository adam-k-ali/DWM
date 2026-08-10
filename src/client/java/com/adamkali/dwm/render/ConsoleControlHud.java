package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Crosshair tooltip for First Doctor console controls.
 */
public final class ConsoleControlHud {
    private ConsoleControlHud() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(ConsoleControlHud::render);
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.options.hideGui) {
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
        int x = (context.guiWidth() - textWidth) / 2;
        int y = context.guiHeight() / 2 - 15;
        context.drawString(client.font, label, x, y, 0xFFFFFF);
    }
}
