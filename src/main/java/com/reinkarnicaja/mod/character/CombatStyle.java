package com.reinkarnicaja.mod.character;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Боевые стили мода
 */
public enum CombatStyle {
    NORTH("north", Formatting.WHITE),
    WATER("water", Formatting.AQUA),
    SWORD("sword", Formatting.RED),
    MAGIC("magic", Formatting.BLUE);

    private final String key;
    private final Formatting color;

    CombatStyle(String key, Formatting color) {
        this.key = key;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public Text getDisplayName() {
        return Text.translatable("style.reinkarnicaja_mod." + key).formatted(color);
    }

    public Formatting getColor() {
        return color;
    }

    /**
     * Возвращает стиль по ключу
     */
    public static CombatStyle fromKey(String key) {
        for (CombatStyle style : values()) {
            if (style.key.equalsIgnoreCase(key)) {
                return style;
            }
        }
        return MAGIC; // default
    }
}
