package com.reinkarnicaja.mod.mechanics;

import com.reinkarnicaja.mod.ReincarnationMod;
import com.reinkarnicaja.mod.data.PlayerData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер рывка (Dash) - механика для Эрис и других персонажей
 * Double-tap вперёд = рывок
 */
public class DashMechanic {

    private static final Map<UUID, DashState> DASH_STATES = new HashMap<>();
    
    // Константы рывка
    public static final int DASH_COOLDOWN_TICKS = 60; // 3 секунды
    public static final int DASH_DURATION_TICKS = 10; // 0.5 секунды
    public static final int POST_DASH_BONUS_TICKS = 100; // 5 секунд бонуса после рывка
    
    // Множитель дистанции рывка для Эрис
    public static final float ERIS_DASH_MULT = 1.5f;
    
    // Бонус урона после рывка (+40%)
    public static final float ERIS_DAMAGE_BOOST_PCT = 0.4f;
    
    // Скорость рывка
    public static final float DASH_SPEED_MULTIPLIER = 3.0f;

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
     * Тик обработки рывка
     */
    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerData data = PlayerData.get(player);
        
        if (data == null || data.getCharacter() == null) {
            return;
        }
        
        DashState state = DASH_STATES.computeIfAbsent(uuid, k -> new DashState());
        
        // Обновление кулдауна
        if (state.dashCooldown > 0) {
            state.dashCooldown--;
        }
        
        // Обновление длительности рывка
        if (state.isDashing && state.dashTicks > 0) {
            state.dashTicks--;
            
            if (state.dashTicks <= 0) {
                state.isDashing = false;
                data.setRecentlyDashing(false);
                
                // Активировать пост-рывок бонус для Эрис
                if (data.getCharacter().getKey().equals("eris")) {
                    data.setPostDashBonus(true);
                    state.postDashBonusTicks = POST_DASH_BONUS_TICKS;
                }
            } else {
                // Применение эффекта рывка (ускорение)
                applyDashMovement(player, data);
            }
        }
        
        // Обновление пост-рывок бонуса
        if (state.postDashBonusTicks > 0) {
            state.postDashBonusTicks--;
            
            if (state.postDashBonusTicks <= 0) {
                data.setPostDashBonus(false);
            }
        }
        
        // Синхронизация состояния с PlayerData
        data.setDashCooldown(state.dashCooldown);
        data.setDashReleaseTicks(state.dashTicks);
    }

    /**
     * Применить движение рывка
     */
    private static void applyDashMovement(ServerPlayerEntity player, PlayerData data) {
        Vec3d velocity = player.getVelocity();
        Vec3d forward = player.getRotationVector();
        
        // Увеличить скорость в направлении взгляда
        float dashMult = getDashMultiplier(data);
        player.setVelocity(
            forward.x * dashMult,
            velocity.y,
            forward.z * dashMult
        );
        
        player.velocityModified = true;
    }

    /**
     * Получить множитель рывка для персонажа
     */
    public static float getDashMultiplier(PlayerData data) {
        if (data == null || data.getCharacter() == null) {
            return DASH_SPEED_MULTIPLIER;
        }
        
        // Эрис имеет увеличенную дистанцию рывка
        if (data.getCharacter().getKey().equals("eris")) {
            return DASH_SPEED_MULTIPLIER * ERIS_DASH_MULT;
        }
        
        return DASH_SPEED_MULTIPLIER;
    }

    /**
     * Попытаться выполнить рывок
     * @return true если успешно
     */
    public static boolean tryDash(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        PlayerData data = PlayerData.get(player);
        
        if (data == null || data.getCharacter() == null) {
            return false;
        }
        
        // Проверить может ли персонаж делать рывок
        if (!canDash(data)) {
            return false;
        }
        
        DashState state = DASH_STATES.computeIfAbsent(uuid, k -> new DashState());
        
        // Проверить кулдаун
        if (state.dashCooldown > 0) {
            return false;
        }
        
        // Запустить рывок
        state.isDashing = true;
        state.dashTicks = DASH_DURATION_TICKS;
        state.dashCooldown = DASH_COOLDOWN_TICKS;
        
        data.setRecentlyDashing(true);
        
        ReincarnationMod.LOGGER.debug("Player {} performed dash", player.getName().getString());
        
        return true;
    }

    /**
     * Проверить может ли персонаж делать рывок
     */
    public static boolean canDash(PlayerData data) {
        if (data == null || data.getCharacter() == null) {
            return false;
        }
        
        String charKey = data.getCharacter().getKey();
        
        // Орстед и Бадигади не могут делать рывок
        if (charKey.equals("orsted") || charKey.equals("badigadi")) {
            return false;
        }
        
        return true;
    }

    /**
     * Проверить находится ли игрок в состоянии рывка
     */
    public static boolean isDashing(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        DashState state = DASH_STATES.get(uuid);
        return state != null && state.isDashing && state.dashTicks > 0;
    }

    /**
     * Получить бонус урона от рывка (для Эрис)
     */
    public static float getDashDamageBonus(PlayerData data) {
        if (data == null || !data.hasPostDashDamageBonus()) {
            return 0f;
        }
        
        // Только Эрис получает бонус урона
        if (data.getCharacter().getKey().equals("eris")) {
            return ERIS_DAMAGE_BOOST_PCT;
        }
        
        return 0f;
    }

    /**
     * Очистить состояние игрока
     */
    public static void clearPlayer(UUID uuid) {
        DASH_STATES.remove(uuid);
    }

    /**
     * Внутренний класс состояния рывка
     */
    private static class DashState {
        public boolean isDashing = false;
        public int dashTicks = 0;
        public int dashCooldown = 0;
        public int postDashBonusTicks = 0;
    }
}
