package com.reinkarnicaja.mod.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.reinkarnicaja.mod.spell.SpellData;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.character.CombatStyle;

import java.util.List;

/**
 * Радиальное меню выбора заклинаний (открывается по клавише).
 */
public class SpellRadialMenu extends Screen {

    private static final Identifier RADIAL_TEXTURE = Identifier.of("reinkarnicaja_mod", "textures/gui/spell_radial.png");
    
    private final List<SpellData> availableSpells;
    private final PlayerData playerData;
    private int selectedIndex = -1;
    private double centerX;
    private double centerY;
    private double radius = 80;

    public SpellRadialMenu(List<SpellData> availableSpells, PlayerData playerData) {
        super(Text.literal("Выбор заклинания"));
        this.availableSpells = availableSpells;
        this.playerData = playerData;
    }

    @Override
    protected void init() {
        super.init();
        centerX = width / 2.0;
        centerY = height / 2.0;
        selectedIndex = -1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Полупрозрачный фон
        context.fill(0, 0, width, height, 0x40000000);

        if (availableSpells.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal("Нет доступных заклинаний"), width / 2, height / 2, 0xFF0000);
            return;
        }

        // Рисуем радиальное меню
        int numSpells = availableSpells.size();
        double angleStep = (2 * Math.PI) / numSpells;

        for (int i = 0; i < numSpells; i++) {
            SpellData spell = availableSpells.get(i);
            double angle = i * angleStep - Math.PI / 2; // Начинаем сверху
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;

            // Проверка наведения
            double dist = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
            boolean isHovered = dist < 30;
            
            if (isHovered) {
                selectedIndex = i;
            }

            // Цвет слота
            int color = (i == selectedIndex) ? 0xFFFFD700 : 0x808080;
            
            // Круг слота
            context.fill((int)(x - 25), (int)(y - 25), (int)(x + 25), (int)(y + 25), 0x40000000);
            context.drawBorder((int)(x - 25), (int)(y - 25), 50, 50, color);

            // Название заклинания (сокращённое)
            String shortName = spell.name().length() > 12 ? 
                    spell.name().substring(0, 10) + ".." : spell.name();
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal(shortName), (int)x, (int)y - 5, 0xFFFFFF);

            // Стоимость маны
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal(spell.manaCost() + " MP"), (int)x, (int)y + 8, 0x00AAFF);
        }

        // Информация о выбранном заклинании
        if (selectedIndex >= 0 && selectedIndex < availableSpells.size()) {
            SpellData spell = availableSpells.get(selectedIndex);
            
            // Панель информации
            int infoX = width / 2 - 100;
            int infoY = height - 90;
            context.fill(infoX, infoY, infoX + 200, infoY + 80, 0x80000000);
            context.drawBorder(infoX, infoY, 200, 80, 0xFFFFFF);
            
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal(spell.name()), width / 2, infoY + 5, 0xFFFF00);
            
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal("Стиль: " + spell.style().getDisplayName()), 
                    width / 2, infoY + 20, 0xAAAAAA);
            
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal("Ранг: " + spell.requiredRank().getDisplayName()), 
                    width / 2, infoY + 35, 0xAAAAAA);
            
            context.drawCenteredTextWithShadow(textRenderer, 
                    Text.literal("Урон: " + spell.damage()), 
                    width / 2, infoY + 50, 0x00FF00);
            
            if (spell.castTimeSeconds() > 0) {
                context.drawCenteredTextWithShadow(textRenderer, 
                        Text.literal("Время каста: " + spell.castTimeSeconds() + "с"), 
                        width / 2, infoY + 65, 0xFFAA00);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || selectedIndex < 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (selectedIndex < availableSpells.size()) {
            selectSpell(availableSpells.get(selectedIndex));
        }

        return true;
    }

    private void selectSpell(SpellData spell) {
        assert client != null;
        assert client.player != null;
        
        // Отправить пакет выбора заклинания
        client.player.networkHandler.sendPacket(
            new com.reinkarnicaja.mod.network.SpellSelectC2SPacket(spell.id())
        );
        
        close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    /**
     * Получить угол между центром и мышью.
     */
    private double getAngleFromCenter(int mouseX, int mouseY) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double angle = Math.atan2(dy, dx) + Math.PI / 2;
        if (angle < 0) angle += 2 * Math.PI;
        return angle;
    }
}
