package com.reinkarnicaja.mod.passive;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.rank.Rank;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Обработчик пассивных способностей и проклятий всех персонажей.
 * Подключается через события Fabric API.
 */
public class PassiveHandler {

    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient()) return;
            
            tickCounter++;
            ServerWorld serverWorld = (ServerWorld) world;
            
            // Обрабатываем всех игроков в мире
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                PlayerData data = PlayerData.get(player);
                if (data == null || data.getCharacter() == null) continue;
                
                tickPlayerPassives(player, data, serverWorld);
            }
        });
    }

    /**
     * Тик пассивных способностей для каждого игрока
     */
    private static void tickPlayerPassives(ServerPlayerEntity player, PlayerData data, ServerWorld world) {
        CharacterDefinition character = data.getCharacter();
        
        switch (character) {
            case RUDEUS -> handleRudeusPassives(player, data);
            case ORSTED -> handleOrstedPassives(player, data, world);
            case ERIS -> handleErisPassives(player, data);
            case LAPLACE -> handleLaplacePassives(player, data);
            case KISHIRIKA -> handleKishirikaPassives(player, data, world);
            case CALMAN_III -> handleCalmanPassives(player, data);
            case BADIGADI -> handleBadigadiPassives(player, data);
            case ROXY -> handleRoxyPassives(player, data, world);
        }
    }

    // =========================================================================
    // РУДЕУС - Магия без слов, регенерация при пустой мане, двойной каст
    // =========================================================================
    
    private static void handleRudeusPassives(ServerPlayerEntity player, PlayerData data) {
        // MANA_REGEN_ON_EMPTY: при mana==0 manaTrainingXP *= 1.2
        // Это обрабатывается в ManaManager.checkDepletion(), здесь только логирование
        if (data.getCurrentMana() <= 0 && data.isInDepletion()) {
            // Бонус уже применён в checkDepletion()
        }
        
        // Двойной каст (EMPEROR+) - флаг dualCastActive устанавливается извне
        // Здесь можно добавить автоматическую активацию при определённых условиях
        if (data.hasImperialRank() && data.isDualCastActive()) {
            // Двойной каст обрабатывается в SpellCastC2SPacket
        }
    }

    // =========================================================================
    // ОРСТЕД - Урон при низком HP, запрет оружия, страх NPC, медленная мана
    // =========================================================================
    
    private static void handleOrstedPassives(ServerPlayerEntity player, PlayerData data, ServerWorld world) {
        float hpPercent = player.getHealth() / player.getMaxHealth();
        
        // ЗАПРЕТ ОРУЖИЯ при HP > 50%
        if (hpPercent > 0.5f) {
            var mainHand = player.getMainHandStack();
            if (!mainHand.isEmpty() && mainHand.getItem().toString().contains("sword")) {
                // Запрет атаки обрабатывается в DamageMixin
            }
        }
        
        // СТРАХ NPC - мобам рядом применять эффект страха
        if (tickCounter % 20 == 0) {
            Box searchBox = new Box(player.getBlockPos()).expand(10.0);
            List<MobEntity> nearbyMobs = world.getEntitiesByClass(MobEntity.class, searchBox, e -> true);
            
            for (MobEntity mob : nearbyMobs) {
                // Моб должен убегать от Орстеда
                Vec3d dir = mob.getPos().subtract(player.getPos()).normalize();
                mob.setVelocity(dir.multiply(0.5));
            }
        }
    }

    // =========================================================================
    // ЭРИС - Бонус урона после рывка
    // =========================================================================
    
    private static void handleErisPassives(ServerPlayerEntity player, PlayerData data) {
        // DASH_DAMAGE_BONUS (+40%) уже реализован в DashMechanic
        // Здесь можно добавить дополнительные эффекты если нужно
        
        // Проверка пост-даш бонуса
        if (data.hasPostDashDamageBonus()) {
            // Бонус применяется в DamageMixin
        }
    }

    // =========================================================================
    // ЛАПЛАС - Смена элементов, поглощение магии, дебафф отката
    // =========================================================================
    
    private static void handleLaplacePassives(ServerPlayerEntity player, PlayerData data) {
        // ELEMENT_SWAP: +20% урона при смене стихии
        // lastSpellStyle и elementSwapBonus обновляются в SpellManager.castSpell()
        
        // МАГИЧЕСКОЕ ПОГЛОЩЕНИЕ: пропуск manaTraining при истощении
        // Обрабатывается в ManaManager.checkDepletion()
        
        // Дебафф ОТКАТ (10 сек без заклинаний) уже в ManaManager
    }

    // =========================================================================
    // КИШИРИКА - Глаз Бога, ночное зрение, светящиеся враги
    // =========================================================================
    
    private static void handleKishirikaPassives(ServerPlayerEntity player, PlayerData data, ServerWorld world) {
        // Каждые 200 тиков (10 сек) применять NIGHT_VISION и GLOWING к врагам
        if (tickCounter % 200 == 0) {
            // NIGHT_VISION на 400 тиков (20 сек)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false));
            
            // GLOWING к мобам в радиусе 16 блоков
            Box searchBox = new Box(player.getBlockPos()).expand(16.0);
            List<MobEntity> nearbyMobs = world.getEntitiesByClass(MobEntity.class, searchBox, e -> 
                !e.isAllied(player) && e.isAlive());
            
            for (MobEntity mob : nearbyMobs) {
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 400, 0, false, false));
            }
        }
        
        // DemonEyeGiftHandler обрабатывает выбор даров отдельно
    }

    // =========================================================================
    // КАЛЬМАН III - Награда Dragon Sword при EMPEROR NORTH
    // =========================================================================
    
    private static void handleCalmanPassives(ServerPlayerEntity player, PlayerData data) {
        // Проверка получения меча Каджакуто при достижении EMPEROR NORTH
        // Обрабатывается в RankManager.checkRankUpForStyle()
        
        // Слабость при стоянии >2 сек уже в ManaManager.tickCalmanImmobility()
    }

    // =========================================================================
    // БАДИГАДИ - Сопротивление отбрасыванию, AOE удары, запрет магии
    // =========================================================================
    
    private static void handleBadigadiPassives(ServerPlayerEntity player, PlayerData data) {
        // KNOCKBACK_RESIST обрабатывается в LivingEntity.takeKnockback() mixin
        
        // ЗАПРЕТ МАГИИ выше INTERMEDIATE проверяется в SpellManager.canCastSpell()
        
        // AOE КАЖДОГО УДАРА обрабатывается в DamageMixin
        
        // БРОНЯ БОГА БИТВ: расход маны и запрет снятия
        // Расход: 0.1% -> 1%/сек нарастает за 20 мин
        // Обрабатывается в ManaManager.tickPlayer()
    }

    // =========================================================================
    // РОКСИ - Молчание при попадании, аура маны для союзников
    // =========================================================================
    
    private static void handleRoxyPassives(ServerPlayerEntity player, PlayerData data, ServerWorld world) {
        // SILENCE_ON_HIT: при попадании заклинания - silence на 2 сек
        // Кулдаун 17 сек, обрабатывается в DamageMixin
        
        // MANA_AURA_ALLY: регенерация маны союзникам в радиусе 10 блоков
        if (tickCounter % 20 == 0) {
            Box searchBox = new Box(player.getBlockPos()).expand(10.0);
            List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(ServerPlayerEntity.class, searchBox, e -> 
                e != player && e.isAlive());
            
            for (ServerPlayerEntity ally : nearbyPlayers) {
                PlayerData allyData = PlayerData.get(ally);
                if (allyData != null) {
                    // Регенерация 15% от скорости регенерации союзника
                    float regenAmount = allyData.getManaRegenRate() * 0.15f;
                    allyData.regenMana(regenAmount);
                }
            }
        }
        
        // СИНЕРГИЯ Рокси+Рудеус: +10% урон Рудеусу если Рокси рядом
        // Обрабатывается в DamageMixin
    }

    /**
     * Проверка запрета атаки оружием для Орстеда при HP > 50%
     */
    public static boolean canAttackWithWeapon(ServerPlayerEntity player, PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.ORSTED) {
            return true;
        }
        
        float hpPercent = player.getHealth() / player.getMaxHealth();
        return hpPercent <= 0.5f;
    }

    /**
     * Получить множитель урона для Орстеда при низком HP
     */
    public static float getOrstedDamageMultiplier(ServerPlayerEntity player, PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.ORSTED) {
            return 1.0f;
        }
        
        float hpPercent = player.getHealth() / player.getMaxHealth();
        if (hpPercent < 0.3f) {
            return 1.5f; // +50% урона при HP < 30%
        }
        
        return 1.0f;
    }

    /**
     * Получить множитель урона для Лапласа при смене элемента
     */
    public static float getLaplaceElementSwapMultiplier(PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.LAPLACE) {
            return 1.0f;
        }
        
        if (data.isElementSwapBonus()) {
            return 1.2f; // +20% урона
        }
        
        return 1.0f;
    }

    /**
     * Получить множитель урона после рывка для Эрис
     */
    public static float getErisDashDamageMultiplier(PlayerData data) {
        if (data.getCharacter() != CharacterDefinition.ERIS) {
            return 1.0f;
        }
        
        if (data.hasPostDashDamageBonus()) {
            return 1.4f; // +40% урона
        }
        
        return 1.0f;
    }

    /**
     * Проверка может ли персонаж использовать магию выше определённого ранга
     */
    public static boolean canUseMagicAboveRank(PlayerData data, Rank maxRank) {
        if (data.getCharacter() != CharacterDefinition.BADIGADI) {
            return true;
        }
        
        // Бадигади не может использовать магию выше INTERMEDIATE
        return maxRank.ordinal() <= Rank.INTERMEDIATE.ordinal();
    }

    /**
     * Проверка синергии Рокси+Рудеус
     */
    public static boolean hasRoxySynergy(ServerPlayerEntity attacker, ServerWorld world) {
        if (attacker == null) return false;
        
        PlayerData attackerData = PlayerData.get(attacker);
        if (attackerData == null || attackerData.getCharacter() != CharacterDefinition.RUDEUS) {
            return false;
        }
        
        // Поиск Рокси в радиусе 10 блоков
        Box searchBox = new Box(attacker.getBlockPos()).expand(10.0);
        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(ServerPlayerEntity.class, searchBox, e -> 
            e != attacker && e.isAlive());
        
        for (ServerPlayerEntity player : nearbyPlayers) {
            PlayerData playerData = PlayerData.get(player);
            if (playerData != null && playerData.getCharacter() == CharacterDefinition.ROXY) {
                return true;
            }
        }
        
        return false;
    }
}
