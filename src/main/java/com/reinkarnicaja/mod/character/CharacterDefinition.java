package com.reinkarnicaja.mod.character;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Enum всех 8 персонажей вселенной Mushoku Tensei
 */
public enum CharacterDefinition {
    RUDEUS("rudeus", Formatting.AQUA, 100, 1.0f, true, true, true),
    ORSTED("orsted", Formatting.DARK_PURPLE, 150, 1.5f, false, true, false),
    ERIS("eris", Formatting.RED, 120, 1.2f, true, false, false),
    LAPLACE("laplace", Formatting.GREEN, 130, 1.1f, true, true, false),
    KISHIRIKA("kishirika", Formatting.LIGHT_PURPLE, 110, 1.0f, true, false, true),
    CALMAN_III("calman_iii", Formatting.GOLD, 115, 1.15f, true, false, false),
    BADIGADI("badigadi", Formatting.DARK_BLUE, 140, 1.3f, false, false, true),
    ROXY("roxy", Formatting.BLUE, 95, 0.95f, true, true, true);

    private final String key;
    private final Formatting color;
    private final int baseHP;
    private final float baseDamage;
    private final boolean canUseMagic;
    private final boolean canUseBattleSpirit;
    private final boolean canChargeSpells;

    CharacterDefinition(String key, Formatting color, int baseHP, float baseDamage, 
                        boolean canUseMagic, boolean canUseBattleSpirit, boolean canChargeSpells) {
        this.key = key;
        this.color = color;
        this.baseHP = baseHP;
        this.baseDamage = baseDamage;
        this.canUseMagic = canUseMagic;
        this.canUseBattleSpirit = canUseBattleSpirit;
        this.canChargeSpells = canChargeSpells;
    }

    public String getKey() {
        return key;
    }

    public Text getDisplayName() {
        return Text.translatable("character.reinkarnicaja_mod." + key).formatted(color);
    }

    public Formatting getColor() {
        return color;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public boolean canUseMagic() {
        return canUseMagic;
    }

    public boolean canUseBattleSpirit() {
        return canUseBattleSpirit;
    }

    public boolean canChargeSpells() {
        return canChargeSpells;
    }

    /**
     * Проверяет, может ли персонаж использовать Боевой Дух для полёта (только Лаплас)
     */
    public boolean canFlyWithBattleSpirit() {
        return this == LAPLACE;
    }

    /**
     * Возвращает персонажа по ключу
     */
    public static CharacterDefinition fromKey(String key) {
        for (CharacterDefinition def : values()) {
            if (def.key.equalsIgnoreCase(key)) {
                return def;
            }
        }
        return RUDEUS; // default
    }
}
