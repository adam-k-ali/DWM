package com.adamkali.dwm.gui;

import com.adamkali.dwm.ClientTardis;
import com.adamkali.dwm.TardisChameleonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class TardisChameleonGui extends Screen {
    private static final TardisChameleonVariant[] variants = TardisChameleonVariant.values();
    private final ClientTardis tardis;

    public TardisChameleonGui(ClientTardis tardis) {
        super(Text.literal("Tardis Chameleon"));
        this.tardis = tardis;
    }

    private int currentVariantIndex = 0;
    private String chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();

    private static ButtonWidget upButton;
    private static ButtonWidget downButton;

    private void setVariant(int variantIndex) {
        if (variantIndex < 0 || variantIndex >= variants.length) {
            return;
        }
        currentVariantIndex = variantIndex;
        chameleonVariantName = variants[currentVariantIndex].getId().toTranslationKey();

        downButton.active = currentVariantIndex != 0;
        upButton.active = currentVariantIndex != variants.length - 1;
        if (upButton.active) {
            Tooltip tooltip = Tooltip.of(Text.translatable(variants[currentVariantIndex + 1].getId().toTranslationKey()));
            upButton.setTooltip(tooltip);
        } else {
            upButton.setTooltip(Tooltip.of(Text.literal("No more variants")));
        }
        if (downButton.active) {
            Tooltip tooltip = Tooltip.of(Text.translatable(variants[currentVariantIndex - 1].getId().toTranslationKey()));
            downButton.setTooltip(tooltip);
        } else {
            downButton.setTooltip(Tooltip.of(Text.literal("No more variants")));
        }
    }

    private void incrementVariant() {
        currentVariantIndex = (currentVariantIndex + 1) % variants.length;
        setVariant(currentVariantIndex);
    }

    private void decrementVariant() {
        currentVariantIndex = (currentVariantIndex - 1 + variants.length) % variants.length;
        setVariant(currentVariantIndex);
    }

    @Override
    protected void init() {
        setVariant(0);

        upButton = ButtonWidget.builder(Text.literal(">"), button -> {
            incrementVariant();
        }).dimensions(width / 2 + 80, this.height / 2 + 20, 20, 20).build();

        downButton = ButtonWidget.builder(Text.literal("<"), button -> {
            decrementVariant();
        }).dimensions(width / 2 - 100, this.height / 2 + 20, 20, 20).build();

        ButtonWidget saveButton = ButtonWidget.builder(Text.literal("Save"), button -> {
            this.tardis.updateChameleonVariant(variants[currentVariantIndex]);
            close();
        }).dimensions(width / 2 - 100, this.height / 2 + 50, 95, 20).build();

        ButtonWidget cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button -> {
            close();
        }).dimensions(width / 2 + 5, this.height / 2 + 50, 95, 20).build();


        addDrawableChild(upButton);
        addDrawableChild(downButton);
        addDrawableChild(saveButton);
        addDrawableChild(cancelButton);
    }

    public void drawTardis(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, BlockEntity entity) {
        float horizontalCenter = (float) (x1 + x2) / 2.0F;
        float verticalCenter = (float) (y1 + y2) / 2.0F;

        context.enableScissor(x1, y1, x2, y2);
        context.fill(x1, y1, x2, y2, 0xFF000000);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI).rotateY((float) Math.toRadians(250.0D));
        Vector3f vector3f = new Vector3f(-0.6F, 1.5F, 0.0F);
        float q = (float) size;
        drawTardis(context, horizontalCenter, verticalCenter, q, vector3f, quaternionf, entity);
        context.disableScissor();
    }

    public void drawTardis(DrawContext context, float x, float y, float size, Vector3f vector3f, Quaternionf quaternionf, BlockEntity entity) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 50.0F);
        context.getMatrices().scale(size, size, -size);
        context.getMatrices().translate(vector3f.x, vector3f.y, vector3f.z);
        context.getMatrices().multiply(quaternionf);
        context.draw();
        DiffuseLighting.method_34742();
        BlockEntityRenderDispatcher renderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();

        context.draw((vertexConsumers) -> renderDispatcher.render(entity, 0.0F, context.getMatrices(), vertexConsumers));
        context.draw();
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentWidth = 256;
        int contentHeight = 256;

        int x1 = (width - contentWidth) / 2;
        int y1 = (height) / 2 - contentHeight / 3;
        context.drawTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("textures/gui/demo_background.png"), x1, y1, 0, 0, 256, 256, 256, 256);

        super.render(context, mouseX, mouseY, delta);
        int tardisWidth = 64;
        int tardisHeight = tardisWidth * 13 / 10;
        int tardisScale = 16;

        int tardisX1 = (x1 + contentWidth + (tardisWidth / 2)) / 2;
        int tardisY1 = y1 + 10;

        int tardisX2 = tardisX1 + tardisWidth;
        int tardisY2 = tardisY1 + tardisHeight;
        drawTardis(context, tardisX1, tardisY1, tardisX2, tardisY2, tardisScale, 0.0625f, this.tardis.getTardisBlockEntity());

        int textXCenter = (tardisX1 + tardisX2) / 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable(this.chameleonVariantName), textXCenter, height / 2 + 26, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
