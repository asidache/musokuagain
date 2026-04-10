package com.reinkarnicaja.mod.rank;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.equipment.EquipmentHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Менеджер рангов: получение XP за убийства, проверка повышения ранга
 */
public class RankManager {

    // Таблица XP за убийство мобов
    private static final int COMMON_MOB_XP = 10;
    private static final int RARE_MOB_XP = 25;
    private static final int BOSS_XP = 100;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient()) return;
            // Периодическая синхронизация или другие тик-обработчики
        });
    }

    /**
     * Обработка убийства моба - начисление XP
     */
    public static void onMobKill(ServerPlayerEntity killer, MobEntity victim) {
        if (killer == null) return;

        PlayerData data = PlayerData.get(killer);
        if (data == null || data.getCharacter() == null) return;

        int xp = computeKillXP(victim);
        
        // Орстед теряет XP при смерти жертвы (проклятие)
        if (data.getCharacter() == CharacterDefinition.ORSTED) {
            xp = (int) (xp * 0.5); // -50% XP
        }

        CombatStyle activeStyle = data.getActiveStyle();
        data.getStyleData().addXP(activeStyle, xp);
        data.addTotalXP(xp);

        // Проверка повышения ранга
        checkRankUpForStyle(killer, data, activeStyle);

        // Уведомление
        killer.sendMessage(Text.literal("+" + xp + " XP за убийство"), true);
    }

    /**
     * Вычислить XP за убийство моба
     */
    public static int computeKillXP(MobEntity mob) {
        String type = mob.getType().toString();

        // Боссы
        if (type.contains("elder_guardian") || type.contains("wither") || type.contains("ender_dragon")) {
            return BOSS_XP;
        }

        // Редкие мобы
        if (type.contains("evoker") || type.contains("vindicator") || type.contains("ravager")) {
            return RARE_MOB_XP;
        }

        // Обычные мобы
        return COMMON_MOB_XP;
    }

    /**
     * Обработка убийства босса
     */
    public static void onBossKill(ServerPlayerEntity killer, String bossId) {
        if (killer == null) return;

        PlayerData data = PlayerData.get(killer);
        if (data == null) return;

        // Босс даёт больше XP
        CombatStyle activeStyle = data.getActiveStyle();
        data.getStyleData().addXP(activeStyle, BOSS_XP * 2);

        checkRankUpForStyle(killer, data, activeStyle);
    }

    /**
     * Обработка смерти игрока - потеря XP для Орстеда
     */
    public static void onPlayerDeath(ServerPlayerEntity player) {
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() != CharacterDefinition.ORSTED) return;

        // Орстед теряет 10% XP при смерти
        CombatStyleData styleData = data.getStyleData();
        CombatStyle style = data.getActiveStyle();

        int currentXP = styleData.getXP(style);
        int loss = (int) (currentXP * 0.1);
        styleData.setXP(style, Math.max(0, currentXP - loss));

        player.sendMessage(Text.literal("Вы потеряли " + loss + " XP проклятия Орстеда"), true);
    }

    /**
     * Проверка повышения ранга для стиля
     */
    public static void checkRankUpForStyle(ServerPlayerEntity player, PlayerData data, CombatStyle style) {
        CombatStyleData styleData = data.getStyleData();
        
        if (styleData.checkRankUp(style)) {
            Rank newRank = styleData.getRank(style);
            
            // Уведомление о повышении
            Text message = Text.literal("Повышение ранга! Теперь вы: " + newRank.getDisplayName().getString());
            player.sendMessage(message, true);

            // Награда Кальмана III: меч Каджакуто при EMPEROR NORTH
            if (data.getCharacter() == CharacterDefinition.CALMAN_III && 
                style == CombatStyle.NORTH && 
                newRank == Rank.EMPEROR) {
                
                var swordStack = EquipmentHandler.KAJAKUTO_SWORD.getDefaultStack();
                player.getInventory().offerOrDrop(swordStack);
                player.sendMessage(Text.literal("Вы получили Меч Каджакуто!"), true);
            }

            // Синхронизация
            data.syncData(player);
        }
    }

    /**
     * Получить ранг игрока для стиля
     */
    public static Rank getRank(ServerPlayerEntity player, CombatStyle style) {
        PlayerData data = PlayerData.get(player);
        if (data == null) return Rank.BEGINNER;
        return data.getStyleData().getRank(style);
    }

    /**
     * Получить XP игрока для стиля
     */
    public static int getXP(ServerPlayerEntity player, CombatStyle style) {
        PlayerData data = PlayerData.get(player);
        if (data == null) return 0;
        return data.getStyleData().getXp(style);
    }

    /**
     * Добавить XP игроку для стиля
     */
    public static void addXP(ServerPlayerEntity player, CombatStyle style, int amount) {
        PlayerData data = PlayerData.get(player);
        if (data == null) return;

        data.getStyleData().addXP(style, amount);
        checkRankUpForStyle(player, data, style);
    }
}
