package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Crosshair tooltip for First Doctor console controls (via interaction entities).
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
        if (!(hit instanceof EntityHitResult entityHit) || entityHit.getType() != HitResult.Type.ENTITY) {
            return;
        }
        if (!(entityHit.getEntity() instanceof ConsoleControlInteractionEntity control)) {
            return;
        }

        LookTarget target = control.getLookTarget();
        BlockPos consolePos = control.getConsolePos();
        boolean stabilisersOn = true;
        if (consolePos != null) {
            BlockEntity be = client.level.getBlockEntity(consolePos);
            if (be instanceof FirstDoctorConsoleBlockEntity console) {
                stabilisersOn = console.isSyncedStabilisersEnabled();
            }
        }
        Component label = labelFor(target, stabilisersOn);
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

    private static Component labelFor(LookTarget target, boolean stabilisersOn) {
        return switch (target) {
            case BIOME_SELECTOR -> Component.translatable("dwm.console.biome_selector");
            case WAYPOINT_SELECTOR -> Component.translatable("dwm.console.waypoint_selector");
            case PLAYER_LOCATOR -> Component.translatable("dwm.console.player_locator");
            case PLANET_LOCATOR -> Component.translatable("dwm.console.planet_locator");
            case CHAMELEON_CIRCUIT -> Component.translatable("dwm.console.chameleon_circuit");
            case MATERIALISATION_LEVER -> Component.translatable("dwm.console.materialisation_lever");
            case FAST_RETURN -> Component.translatable("dwm.console.fast_return");
            case STABILISERS -> Component.translatable(
                    stabilisersOn ? "dwm.console.stabilisers_on" : "dwm.console.stabilisers_off");
            case NONE -> null;
        };
    }
}
