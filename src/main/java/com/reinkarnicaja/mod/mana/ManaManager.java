package com.reinkarnicaja.mod.mana;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.rank.RankManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Менеджер маны: регенерация, тренировка через истощение, детекция рывков
 */
public class ManaManager {

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient()) return;

            ServerWorld serverWorld = (ServerWorld) world;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                tickPlayer(player);
            }
        });
    }

    /**
     * Тик обработки маны для игрока
     */
    public static void tickPlayer(ServerPlayerEntity player) {
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) return;

        // 1. Регенерация маны
        regenMana(data);

        // 2. Проверка истощения и тренировка
        checkDepletion(player, data);

        // 3. Детекция рывка (для Эрис)
        tickDashDetection(player, data);

        // 4. Слабость Кальмана при стоянии
        tickCalmanImmobility(player, data);

        // 5. Расход маны от Брони Бога Битвы (Бадигади)
        tickBattleArmorDrain(player, data);
    }

    /**
     * Регенерация маны каждый тик
     */
    private static void regenMana(PlayerData data) {
        if (data.getCurrentMana() < data.getMaxMana() && !data.isInDepletion()) {
            float regenAmount = data.getManaRegenRate() / 20f; // 20 тиков в секунду
            
            // Орстед имеет медленную регенерацию (0.5 ед/сек)
            if (data.getCharacter() == CharacterDefinition.ORSTED) {
                regenAmount = 0.5f / 20f;
            }
            
            data.regenMana(regenAmount);
        }
    }

    /**
     * Проверка истощения и добавление XP за тренировку
     */
    public static void checkDepletion(ServerPlayerEntity player, PlayerData data) {
        if (data.isInDepletion() && data.getCurrentMana() <= 0) {
            // Лаплас пропускает тренировку магии при истощении
            if (data.getCharacter() == CharacterDefinition.LAPLACE) {
                return;
            }

            // MANA_REGEN_ON_EMPTY: Рудеус получает x1.2 множитель
            int xpGain = 1;
            if (data.getCharacter() == CharacterDefinition.RUDEUS) {
                xpGain = (int) (xpGain * 1.2); // +20%
            }

            data.addManaTrainingXP(xpGain);

            // Проверка повышения уровня тренировки
            checkLevelUp(player, data);
        }
    }

    /**
     * Проверка повышения уровня тренировки маны
     */
    private static void checkLevelUp(ServerPlayerEntity player, PlayerData data) {
        int trainingXP = data.getManaTrainingXP();
        if (trainingXP >= 100) {
            // Повышение максимума маны
            data.setMaxMana(data.getMaxMana() + 10f);
            data.setManaTrainingXP(0);
            data.regenMana(50f); // Восстановить немного маны

            // Синхронизация с клиентом
            syncTo(data, player);
        }
    }

    /**
     * Детекция рывка по движению (для Эрис)
     */
    public static void tickDashDetection(ServerPlayerEntity player, PlayerData data) {
        // Реализуется в DashMechanic
        // Здесь только заглушка
    }

    /**
     * Слабость Кальмана при длительном стоянии (>2 сек = 40 тиков)
     */
    public static void tickCalmanImmobility(ServerPlayerEntity player, PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.CALMAN_III) return;

        double speed = player.getVelocity().horizontalLength();
        if (speed < 0.05) {
            data.setCalmanStationaryTicks(data.getCalmanStationaryTicks() + 1);
        } else {
            data.setCalmanStationaryTicks(0);
        }

        // Если стоит >40 тиков (2 сек) - применить слабость
        if (data.getCalmanStationaryTicks() >= 40) {
            // Применить эффект слабости
            // Реализуется через StatusEffectInstance
        }
    }

    /**
     * Расход маны от Брони Бога Битвы
     */
    private static void tickBattleArmorDrain(ServerPlayerEntity player, PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.BADIGADI) return;

        // Проверка наличия брони
        var armorStack = player.getInventory().getArmorStack(2); // Chestplate slot
        if (armorStack.isEmpty() || !armorStack.getItem().toString().contains("battle_god_armor")) {
            return;
        }

        // Расход маны: 0.1% -> 1%/сек нарастает за 20 минут
        float drainRate = data.getBattleArmorDrainRate();
        float manaCost = data.getMaxMana() * drainRate / 20f;

        if (!data.spendMana(manaCost)) {
            // Игрок не может заплатить ману - разрешить снять броню
            data.setBattleArmorCanRemove(true);
        }

        // Увеличение расхода со временем (до 1%/сек за 20 минут = 24000 тиков)
        if (drainRate < 0.01f) {
            data.setBattleArmorDrainRate(drainRate + 0.000000375f); // (0.01 - 0.001) / 24000
        }
    }

    /**
     * Применение дебаффа Лапласу при 10 сек без заклинаний
     */
    public static void applyLaplaceDebuff(ServerPlayerEntity player, PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.LAPLACE) return;

        // -30% магия, -15% скорость
        // Реализуется через StatusEffectInstance или AttributeModifier
    }

    /**
     * Синхронизация данных с клиентом
     */
    public static void syncTo(PlayerData data, ServerPlayerEntity player) {
        data.syncData(player);
    }

    /**
     * Потратить ману
     */
    public static boolean spendMana(ServerPlayerEntity player, float amount) {
        PlayerData data = PlayerData.get(player);
        if (data == null) return false;
        return data.spendMana(amount);
    }

    /**
     * Получить текущую ману игрока
     */
    public static float getCurrentMana(ServerPlayerEntity player) {
        PlayerData data = PlayerData.get(player);
        if (data == null) return 0f;
        return data.getCurrentMana();
    }

    /**
     * Получить максимальную ману игрока
     */
    public static float getMaxMana(ServerPlayerEntity player) {
        PlayerData data = PlayerData.get(player);
        if (data == null) return 100f;
        return data.getMaxMana();
    }
}
