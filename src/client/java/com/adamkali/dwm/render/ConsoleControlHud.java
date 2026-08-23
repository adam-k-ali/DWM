package com.adamkali.dwm.render;

import com.adamkali.dwm.DWMReference;
import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;
import com.adamkali.dwm.block.entities.FirstDoctorConsoleBlockEntity;
import com.adamkali.dwm.entity.ConsoleControlInteractionEntity;
import com.adamkali.dwm.platform.DwmClientPlatform;
import com.adamkali.dwm.platform.DwmClientServices;
import com.adamkali.dwm.tardis.logic.ConsoleDisplayState;
import com.adamkali.dwm.tardis.logic.ExteriorEnvironmentReadout;
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
import org.jetbrains.annotations.Nullable;

/**
 * Crosshair tooltip for First Doctor console controls (via interaction entities).
 */
public final class ConsoleControlHud {
    private static final Identifier ELEMENT_ID =
            Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "console_control_hud");

    private ConsoleControlHud() {
    }

    public static void initialize() {
        DwmClientPlatform platform = DwmClientServices.get();
        platform.attachHudAfter(platform.hudAnchorCrosshair(), ELEMENT_ID, ConsoleControlHud::extract);
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
        FirstDoctorConsoleBlockEntity console = null;
        if (consolePos != null) {
            BlockEntity be = client.level.getBlockEntity(consolePos);
            if (be instanceof FirstDoctorConsoleBlockEntity found) {
                console = found;
            }
        }
        Component label = labelFor(target, console == null ? null : console.syncedDisplay());
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

    private static Component labelFor(LookTarget target, @Nullable ConsoleDisplayState display) {
        boolean stabilisersOn = display == null || display.stabilisersEnabled();
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
            case OXYGEN_READER -> readerLabel(display, ExteriorEnvironmentReadout.Reading::oxygen, "dwm.console.oxygen");
            case PRESSURE_READER -> readerLabel(display, ExteriorEnvironmentReadout.Reading::pressure, "dwm.console.pressure");
            case TEMPERATURE_READER -> readerLabel(display, ExteriorEnvironmentReadout.Reading::temperature, "dwm.console.temperature");
            case RADIATION_READER -> readerLabel(display, ExteriorEnvironmentReadout.Reading::radiation, "dwm.console.radiation");
            case REFUELER -> Component.translatable("dwm.console.refueler_stable");
            case TELEPATHIC_CIRCUIT -> Component.translatable("dwm.console.telepathic_circuit");
            case CLOAK -> Component.translatable(
                    display != null && display.cloaked()
                            ? "dwm.console.cloak_on"
                            : "dwm.console.cloak_off");
            case DOOR_LOCK -> Component.translatable(
                    display != null && display.doorsLocked()
                            ? "dwm.console.doors_locked"
                            : "dwm.console.doors_unlocked");
            case COORDINATE_LOCK_X -> Component.translatable(
                    display != null && display.lockX()
                            ? "dwm.console.lock_x_on"
                            : "dwm.console.lock_x_off");
            case COORDINATE_LOCK_Y -> Component.translatable(
                    display != null && display.lockY()
                            ? "dwm.console.lock_y_on"
                            : "dwm.console.lock_y_off");
            case COORDINATE_LOCK_Z -> Component.translatable(
                    display != null && display.lockZ()
                            ? "dwm.console.lock_z_on"
                            : "dwm.console.lock_z_off");
            case NONE -> null;
        };
    }

    private static Component readerLabel(
            @Nullable ConsoleDisplayState display,
            java.util.function.ToDoubleFunction<ExteriorEnvironmentReadout.Reading> value,
            String key
    ) {
        if (display == null || display.reading().noSignal()) {
            return Component.translatable("dwm.console.reader_no_signal");
        }
        ExteriorEnvironmentReadout.Reading reading = display.reading();
        int percent = Math.round(reading.needle((float) value.applyAsDouble(reading)) * 100.0F);
        return Component.translatable(key, percent);
    }
}
