package com.reinkarnicaja.mod.boss;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Данные босса: здоровье, позиция, состояние
 */
public class BossData {
    private final UUID bossId;
    private final BossDefinition definition;
    private float currentHealth;
    private Vec3d spawnPosition;
    private World world;
    private long spawnTime;
    private boolean isAlive;
    private int respawnTicks;

    public BossData(UUID bossId, BossDefinition definition, World world, Vec3d spawnPosition) {
        this.bossId = bossId;
        this.definition = definition;
        this.currentHealth = definition.getMaxHealth();
        this.spawnPosition = spawnPosition;
        this.world = world;
        this.spawnTime = System.currentTimeMillis();
        this.isAlive = true;
        this.respawnTicks = 0;
    }

    public UUID getBossId() {
        return bossId;
    }

    public BossDefinition getDefinition() {
        return definition;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float health) {
        this.currentHealth = Math.max(0, Math.min(health, definition.getMaxHealth()));
        if (currentHealth <= 0) {
            isAlive = false;
        }
    }

    public void damage(float amount) {
        setCurrentHealth(currentHealth - amount);
    }

    public void heal(float amount) {
        setCurrentHealth(currentHealth + amount);
    }

    public Vec3d getSpawnPosition() {
        return spawnPosition;
    }

    public World getWorld() {
        return world;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getRespawnTicks() {
        return respawnTicks;
    }

    public void setRespawnTicks(int ticks) {
        this.respawnTicks = ticks;
    }

    public void tickRespawn() {
        if (respawnTicks > 0) {
            respawnTicks--;
        }
    }

    /**
     * Сохранить данные в NBT
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("BossId", bossId);
        nbt.putString("DefinitionKey", definition.getKey());
        nbt.putFloat("CurrentHealth", currentHealth);
        nbt.putDouble("SpawnX", spawnPosition.x);
        nbt.putDouble("SpawnY", spawnPosition.y);
        nbt.putDouble("SpawnZ", spawnPosition.z);
        nbt.putLong("SpawnTime", spawnTime);
        nbt.putBoolean("IsAlive", isAlive);
        nbt.putInt("RespawnTicks", respawnTicks);
        return nbt;
    }

    /**
     * Загрузить данные из NBT
     */
    public static BossData fromNbt(NbtCompound nbt, World world) {
        UUID bossId = nbt.getUuid("BossId");
        String defKey = nbt.getString("DefinitionKey");
        BossDefinition definition = BossDefinition.fromKey(defKey);
        
        Vec3d spawnPos = new Vec3d(
            nbt.getDouble("SpawnX"),
            nbt.getDouble("SpawnY"),
            nbt.getDouble("SpawnZ")
        );
        
        BossData data = new BossData(bossId, definition, world, spawnPos);
        data.setCurrentHealth(nbt.getFloat("CurrentHealth"));
        data.spawnTime = nbt.getLong("SpawnTime");
        data.isAlive = nbt.getBoolean("IsAlive");
        data.respawnTicks = nbt.getInt("RespawnTicks");
        
        return data;
    }

    /**
     * Получить процент здоровья
     */
    public float getHealthPercent() {
        return currentHealth / definition.getMaxHealth();
    }
}
