package com.reinkarnicaja.mod.passive;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Обработчик системы даров Глаза Бога для Киширики.
 * Открывается при использовании еды рядом с игроком-Киширикой.
 */
public class DemonEyeGiftHandler {

    /**
     * Типы даров Глаза Бога
     */
    public enum DemonEyeGift {
        EYE_NIGHT_VISION("eye_night_vision", "Ночное зрение навсегда"),
        EYE_FAR_SIGHT("eye_far_sight", "Обнаружение сущностей сквозь стены"),
        EYE_DEMON("eye_demon", "Постоянный GLOWING на врагов"),
        EYE_FATE("eye_fate", "Отображение HP врагов в HUD"),
        EYE_VOID("eye_void", "Иммунитет к слепоте и темноте");

        private final String key;
        private final String description;

        DemonEyeGift(String key, String description) {
            this.key = key;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }
    }

    public static void register() {
        // Обработка использования еды рядом с Киширикой
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && stack.getItem().getFoodComponent() != null) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                PlayerData data = PlayerData.get(serverPlayer);
                
                if (data != null && data.getCharacter() == CharacterDefinition.KISHIRIKA) {
                    // Поиск других игроков-Киширик в радиусе 3 блоков
                    Box searchBox = new Box(player.getBlockPos()).expand(3.0);
                    List<ServerPlayerEntity> nearbyPlayers = ((ServerWorld) world)
                        .getEntitiesByClass(ServerPlayerEntity.class, searchBox, p -> p != player);
                    
                    for (ServerPlayerEntity nearby : nearbyPlayers) {
                        PlayerData nearbyData = PlayerData.get(nearby);
                        if (nearbyData != null && nearbyData.getCharacter() == CharacterDefinition.KISHIRIKA) {
                            // Открыть экран выбора дара
                            // Это будет обработано через сеть - отправка пакета клиенту
                            openDemonEyeSelectScreen(serverPlayer);
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
            
            return ActionResult.PASS;
        });
    }

    /**
     * Открыть экран выбора дара Глаза Бога
     */
    private static void openDemonEyeSelectScreen(ServerPlayerEntity player) {
        // Отправка пакета клиенту для открытия DemonEyeSelectScreen
        // Реализуется через сеть
    }

    /**
     * Применить выбранный дар к игроку
     */
    public static void applyGift(ServerPlayerEntity player, PlayerData data, DemonEyeGift gift) {
        switch (gift) {
            case EYE_NIGHT_VISION -> {
                // Ночное зрение навсегда (бесконечный эффект)
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                data.setHasDemonEyeNightVision(true);
            }
            case EYE_FAR_SIGHT -> {
                // Обнаружение сущностей сквозь стены
                data.setHasDemonEyeFarSight(true);
            }
            case EYE_DEMON -> {
                // Постоянный GLOWING на врагов
                data.setHasDemonEyeDemon(true);
            }
            case EYE_FATE -> {
                // Отображение HP врагов в HUD
                data.setHasDemonEyeFate(true);
            }
            case EYE_VOID -> {
                // Иммунитет к слепоте и темноте
                data.setHasDemonEyeVoid(true);
            }
        }
        
        // Синхронизация с клиентом
        // ManaManager.syncTo(data, player);
    }

    /**
     * Тик эффектов Глаза Бога
     */
    public static void tick(ServerPlayerEntity player, PlayerData data, ServerWorld world) {
        // EYE_FAR_SIGHT: обнаружение сущностей сквозь стены
        if (data.hasDemonEyeFarSight()) {
            // Подсветка сущностей в радиусе 32 блоков
            Box searchBox = new Box(player.getBlockPos()).expand(32.0);
            List<MobEntity> nearbyMobs = world.getEntitiesByClass(MobEntity.class, searchBox, e -> 
                !e.isAllied(player) && e.isAlive());
            
            for (MobEntity mob : nearbyMobs) {
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0, false, false));
            }
        }

        // EYE_DEMON: постоянный GLOWING на врагов
        if (data.hasDemonEyeDemon()) {
            Box searchBox = new Box(player.getBlockPos()).expand(16.0);
            List<MobEntity> nearbyMobs = world.getEntitiesByClass(MobEntity.class, searchBox, e -> 
                !e.isAllied(player) && e.isAlive());
            
            for (MobEntity mob : nearbyMobs) {
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0, false, false));
            }
        }

        // EYE_VOID: иммунитет к слепоте и темноте
        if (data.hasDemonEyeVoid()) {
            player.removeStatusEffect(StatusEffects.BLINDNESS);
            player.removeStatusEffect(StatusEffects.DARKNESS);
        }
    }

    /**
     * Проверка имеет ли игрок уже выбранный дар
     */
    public static boolean hasSelectedGift(PlayerData data) {
        return data.hasDemonEyeNightVision() || 
               data.hasDemonEyeFarSight() || 
               data.hasDemonEyeDemon() || 
               data.hasDemonEyeFate() || 
               data.hasDemonEyeVoid();
    }
}
