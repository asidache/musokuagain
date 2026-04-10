package com.reinkarnicaja.mod.mechanics;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.rank.Rank;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер Боевого Духа - активная способность для усиления характеристик.
 * Доступен: ERIS, CALMAN_III, ORSTED, BADIGADI, LAPLACE (только полёт)
 */
public class BattleSpiritManager {

    private static final Map<UUID, BattleSpiritState> ACTIVE_STATES = new HashMap<>();

    // Таблица поглощения урона по уровню ранга
    private static final float[] DAMAGE_ABSORPTION_BY_RANK = {
        0.20f, // BEGINNER - 20%
        0.28f, // INTERMEDIATE - 28%
        0.35f, // ADVANCED - 35%
        0.42f, // SAINT - 42%
        0.48f, // KING - 48%
        0.53f, // EMPEROR - 53%
        0.55f  // DIVINE - 55%
    };

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient()) return;

            ServerWorld serverWorld = (ServerWorld) world;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                tick(player);
            }
        });
    }

    /**
     * Тик обработки Боевого Духа
     */
    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        BattleSpiritState state = ACTIVE_STATES.get(uuid);

        if (state == null || !state.isActive()) {
            // Если не активно, но игрок Лаплас - сбросить гравитацию
            PlayerData data = PlayerData.get(player);
            if (data != null && data.getCharacter() == CharacterDefinition.LAPLACE) {
                player.setNoGravity(false);
            }
            return;
        }

        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) {
            deactivate(player);
            return;
        }

        CharacterDefinition character = data.getCharacter();

        // Расход маны в зависимости от персонажа
        float manaCostPerSec = getManaPerSecCost(data);
        float manaCostPerTick = manaCostPerSec / 20f;

        if (!data.spendMana(manaCostPerTick)) {
            // Недостаточно маны - деактивировать
            deactivate(player);
            return;
        }

        // Лаплас - полёт вместо поглощения урона
        if (character == CharacterDefinition.LAPLACE) {
            player.setNoGravity(true);
        } else {
            // Применение эффектов Боевого Духа
            applyBattleSpiritEffects(player, data);
        }
    }

    /**
     * Применить эффекты Боевого Духа
     */
    private static void applyBattleSpiritEffects(ServerPlayerEntity player, PlayerData data) {
        Rank currentRank = data.getEffectiveRank(data.getActiveStyle());
        int rankLevel = currentRank.getLevel();

        // Поглощение урона
        float absorptionPercent = getDamageAbsorptionPercent(rankLevel);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 60, 
            (int) (player.getMaxHealth() * absorptionPercent / 4), false, false));

        // Усиление атаки
        float attackBoost = getAttackDamageBoost(rankLevel);
        player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE)
            .addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                net.minecraft.util.Identifier.of("reinkarnicaja_mod", "battle_spirit_attack"),
                attackBoost,
                net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));

        // Ускорение движения
        float speedBonus = getMovementSpeedBonus(rankLevel);
        player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED)
            .addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                net.minecraft.util.Identifier.of("reinkarnicaja_mod", "battle_spirit_speed"),
                speedBonus,
                net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
    }

    /**
     * Активировать Боевой Дух
     */
    public static boolean activate(ServerPlayerEntity player) {
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) {
            return false;
        }

        if (!canUseBattleSpirit(data.getCharacter())) {
            return false;
        }

        UUID uuid = player.getUuid();
        if (ACTIVE_STATES.containsKey(uuid) && ACTIVE_STATES.get(uuid).isActive()) {
            return false; // Уже активно
        }

        ACTIVE_STATES.put(uuid, new BattleSpiritState(true));
        return true;
    }

    /**
     * Деактивировать Боевой Дух
     */
    public static void deactivate(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        ACTIVE_STATES.remove(uuid);

        // Сброс эффектов
        player.removeStatusEffect(StatusEffects.ABSORPTION);

        // Сброс гравитации для Лапласа
        PlayerData data = PlayerData.get(player);
        if (data != null && data.getCharacter() == CharacterDefinition.LAPLACE) {
            player.setNoGravity(false);
        }
    }

    /**
     * Проверить может ли персонаж использовать Боевой Дух
     */
    public static boolean canUseBattleSpirit(CharacterDefinition character) {
        if (character == null) return false;
        
        // ERIS, CALMAN_III, ORSTED, BADIGADI - полный доступ
        // LAPLACE - только полёт
        return character.canUseBattleSpirit() || character == CharacterDefinition.LAPLACE;
    }

    /**
     * Получить процент поглощения урона по уровню ранга
     */
    public static float getDamageAbsorptionPercent(int rankLevel) {
        if (rankLevel < 0 || rankLevel >= DAMAGE_ABSORPTION_BY_RANK.length) {
            return 0.20f;
        }
        return DAMAGE_ABSORPTION_BY_RANK[rankLevel];
    }

    /**
     * Получить бонус к урону атаки по уровню ранга
     */
    public static float getAttackDamageBoost(int rankLevel) {
        // 10% + 5% за каждый уровень ранга
        return 0.10f + (rankLevel * 0.05f);
    }

    /**
     * Получить расход маны в секунду
     */
    public static float getManaPerSecCost(PlayerData data) {
        if (data == null || data.getCharacter() == null) {
            return 1.0f;
        }

        CharacterDefinition character = data.getCharacter();

        // Лаплас - полёт, расход 1.5%/сек
        if (character == CharacterDefinition.LAPLACE) {
            return data.getMaxMana() * 0.015f;
        }

        // Остальные: 1% + 0.5% за каждый уровень ранга
        Rank currentRank = data.getEffectiveRank(data.getActiveStyle());
        int rankLevel = currentRank.getLevel();
        float baseCost = 0.01f + (rankLevel * 0.005f);

        return data.getMaxMana() * baseCost;
    }

    /**
     * Получить бонус к скорости движения по уровню ранга
     */
    public static float getMovementSpeedBonus(int rankLevel) {
        // 5% + 2% за каждый уровень ранга
        return 0.05f + (rankLevel * 0.02f);
    }

    /**
     * Поглотить урон через Боевой Дух
     */
    public static float absorbDamage(ServerPlayerEntity player, PlayerData data, float incomingDamage) {
        if (!isBattleSpiritActive(player)) {
            return 0f;
        }

        Rank currentRank = data.getEffectiveRank(data.getActiveStyle());
        int rankLevel = currentRank.getLevel();
        float absorptionPercent = getDamageAbsorptionPercent(rankLevel);

        return incomingDamage * absorptionPercent;
    }

    /**
     * Проверить активен ли Боевой Дух у игрока
     */
    public static boolean isBattleSpiritActive(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        BattleSpiritState state = ACTIVE_STATES.get(uuid);
        return state != null && state.isActive();
    }

    /**
     * Очистить состояние игрока
     */
    public static void clearPlayer(UUID uuid) {
        ACTIVE_STATES.remove(uuid);
    }

    /**
     * Внутренний класс состояния Боевого Духа
     */
    private static class BattleSpiritState {
        private final boolean active;

        public BattleSpiritState(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }
}
