package com.reinkarnicaja.mod.character;

/**
 * Тип пассивного умения
 */
public enum PassiveType {
    MANA_REGEN_ON_EMPTY,      // Регенерация маны при истощении (Рудеус)
    DAMAGE_BOOST_LOW_HP,      // Урон при низком HP (Орстед)
    DASH_DAMAGE_BONUS,        // Бонус урона после рывка (Эрис)
    ELEMENT_SWAP,             // Смена стихии даёт бонус (Лаплас)
    DEMON_EYE_GIFT,           // Глаз Бога (Киширика)
    DRAGON_SWORD_REWARD,      // Награда мечом (Кальман III)
    KNOCKBACK_RESIST,         // Сопротивление отбрасыванию (Бадигади)
    SILENCE_ON_HIT,           // Молчание при попадании (Рокси)
    MANA_AURA_ALLY            // Аура маны для союзников (Рокси)
}
