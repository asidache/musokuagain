package com.reinkarnicaja.mod.spell;

import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.rank.Rank;

import java.util.List;

/**
 * Record данных заклинания
 */
public record SpellData(
    String id,
    String name,
    CombatStyle style,
    Rank requiredRank,
    int manaCost,
    float damage,
    int cooldownTicks,
    boolean isAOE,
    float castTimeSeconds,
    boolean rudeusOnly
) {
    // Конструктор по умолчанию для обратной совместимости
    public SpellData(String id, String name, CombatStyle style, Rank requiredRank, 
                     int manaCost, float damage, int cooldownTicks, boolean isAOE, float castTimeSeconds) {
        this(id, name, style, requiredRank, manaCost, damage, cooldownTicks, isAOE, castTimeSeconds, false);
    }

    // === MAGIC стиль ===
    public static final SpellData WATER_BALL = new SpellData("water_ball", "Water Ball", CombatStyle.MAGIC, Rank.BEGINNER, 10, 4f, 60, false, 0.5f);
    public static final SpellData FIRE_BALL = new SpellData("fire_ball", "Fireball", CombatStyle.MAGIC, Rank.BEGINNER, 12, 5f, 80, false, 0.6f);
    public static final SpellData FIREBOLT = new SpellData("firebolt", "Firebolt", CombatStyle.MAGIC, Rank.BEGINNER, 8, 3f, 40, false, 0.3f);
    public static final SpellData STONE_CANNON = new SpellData("stone_cannon", "Stone Cannon", CombatStyle.MAGIC, Rank.BEGINNER, 15, 6f, 100, false, 0.8f);
    public static final SpellData FIREBALL_AOE = new SpellData("fireball_aoe", "Fireball AoE", CombatStyle.MAGIC, Rank.BEGINNER, 20, 4f, 120, true, 1.0f);
    
    public static final SpellData WATER_BOLT = new SpellData("water_bolt", "Water Bolt", CombatStyle.MAGIC, Rank.INTERMEDIATE, 25, 8f, 90, false, 0.7f);
    public static final SpellData SLOSHING_WATER = new SpellData("sloshing_water", "Sloshing Water", CombatStyle.MAGIC, Rank.INTERMEDIATE, 30, 6f, 100, true, 0.9f);
    
    public static final SpellData ICE_LANCE = new SpellData("ice_lance", "Ice Lance", CombatStyle.MAGIC, Rank.ADVANCED, 35, 10f, 120, false, 1.0f);
    public static final SpellData WATER_DRAGON = new SpellData("water_dragon", "Water Dragon", CombatStyle.MAGIC, Rank.ADVANCED, 45, 8f, 140, true, 1.2f);
    
    public static final SpellData WATER_TYRANT = new SpellData("water_tyrant", "Water Tyrant", CombatStyle.MAGIC, Rank.SAINT, 60, 10f, 160, true, 1.5f);
    
    public static final SpellData LIGHTNING = new SpellData("lightning", "Lightning", CombatStyle.MAGIC, Rank.KING, 80, 15f, 200, false, 1.8f);
    
    public static final SpellData METEOR = new SpellData("meteor", "Meteor", CombatStyle.MAGIC, Rank.EMPEROR, 120, 20f, 300, true, 2.5f);

    // === NORTH стиль ===
    public static final SpellData SMALL_WIND = new SpellData("small_wind", "Small Wind", CombatStyle.NORTH, Rank.BEGINNER, 8, 3f, 50, false, 0.4f);
    public static final SpellData WHIRLWIND_BLADE = new SpellData("whirlwind_blade", "Whirlwind Blade", CombatStyle.NORTH, Rank.INTERMEDIATE, 20, 6f, 80, false, 0.6f);
    public static final SpellData DRAGON_FANG = new SpellData("dragon_fang", "Dragon Fang", CombatStyle.NORTH, Rank.ADVANCED, 35, 10f, 100, false, 0.8f);
    public static final SpellData BEAST_CLAW = new SpellData("beast_claw", "Beast Claw", CombatStyle.NORTH, Rank.ADVANCED, 30, 8f, 90, false, 0.7f);
    public static final SpellData NORTH_WIND = new SpellData("north_wind", "North Wind", CombatStyle.NORTH, Rank.SAINT, 50, 12f, 140, true, 1.2f);
    public static final SpellData FANG_OF_GOD = new SpellData("fang_of_god", "Fang of God", CombatStyle.NORTH, Rank.EMPEROR, 90, 18f, 200, false, 1.5f);

    // === WATER стиль (Рокси) ===
    public static final SpellData WATER_SLASH = new SpellData("water_slash", "Water Slash", CombatStyle.WATER, Rank.BEGINNER, 10, 4f, 60, false, 0.5f);
    public static final SpellData WATER_FLOW = new SpellData("water_flow", "Water Flow", CombatStyle.WATER, Rank.BEGINNER, 12, 3f, 70, false, 0.5f);
    public static final SpellData WATER_SLIT = new SpellData("water_slit", "Water Slit", CombatStyle.WATER, Rank.INTERMEDIATE, 22, 7f, 80, false, 0.6f);
    public static final SpellData SNAKE_FANG = new SpellData("snake_fang", "Snake Fang", CombatStyle.WATER, Rank.INTERMEDIATE, 25, 6f, 90, false, 0.7f);
    public static final SpellData RIPPLE = new SpellData("ripple", "Ripple", CombatStyle.WATER, Rank.ADVANCED, 40, 9f, 110, true, 1.0f);
    public static final SpellData WATER_GOD_DANCE = new SpellData("water_god_dance", "Water God Dance", CombatStyle.WATER, Rank.SAINT, 55, 11f, 130, false, 1.2f);
    public static final SpellData WATER_MIRROR = new SpellData("water_mirror", "Water Mirror", CombatStyle.WATER, Rank.KING, 70, 0f, 180, false, 1.0f);

    // === SWORD стиль ===
    public static final SpellData QUICK_STRIKE = new SpellData("quick_strike", "Quick Strike", CombatStyle.SWORD, Rank.BEGINNER, 5, 5f, 40, false, 0.2f);
    public static final SpellData SWORD_FLASH = new SpellData("sword_flash", "Sword Flash", CombatStyle.SWORD, Rank.INTERMEDIATE, 15, 8f, 60, false, 0.4f);
    public static final SpellData LIGHTNING_SPEED = new SpellData("lightning_speed", "Lightning Speed", CombatStyle.SWORD, Rank.ADVANCED, 25, 0f, 100, false, 0.5f);
    public static final SpellData SHADOWLESS = new SpellData("shadowless", "Shadowless", CombatStyle.SWORD, Rank.ADVANCED, 30, 12f, 80, false, 0.6f);
    public static final SpellData DARK_CUTTER = new SpellData("dark_cutter", "Dark Cutter", CombatStyle.SWORD, Rank.SAINT, 45, 14f, 120, false, 0.8f);
    public static final SpellData SWORD_GOD_FLASH = new SpellData("sword_god_flash", "Sword God Flash", CombatStyle.SWORD, Rank.KING, 65, 16f, 150, false, 1.0f);
    public static final SpellData HIDDEN_BLADE = new SpellData("hidden_blade", "Hidden Blade", CombatStyle.SWORD, Rank.KING, 60, 15f, 140, false, 0.9f);
    public static final SpellData GOD_SPEED_THRUST = new SpellData("god_speed_thrust", "God Speed Thrust", CombatStyle.SWORD, Rank.EMPEROR, 100, 22f, 250, false, 1.5f);

    /**
     * Возвращает заклинание по ID
     */
    public static SpellData getById(String id) {
        for (var field : SpellData.class.getFields()) {
            if (field.getType() == SpellData.class) {
                try {
                    SpellData spell = (SpellData) field.get(null);
                    if (spell.id().equals(id)) {
                        return spell;
                    }
                } catch (IllegalAccessException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    /**
     * Получить все заклинания (основные + расширенные)
     */
    public static List<SpellData> getAllSpells() {
        var allSpells = new java.util.ArrayList<SpellData>();
        for (var field : SpellData.class.getFields()) {
            if (field.getType() == SpellData.class) {
                try {
                    allSpells.add((SpellData) field.get(null));
                } catch (IllegalAccessException e) {
                    // ignore
                }
            }
        }
        return allSpells;
    }

    /**
     * Проверка является ли заклинание эксклюзивным для Рудеуса
     */
    public boolean isRudeusExclusive() {
        return rudeusOnly;
    }
}
