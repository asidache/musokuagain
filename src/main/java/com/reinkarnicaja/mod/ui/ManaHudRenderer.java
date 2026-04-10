package com.reinkarnicaja.mod.ui;

import net.minecraft.client.gui.DrawContext;

/**
 * HUD рендерер полосы маны
 */
public class ManaHudRenderer {

    private static final int MANA_BAR_WIDTH = 180;
    private static final int MANA_BAR_HEIGHT = 12;
    private static final int PADDING = 4;

    /**
     * Рендеринг HUD маны на экране
     */
    public static void render(DrawContext context, float currentMana, float maxMana) {
        if (currentMana <= 0 || maxMana <= 0) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int x = PADDING;
        int y = screenHeight - PADDING - MANA_BAR_HEIGHT;

        // Фон полосы маны
        context.drawTexture(
            net.minecraft.util.Identifier.of("reinkarnicaja_mod", "textures/gui/mana_bar_empty.png"),
            x, y, 0, 0, MANA_BAR_WIDTH, MANA_BAR_HEIGHT, MANA_BAR_WIDTH, MANA_BAR_HEIGHT
        );

        // Заполнение маны
        float percent = Math.min(1.0f, currentMana / maxMana);
        int fillWidth = (int) (MANA_BAR_WIDTH * percent);

        if (fillWidth > 0) {
            context.drawTexture(
                net.minecraft.util.Identifier.of("reinkarnicaja_mod", "textures/gui/mana_bar_fill.png"),
                x, y, 0, 0, fillWidth, MANA_BAR_HEIGHT, fillWidth, MANA_BAR_HEIGHT
            );
        }

        // Текст с текущей маной
        String manaText = Math.round(currentMana) + " / " + Math.round(maxMana);
        context.drawTextWithShadow(
            net.minecraft.client.MinecraftClient.getInstance().textRenderer,
            manaText,
            x + MANA_BAR_WIDTH / 2 - context.getTextRenderer().getWidth(manaText) / 2,
            y + (MANA_BAR_HEIGHT - 8) / 2,
            0xFFFFFF
        );
    }

    /**
     * Получить цвет маны в зависимости от процента
     */
    public static int getManaColor(float percent) {
        if (percent > 0.6f) {
            return 0xFF00AAFF; // Синий
        } else if (percent > 0.3f) {
            return 0xFFFFFF00; // Жёлтый
        } else {
            return 0xFFFF0000; // Красный
        }
    }
}
