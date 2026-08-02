package com.adamkali.dwm.render;

import com.adamkali.dwm.block.FirstDoctorConsoleBlock;
import com.adamkali.dwm.block.FirstDoctorConsoleControls;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Crosshair tooltip for First Doctor console controls.
 */
public final class ConsoleControlHud {
    private ConsoleControlHud() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(ConsoleControlHud::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }
        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);
        if (!(state.getBlock() instanceof FirstDoctorConsoleBlock)) {
            return;
        }

        Direction facing = state.get(FirstDoctorConsoleBlock.FACING, Direction.NORTH);
        Text label;
        if (FirstDoctorConsoleControls.isMaterialisationLeverLookHit(facing, pos, client.player)) {
            label = Text.translatable("dwm.console.materialisation_lever");
        } else if (FirstDoctorConsoleControls.isBiomeSelectorLookHit(facing, pos, client.player)) {
            label = Text.translatable("dwm.console.biome_selector");
        } else {
            return;
        }

        int textWidth = client.textRenderer.getWidth(label);
        int x = (context.getScaledWindowWidth() - textWidth) / 2;
        int y = context.getScaledWindowHeight() / 2 - 15;
        context.drawTextWithShadow(client.textRenderer, label, x, y, 0xFFFFFF);
    }
}
