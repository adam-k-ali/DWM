package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
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
        LookTarget target = FirstDoctorConsoleControls.resolveLookTarget(facing, pos, client.player);
        Component label = labelFor(target);
        if (label == null) {
            return;
        }

        // GuiGraphicsExtractor.text no-ops when ARGB alpha is 0; opaque white is required in 26.2.
        int color = ARGB.opaque(0xFFFFFF);
        int textWidth = client.font.width(label);
        int x = (graphics.guiWidth() - textWidth) / 2;
        int y = graphics.guiHeight() / 2 - 15;
        graphics.text(client.font, label, x, y, color);
    }

    private static Component labelFor(LookTarget target) {
        return switch (target) {
            case BIOME_SELECTOR -> Component.translatable("dwm.console.biome_selector");
            case WAYPOINT_SELECTOR -> Component.translatable("dwm.console.waypoint_selector");
            case PLAYER_LOCATOR -> Component.translatable("dwm.console.player_locator");
            case PLANET_LOCATOR -> Component.translatable("dwm.console.planet_locator");
            case CHAMELEON_CIRCUIT -> Component.translatable("dwm.console.chameleon_circuit");
            case MATERIALISATION_LEVER -> Component.translatable("dwm.console.materialisation_lever");
            case NONE -> null;
        };
    }
}
