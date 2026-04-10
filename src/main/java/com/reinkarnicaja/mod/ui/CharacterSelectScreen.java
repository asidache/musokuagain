package com.reinkarnicaja.mod.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.reinkarnicaja.mod.character.CharacterDefinition;

/**
 * Экран выбора персонажа при первом входе на сервер.
 */
public class CharacterSelectScreen extends Screen {

    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("reinkarnicaja_mod", "textures/gui/character_select_bg.png");
    private static final int SLOT_WIDTH = 80;
    private static final int SLOT_HEIGHT = 100;
    private static final int SLOTS_PER_ROW = 4;
    
    private final CharacterDefinition[] characters = CharacterDefinition.values();
    private int selectedSlot = -1;

    public CharacterSelectScreen() {
        super(Text.literal("Выберите персонажа"));
    }

    @Override
    protected void init() {
        super.init();
        selectedSlot = -1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Тёмный фон
        context.fill(0, 0, width, height, 0x60000000);
        
        // Заголовок
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Каждый персонаж может быть выбран только одним игроком"), 
                width / 2, 35, 0xAAAAAA);

        // Слоты персонажей
        int startX = (width - (SLOTS_PER_ROW * SLOT_WIDTH + (SLOTS_PER_ROW - 1) * 10)) / 2;
        int startY = 60;
        
        for (int i = 0; i < characters.length; i++) {
            CharacterDefinition character = characters[i];
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;
            int x = startX + col * (SLOT_WIDTH + 10);
            int y = startY + row * (SLOT_HEIGHT + 10);
            
            boolean isSelected = (i == selectedSlot);
            boolean isHovered = (mouseX >= x && mouseX <= x + SLOT_WIDTH && 
                                 mouseY >= y && mouseY <= y + SLOT_HEIGHT);
            
            // Фон слота
            int bgColor = isSelected ? 0xFFD700 : (isHovered ? 0x40FFFFFF : 0x20FFFFFF);
            context.fill(x, y, x + SLOT_WIDTH, y + SLOT_HEIGHT, bgColor);
            
            // Рамка
            int borderColor = isSelected ? 0xFFFF00 : (isHovered ? 0xFFFFFF : 0x808080);
            context.drawBorder(x, y, SLOT_WIDTH, SLOT_HEIGHT, borderColor);
            
            // Имя персонажа
            String name = character.getDisplayName();
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(name), 
                    x + SLOT_WIDTH / 2, y + SLOT_HEIGHT - 25, 0xFFFFFF);
            
            // Стиль боя
            String style = character.getDefaultStyle().getDisplayName();
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(style), 
                    x + SLOT_WIDTH / 2, y + SLOT_HEIGHT - 12, 0xAAAAAA);
        }

        // Инструкция
        context.drawCenteredTextWithShadow(textRenderer, 
                Text.literal("Кликните для выбора"), width / 2, height - 30, 0x00FF00);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) { // Только ЛКМ
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int startX = (width - (SLOTS_PER_ROW * SLOT_WIDTH + (SLOTS_PER_ROW - 1) * 10)) / 2;
        int startY = 60;
        
        for (int i = 0; i < characters.length; i++) {
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;
            int x = startX + col * (SLOT_WIDTH + 10);
            int y = startY + row * (SLOT_HEIGHT + 10);
            
            if (mouseX >= x && mouseX <= x + SLOT_WIDTH && 
                mouseY >= y && mouseY <= y + SLOT_HEIGHT) {
                selectedSlot = i;
                selectCharacter(characters[i]);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectCharacter(CharacterDefinition character) {
        // Отправить пакет на сервер
        // Это будет обработано в ModPacketHandler
        assert client != null;
        assert client.player != null;
        client.player.networkHandler.sendPacket(
            new com.reinkarnicaja.mod.network.CharacterSelectC2SPacket(character.getKey())
        );
        
        // Закрыть экран
        close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Нельзя закрыть ESC, нужно выбрать персонажа
    }
}
