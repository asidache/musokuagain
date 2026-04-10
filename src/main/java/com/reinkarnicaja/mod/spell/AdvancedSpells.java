package com.reinkarnicaja.mod.spell;

import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.rank.Rank;

import java.util.List;

/**
 * Расширение SpellData с новыми заклинаниями Рудеуса.
 * Добавляет: ELECTRIC, DISTURB_MAGIC, ABSOLUTE_ZERO, STONE_CANNON_EMP, CHAOS_MAGIC, NUCLEAR_EXPLOSION
 */
public class AdvancedSpells {

    // =========================================================================
    // НОВЫЕ ЗАКЛИНАНИЯ РУДЕУСА
    // =========================================================================

    /**
     * ELECTRIC - Точный молниевый разряд, не бьёт союзников, короткий КД
     * Только для RUDEUS
     */
    public static final SpellData ELECTRIC = new SpellData(
        "electric",
        "Electric",
        CombatStyle.MAGIC,
        Rank.ADVANCED,
        25,     // manaCost
        12,     // damage
        40,     // cooldownTicks (короткий)
        false,  // isAOE
        0.5f,   // castTimeSeconds
        true    // rudeusOnly
    );

    /**
     * DISTURB_MAGIC - Аннулирует текущий каст врага
     * Только для RUDEUS
     */
    public static final SpellData DISTURB_MAGIC = new SpellData(
        "disturb_magic",
        "Disturb Magic",
        CombatStyle.MAGIC,
        Rank.INTERMEDIATE,
        30,     // manaCost
        0,      // damage (не наносит урон, только прерывает)
        120,    // cooldownTicks
        false,  // isAOE
        0.3f,   // castTimeSeconds (быстрый)
        true    // rudeusOnly
    );

    /**
     * ABSOLUTE_ZERO - Тотальная заморозка области
     * Доступен любому магу KING ранга
     */
    public static final SpellData ABSOLUTE_ZERO = new SpellData(
        "absolute_zero",
        "Absolute Zero",
        CombatStyle.MAGIC,
        Rank.KING,
        80,     // manaCost
        18,     // damage
        300,    // cooldownTicks
        true,   // isAOE
        2.5f,   // castTimeSeconds
        false   // rudeusOnly
    );

    /**
     * STONE_CANNON_EMP - Усиленная Stone Cannon, пробивает Battle Aura
     * Доступен любому магу EMPEROR ранга
     */
    public static final SpellData STONE_CANNON_EMP = new SpellData(
        "stone_cannon_emp",
        "Stone Cannon EMP",
        CombatStyle.MAGIC,
        Rank.EMPEROR,
        60,     // manaCost
        22,     // damage
        100,    // cooldownTicks
        false,  // isAOE
        1.0f,   // castTimeSeconds
        false   // rudeusOnly
    );

    /**
     * CHAOS_MAGIC - Каскад всех стихий
     * Доступен любому магу EMPEROR ранга
     */
    public static final SpellData CHAOS_MAGIC = new SpellData(
        "chaos_magic",
        "Chaos Magic",
        CombatStyle.MAGIC,
        Rank.EMPEROR,
        100,    // manaCost
        25,     // damage
        400,    // cooldownTicks
        true,   // isAOE
        3.0f,   // castTimeSeconds
        false   // rudeusOnly
    );

    /**
     * NUCLEAR_EXPLOSION - Огонь+Воздух (Melded). Использован против Орстеда
     * Доступен любому магу DIVINE ранга
     */
    public static final SpellData NUCLEAR_EXPLOSION = new SpellData(
        "nuclear_explosion",
        "Nuclear Explosion",
        CombatStyle.MAGIC,
        Rank.DIVINE,
        200,    // manaCost
        50,     // damage
        600,    // cooldownTicks
        true,   // isAOE
        4.0f,   // castTimeSeconds
        false   // rudeusOnly
    );

    /**
     * Массив всех новых заклинаний
     */
    public static final SpellData[] ALL_ADVANCED_SPELLS = {
        ELECTRIC,
        DISTURB_MAGIC,
        ABSOLUTE_ZERO,
        STONE_CANNON_EMP,
        CHAOS_MAGIC,
        NUCLEAR_EXPLOSION
    };

    /**
     * Проверка является ли заклинание эксклюзивным для Рудеуса
     */
    public static boolean isRudeusExclusive(SpellData spell) {
        return spell == ELECTRIC || spell == DISTURB_MAGIC;
    }

    /**
     * Получить все заклинания (основные + расширенные)
     */
    public static List<SpellData> getAllAdvancedSpells() {
        return java.util.Arrays.asList(ALL_ADVANCED_SPELLS);
    }

    /**
     * Получить заклинание по ID из расширенных
     */
    public static SpellData getById(String id) {
        for (SpellData spell : ALL_ADVANCED_SPELLS) {
            if (spell.id().equals(id)) {
                return spell;
            }
        }
        return null;
    }
}
