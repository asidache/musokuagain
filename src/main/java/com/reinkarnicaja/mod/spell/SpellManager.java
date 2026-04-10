package com.reinkarnicaja.mod.spell;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.passive.PassiveHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер заклинаний: доступность, каст, стоимость, кулдауны.
 */
public class SpellManager {

    /**
     * Получить доступные заклинания для игрока на основе стиля и ранга
     */
    public static List<SpellData> getAvailableSpells(PlayerData data) {
        List<SpellData> available = new ArrayList<>();
        CombatStyle style = data.getActiveStyle();
        CharacterDefinition character = data.getCharacter();

        if (style == null || character == null) {
            return available;
        }

        // Получить текущий ранг для активного стиля
        var playerRank = data.getEffectiveRank(style);
        int playerRankLevel = playerRank.getLevel();

        // Проверить все заклинания из SpellData
        for (var field : SpellData.class.getFields()) {
            if (field.getType() == SpellData.class) {
                try {
                    SpellData spell = (SpellData) field.get(null);

                    // Проверка соответствия стиля
                    if (spell.style() != style) {
                        continue;
                    }

                    // Проверка ранга
                    if (spell.requiredRank().getLevel() > playerRankLevel) {
                        continue;
                    }

                    // Проверка уникальных заклинаний Рудеуса
                    if (spell.isRudeusExclusive() && character != CharacterDefinition.RUDEUS) {
                        continue;
                    }

                    // Проверка запрета магии для Бадигади выше INTERMEDIATE
                    if (character == CharacterDefinition.BADIGADI && spell.style() == CombatStyle.MAGIC) {
                        if (spell.requiredRank().getLevel() > Rank.INTERMEDIATE.getLevel()) {
                            continue;
                        }
                    }

                    available.add(spell);
                } catch (IllegalAccessException e) {
                    // ignore
                }
            }
        }

        // Добавить расширенные заклинания
        for (SpellData spell : AdvancedSpells.getAllAdvancedSpells()) {
            // Проверка стиля
            if (spell.style() != style) {
                continue;
            }

            // Проверка ранга
            if (spell.requiredRank().getLevel() > playerRankLevel) {
                continue;
            }

            // Проверка уникальных заклинаний Рудеуса
            if (spell.isRudeusExclusive() && character != CharacterDefinition.RUDEUS) {
                continue;
            }

            available.add(spell);
        }

        return available;
    }

    /**
     * Проверить может ли игрок кастовать заклинание
     */
    public static boolean canCastSpell(ServerPlayerEntity player, String spellId) {
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) {
            return false;
        }

        SpellData spell = getSpellById(spellId);
        if (spell == null) {
            return false;
        }

        // Проверка доступности заклинания
        List<SpellData> available = getAvailableSpells(data);
        if (!available.contains(spell)) {
            return false;
        }

        // Проверка маны
        if (data.getCurrentMana() < spell.manaCost()) {
            return false;
        }

        // Проверка запрета магии для Бадигади
        if (data.getCharacter() == CharacterDefinition.BADIGADI && spell.style() == CombatStyle.MAGIC) {
            if (spell.requiredRank().getLevel() > Rank.INTERMEDIATE.getLevel()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Получить стоимость заклинания с учётом модификаторов
     */
    public static float getSpellCost(ServerPlayerEntity player, SpellData spell) {
        PlayerData data = PlayerData.get(player);
        if (data == null) {
            return spell.manaCost();
        }

        float cost = spell.manaCost();

        // Модификатор от посоха Рудеуса (-40%)
        var mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty() && mainHand.getItem().toString().contains("rudeus_staff")) {
            cost *= 0.6f;
        }

        return cost;
    }

    /**
     * Проверить может ли персонаж заряжать заклинания
     */
    public static boolean canChargeSpells(CharacterDefinition character) {
        if (character == null) return false;
        return character.canChargeSpells(); // RUDEUS, ROXY, LAPLACE, KISHIRIKA
    }

    /**
     * Каст заклинания по бинду (слот 1-9)
     * @param player игрок
     * @param slotIndex индекс слота (0-8)
     * @return true если успешно
     */
    public static boolean castSpellFromBind(ServerPlayerEntity player, int slotIndex) {
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) {
            return false;
        }

        String spellId = data.getSpellBind(slotIndex);
        if (spellId == null || spellId.isEmpty()) {
            return false;
        }

        return castSpell(player, spellId, 0f);
    }

    /**
     * Каст заклинания
     * @param player игрок
     * @param spellId ID заклинания
     * @param chargeSeconds время зарядки (для заряжаемых заклинаний)
     * @return true если успешно
     */
    public static boolean castSpell(ServerPlayerEntity player, String spellId, float chargeSeconds) {
        if (!canCastSpell(player, spellId)) {
            return false;
        }

        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) {
            return false;
        }

        SpellData spell = getSpellById(spellId);
        if (spell == null) {
            return false;
        }

        CharacterDefinition character = data.getCharacter();

        // РУДЕУС: Магия без слов - принудительно chargeSeconds=0
        // ИСКЛЮЧЕНИЕ: healing (если будет добавлено)
        if (character == CharacterDefinition.RUDEUS && !spellId.equals("healing")) {
            chargeSeconds = 0f;
        }

        // Проверка времени зарядки
        if (canChargeSpells(character)) {
            // Заклинание можно заряжать
            if (chargeSeconds > spell.castTimeSeconds()) {
                chargeSeconds = spell.castTimeSeconds();
            }
        } else {
            // Нельзя заряжать - использовать базовое время
            chargeSeconds = spell.castTimeSeconds();
        }

        // Получить стоимость
        float cost = getSpellCost(player, spell);

        // Потратить ману
        if (!data.spendMana(cost)) {
            return false;
        }

        // Лаплас: записать lastSpellStyle для ELEMENT_SWAP
        if (character == CharacterDefinition.LAPLACE) {
            CombatStyle currentStyle = spell.style();
            CombatStyle lastStyle = data.getLastSpellStyle();

            if (lastStyle != null && lastStyle != currentStyle) {
                // Смена элемента - активировать бонус +20%
                data.setElementSwapBonus(true);
            } else {
                data.setElementSwapBonus(false);
            }

            data.setLastSpellStyle(currentStyle);
        }

        // Выполнить каст
        ServerWorld world = (ServerWorld) player.getWorld();
        return SpellEffects.castSpell(player, world, spell, chargeSeconds);
    }

    /**
     * Получить заклинание по ID
     */
    public static SpellData getSpellById(String id) {
        // Сначала проверить основные заклинания
        SpellData spell = SpellData.getById(id);
        if (spell != null) {
            return spell;
        }

        // Затем проверить расширенные
        spell = AdvancedSpells.getById(id);
        return spell;
    }

    /**
     * Проверить может ли игрок переключиться на стиль
     */
    public static boolean canSwitchStyle(PlayerData data, CombatStyle newStyle) {
        return data.canSwitchStyle(newStyle);
    }
}
