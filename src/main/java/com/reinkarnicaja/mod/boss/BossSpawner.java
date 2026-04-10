package com.reinkarnicaja.mod.boss;

import com.reinkarnicaja.mod.ReincarnationMod;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.rank.Rank;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

/**
 * Менеджер спавна боссов: биомный спавн, таймеры респавна, создание сущностей
 */
public class BossSpawner {

    private static final Map<UUID, BossData> ACTIVE_BOSSES = new HashMap<>();
    private static final Map<String, Long> BOSS_SPAWN_TIMERS = new HashMap<>();
    
    // Время респавна боссов в тиках (20 тиков = 1 секунда)
    private static final int DEFAULT_RESPAWN_TIME = 72000; // 1 час (3600 секунд)
    
    // Минимальный ранг игрока для спавна каждого босса
    private static final Map<BossDefinition, Rank> MIN_PLAYER_RANK_FOR_SPAWN = new EnumMap<>(BossDefinition.class);
    
    static {
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.GOLDEN_MAGE, Rank.BEGINNER);
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.MAGIC_KNIGHT, Rank.INTERMEDIATE);
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.EARTH_SERPENT, Rank.INTERMEDIATE);
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.HYDRA, Rank.ADVANCED);
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.HOLY_SWORD_DEMON, Rank.SAINT);
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.ANCIENT_DRAGON, Rank.KING);
        MIN_PLAYER_RANK_FOR_SPAWN.put(BossDefinition.DEMON_LORD_LAPLACE, Rank.EMPEROR);
    }

    public static void register() {
        // Тик мира для проверки спавна боссов
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.isClient()) return;
            
            ServerWorld serverWorld = (ServerWorld) world;
            tickBossSpawns(serverWorld);
            tickActiveBosses(serverWorld);
        });
    }

    /**
     * Тик проверки спавна боссов
     */
    private static void tickBossSpawns(ServerWorld world) {
        long gameTime = world.getTime();
        
        // Проверка каждые 100 тиков (5 секунд)
        if (gameTime % 100 != 0) {
            return;
        }
        
        // Получить всех игроков в мире
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }
        
        // Проверить шанс спавна босса
        if (world.getRandom().nextFloat() < 0.01f) { // 1% шанс каждые 5 секунд
            trySpawnBoss(world, players);
        }
    }

    /**
     * Попытка спавна босса
     */
    private static void trySpawnBoss(ServerWorld world, List<ServerPlayerEntity> players) {
        // Выбрать случайного игрока
        ServerPlayerEntity targetPlayer = players.get(world.getRandom().nextInt(players.size()));
        PlayerData playerData = PlayerData.get(targetPlayer);
        
        if (playerData == null || playerData.getCharacter() == null) {
            return;
        }
        
        Rank playerRank = playerData.getEffectiveRank(playerData.getActiveStyle());
        
        // Найти подходящего босса по рангу
        List<BossDefinition> eligibleBosses = new ArrayList<>();
        for (BossDefinition boss : BossDefinition.values()) {
            Rank minRank = MIN_PLAYER_RANK_FOR_SPAWN.get(boss);
            if (minRank != null && playerRank.getLevel() >= minRank.getLevel()) {
                // Проверить не активен ли уже этот босс
                boolean isAlive = ACTIVE_BOSSES.values().stream()
                    .anyMatch(data -> data.getDefinition() == boss && data.isAlive());
                
                if (!isAlive) {
                    eligibleBosses.add(boss);
                }
            }
        }
        
        if (eligibleBosses.isEmpty()) {
            return;
        }
        
        // Выбрать случайного босса
        BossDefinition bossDef = eligibleBosses.get(world.getRandom().nextInt(eligibleBosses.size()));
        
        // Найти позицию для спавна (вдали от игрока)
        Vec3d spawnPos = findSpawnPosition(world, targetPlayer, 50, 100);
        if (spawnPos == null) {
            return;
        }
        
        // Заспавнить босса
        createBossEntity(world, bossDef, spawnPos);
        
        // Объявить о спавне
        announceBossSpawn(world, bossDef, spawnPos);
    }

    /**
     * Найти позицию для спавна босса
     */
    private static Vec3d findSpawnPosition(ServerWorld world, ServerPlayerEntity player, 
                                           double minDistance, double maxDistance) {
        Random random = world.getRandom();
        int attempts = 10;
        
        for (int i = 0; i < attempts; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            double y = world.getTopY((int) x, (int) z);
            
            Vec3d pos = new Vec3d(x, y, z);
            
            // Проверить что позиция валидна
            if (isValidSpawnPosition(world, pos)) {
                return pos;
            }
        }
        
        return null;
    }

    /**
     * Проверить валидность позиции для спавна
     */
    private static boolean isValidSpawnPosition(ServerWorld world, Vec3d pos) {
        // Проверить что блок под позицией твердый
        int blockX = (int) pos.getX();
        int blockY = (int) pos.getY() - 1;
        int blockZ = (int) pos.getZ();
        
        var blockState = world.getBlockState(net.minecraft.util.math.BlockPos.of(blockX, blockY, blockZ));
        return blockState.isSolidBlock(world, net.minecraft.util.math.BlockPos.of(blockX, blockY, blockZ));
    }

    /**
     * Создать сущность босса
     */
    @SuppressWarnings("unchecked")
    public static Entity createBossEntity(ServerWorld world, BossDefinition bossDef, Vec3d spawnPos) {
        EntityType<?> entityType = bossDef.getEntityType();
        
        Entity entity = entityType.create(world);
        if (entity == null) {
            return null;
        }
        
        entity.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, 
                                        world.getRandom().nextFloat() * 360, 0);
        
        if (entity instanceof LivingEntity livingEntity) {
            // Установить здоровье
            livingEntity.setHealth(bossDef.getMaxHealth());
            livingEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                .addTemporaryModifier(new EntityAttributeModifier(
                    Identifier.of("reinkarnicaja_mod", "boss_max_health"),
                    bossDef.getMaxHealth() - 20, // Базовое HP моба обычно 20
                    EntityAttributeModifier.Operation.ADD_VALUE
                ));
            
            // Установить урон
            livingEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                .addTemporaryModifier(new EntityAttributeModifier(
                    Identifier.of("reinkarnicaja_mod", "boss_attack_damage"),
                    bossDef.getAttackDamage(),
                    EntityAttributeModifier.Operation.ADD_VALUE
                ));
            
            // Установить кастомное имя
            entity.setCustomName(Text.translatable("boss.reinkarnicaja_mod." + bossDef.getKey())
                .formatted(Formatting.RED, Formatting.BOLD));
            entity.setCustomNameVisible(true);
        }
        
        world.spawnEntity(entity);
        
        // Сохранить данные о боссе
        UUID bossId = entity.getUuid();
        BossData bossData = new BossData(bossId, bossDef, world, spawnPos);
        ACTIVE_BOSSES.put(bossId, bossData);
        
        ReincarnationMod.LOGGER.info("Boss {} spawned at {}", bossDef.getKey(), spawnPos);
        
        return entity;
    }

    /**
     * Объявить о спавне босса всем игрокам
     */
    public static void announceBossSpawn(ServerWorld world, BossDefinition bossDef, Vec3d spawnPos) {
        Text message = Text.translatable("boss.reinkarnicaja_mod.spawned", 
                                         Text.translatable("boss.reinkarnicaja_mod." + bossDef.getKey()))
            .formatted(Formatting.RED, Formatting.BOLD);
        
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(message, true);
        }
    }

    /**
     * Тик активных боссов (регенерация здоровья, проверка состояния)
     */
    private static void tickActiveBosses(ServerWorld world) {
        Iterator<Map.Entry<UUID, BossData>> iterator = ACTIVE_BOSSES.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, BossData> entry = iterator.next();
            BossData bossData = entry.getValue();
            
            if (!bossData.isAlive()) {
                bossData.tickRespawn();
                if (bossData.getRespawnTicks() <= 0) {
                    // Респавн босса
                    iterator.remove();
                }
                continue;
            }
            
            // Регенерация здоровья
            float regenAmount = bossData.getDefinition().getHealthRegenPerSec() / 20f;
            bossData.heal(regenAmount);
            
            // Проверить жив ли еще босс в мире
            Entity bossEntity = world.getEntity(bossData.getBossId());
            if (bossEntity == null || !bossEntity.isAlive()) {
                bossData.setAlive(false);
                bossData.setRespawnTicks(DEFAULT_RESPAWN_TIME);
                
                // Наградить игроков
                onBossKilled(world, bossData);
            }
        }
    }

    /**
     * Обработка убийства босса
     */
    public static void onBossKilled(ServerWorld world, BossData bossData) {
        BossDefinition bossDef = bossData.getDefinition();
        
        Text message = Text.translatable("boss.reinkarnicaja_mod.killed",
                                         Text.translatable("boss.reinkarnicaja_mod." + bossDef.getKey()))
            .formatted(Formatting.GOLD, Formatting.BOLD);
        
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(message, true);
            
            // Наградить игроков рядом с боссом
            double distance = player.getPos().distanceTo(bossData.getSpawnPosition());
            if (distance <= 50) {
                // Выдать опыт и предметы (будет реализовано в BossDropManager)
                player.sendMessage(Text.translatable("boss.reinkarnicaja_mod.reward")
                    .formatted(Formatting.GREEN), true);
            }
        }
        
        ReincarnationMod.LOGGER.info("Boss {} killed by players", bossDef.getKey());
    }

    /**
     * Найти определение босса по сущности
     */
    public static BossDefinition findBossDefinition(Entity entity) {
        if (entity == null) {
            return null;
        }
        
        BossData data = ACTIVE_BOSSES.get(entity.getUuid());
        if (data != null) {
            return data.getDefinition();
        }
        
        // Проверить по имени
        if (entity.hasCustomName()) {
            String customName = entity.getCustomName().getString();
            for (BossDefinition def : BossDefinition.values()) {
                String bossNameKey = "boss.reinkarnicaja_mod." + def.getKey();
                // Сравнение будет неточным, но лучше чем ничего
                if (customName.contains(def.getKey())) {
                    return def;
                }
            }
        }
        
        return null;
    }

    /**
     * Проверить активен ли босс
     */
    public static boolean isBossAlive(BossDefinition bossDef) {
        return ACTIVE_BOSSES.values().stream()
            .anyMatch(data -> data.getDefinition() == bossDef && data.isAlive());
    }

    /**
     * Получить данные активного босса
     */
    public static BossData getBossData(UUID bossId) {
        return ACTIVE_BOSSES.get(bossId);
    }

    /**
     * Получить всех активных боссов
     */
    public static Collection<BossData> getActiveBosses() {
        return Collections.unmodifiableCollection(ACTIVE_BOSSES.values());
    }

    /**
     * Очистить все данные о боссах (при перезагрузке мира)
     */
    public static void clearAll() {
        ACTIVE_BOSSES.clear();
        BOSS_SPAWN_TIMERS.clear();
    }
}
