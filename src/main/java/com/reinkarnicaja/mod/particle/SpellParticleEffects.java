package com.reinkarnicaja.mod.particle;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.data.PlayerData;

/**
 * Визуальные эффекты частиц для заклинаний, пассивок и боевого духа.
 */
public class SpellParticleEffects {

    /**
     * Цвета частиц по стилям боя (RGB).
     */
    public static int getStyleColor(CombatStyle style) {
        return switch (style) {
            case MAGIC -> 0x0066FF;      // Синий
            case NORTH -> 0xFFFFFF;      // Белый
            case SWORD -> 0xFF0000;      // Красный
            case WATER -> 0x00CCFF;      // Голубой
        };
    }

    /**
     * Эффект Боевого Духа: кольцо частиц вокруг игрока.
     */
    public static void spawnBattleSpiritParticles(ServerWorld world, PlayerData data, double x, double y, double z) {
        if (data.getActiveStyle() == null) return;
        
        int color = getStyleColor(data.getActiveStyle());
        double r = (color >> 16 & 0xFF) / 255.0;
        double g = (color >> 8 & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;

        // Кольцо из 16 частиц
        for (int i = 0; i < 16; i++) {
            double angle = (i * Math.PI * 2) / 16;
            double px = x + Math.cos(angle) * 0.8;
            double pz = z + Math.sin(angle) * 0.8;
            
            world.spawnParticles(
                    ParticleTypes.DUST,
                    px, y + 0.5, pz,
                    1,
                    0.0, 0.0, 0.0,
                    0.0,
                    new net.minecraft.particle.DustParticleTransition(
                            new net.minecraft.util.math.Color((int)(r * 255), (int)(g * 255), (int)(b * 255)),
                            1.0f
                    )
            );
        }
    }

    /**
     * Mana Aura Рокси: пульсирующие голубые частицы вокруг союзников.
     */
    public static void spawnRoxieAuraParticles(ServerWorld world, double x, double y, double z, int count) {
        for (int i = 0; i < count; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = world.random.nextDouble() * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;
            
            world.spawnParticles(
                    ParticleTypes.ENCHANT,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    /**
     * Stone Cannon: частицы снаряда.
     */
    public static void spawnStoneCannonTrail(ServerWorld world, double x, double y, double z, Vec3d velocity) {
        world.spawnParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                3,
                velocity.x * 0.1, velocity.y * 0.1, velocity.z * 0.1,
                0.0
        );
    }

    /**
     * Электрическое заклинание: молнии.
     */
    public static void spawnElectricParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
                ParticleTypes.END_ROD,
                x, y, z,
                10,
                0.3, 0.3, 0.3,
                0.0
        );
    }

    /**
     * Ледяное заклинание: заморозка.
     */
    public static void spawnIceParticles(ServerWorld world, double x, double y, double z, double radius) {
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * radius * 2;
            double offsetY = world.random.nextDouble() * radius;
            double offsetZ = (world.random.nextDouble() - 0.5) * radius * 2;
            
            world.spawnParticles(
                    ParticleTypes.SNOWBALL,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    /**
     * Огненное заклинание: пламя.
     */
    public static void spawnFireParticles(ServerWorld world, double x, double y, double z, double radius) {
        for (int i = 0; i < 25; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * radius * 2;
            double offsetY = world.random.nextDouble() * radius;
            double offsetZ = (world.random.nextDouble() - 0.5) * radius * 2;
            
            world.spawnParticles(
                    ParticleTypes.FLAME,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0.0, 0.1, 0.0,
                    0.0
            );
        }
    }

    /**
     * Водное заклинание: брызги.
     */
    public static void spawnWaterParticles(ServerWorld world, double x, double y, double z, double radius) {
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * radius * 2;
            double offsetY = world.random.nextDouble() * radius;
            double offsetZ = (world.random.nextDouble() - 0.5) * radius * 2;
            
            world.spawnParticles(
                    ParticleTypes.SPLASH,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }
    }

    /**
     * Частицы при получении урона (красные искры).
     */
    public static void spawnDamageParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
                ParticleTypes.CRIT,
                x, y + 1.0, z,
                5,
                0.2, 0.2, 0.2,
                0.0
        );
    }

    /**
     * Частицы исцеления (зелёные искры).
     */
    public static void spawnHealParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER,
                x, y + 1.0, z,
                10,
                0.3, 0.3, 0.3,
                0.0
        );
    }
}
