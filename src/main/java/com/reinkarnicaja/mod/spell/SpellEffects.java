package com.reinkarnicaja.mod.spell;

import com.reinkarnicaja.mod.character.CombatStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;

/**
 * Эффекты заклинаний: снаряды, AoE, ближний бой, баффы/дебаффы.
 */
public class SpellEffects {

    /**
     * Выполнить эффект заклинания
     */
    public static boolean castSpell(ServerPlayerEntity player, ServerWorld world, SpellData spell, float chargeSeconds) {
        switch (spell.id()) {
            case "stone_cannon" -> castStoneCannon(player, world, spell.damage());
            case "dragon_fang" -> castDragonFang(player, world, spell.damage());
            case "god_speed_thrust" -> castPiercingThrust(player, world, spell.damage());
            case "fireball_aoe", "sloshing_water", "water_dragon", "water_tyrant", 
                 "meteor", "north_wind", "ripple", "chaos_magic", "nuclear_explosion" 
                -> castAoE(player, world, spell.damage(), 5f);
            case "quick_strike", "sword_flash", "shadowless", "dark_cutter", 
                 "sword_god_flash", "hidden_blade", "beast_claw", "whirlwind_blade"
                -> castMeleeStrike(player, world, spell.damage());
            case "water_ball", "fire_ball", "firebolt", "water_bolt", "ice_lance",
                 "small_wind", "fang_of_god", "water_slash", "water_flow", "water_slit",
                 "snake_fang", "lightning", "electric", "stone_cannon_emp"
                -> castProjectile(player, world, spell.damage(), spell.isAOE());
            case "lightning_speed" -> applyLightningSpeed(player, world);
            case "water_mirror" -> applyWaterMirror(player, world);
            default -> {
                // Заклинание не реализовано
                return false;
            }
        }
        return true;
    }

    /**
     * Stone Cannon - спиральный снаряд
     */
    public static void castStoneCannon(ServerPlayerEntity player, ServerWorld world, float damage) {
        Vec3d pos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d rotation = player.getRotationVecClient();
        Vec3d velocity = rotation.multiply(2.0);

        // Запуск снаряда с текстурой stone_cannon_projectile.png
        spawnProjectile(player, world, pos, velocity, damage, ParticleTypes.SMOKE);
    }

    /**
     * Dragon Fang - спиральный слэш
     */
    public static void castDragonFang(ServerPlayerEntity player, ServerWorld world, float damage) {
        Vec3d pos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d rotation = player.getRotationVecClient();
        Vec3d velocity = rotation.multiply(2.5);

        spawnProjectile(player, world, pos, velocity, damage, ParticleTypes.EXPLOSION);
    }

    /**
     * God Speed Thrust - пронзающий удар
     */
    public static void castPiercingThrust(ServerPlayerEntity player, ServerWorld world, float damage) {
        Vec3d start = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d direction = player.getRotationVecClient().multiply(8.0);
        Vec3d end = start.add(direction);

        // Raycast для поиска целей
        HitResult hit = world.raycast(new RaycastContext(
            start, end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));

        if (hit instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity target) {
                target.damage(world.getDamageSources().playerAttack(player), damage);
            }
        }

        // Частицы
        world.spawnParticles(ParticleTypes.CRIT, start.x, start.y, start.z, 20, 0.2, 0.2, 0.2, 0.1);
    }

    /**
     * AoE заклинание - урон по области
     */
    public static void castAoE(ServerPlayerEntity player, ServerWorld world, float damage, float radius) {
        Box area = new Box(player.getBlockPos()).expand(radius);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, area, e -> 
            e != player && !e.isAllied(player) && e.isAlive());

        for (LivingEntity entity : entities) {
            entity.damage(world.getDamageSources().playerAttack(player), damage);
        }

        // Частицы взрыва
        world.spawnParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 
            50, radius/2, radius/2, radius/2, 0.2);
    }

    /**
     * Ближний удар
     */
    public static void castMeleeStrike(ServerPlayerEntity player, ServerWorld world, float damage) {
        // Удар по цели в руках
        var target = player.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            livingTarget.damage(world.getDamageSources().playerAttack(player), damage);
        }
    }

    /**
     * Снаряд
     */
    public static void castProjectile(ServerPlayerEntity player, ServerWorld world, float damage, boolean isAOE) {
        Vec3d pos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d rotation = player.getRotationVecClient();
        Vec3d velocity = rotation.multiply(2.0);

        ParticleTypes particleType = isAOE ? ParticleTypes.FLAME : ParticleTypes.SMOKE;
        spawnProjectile(player, world, pos, velocity, damage, particleType);
    }

    /**
     * Lightning Speed - бафф скорости для SWORD стиля
     */
    public static void applyLightningSpeed(ServerPlayerEntity player, ServerWorld world) {
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SPEED, 100, 2, false, false));
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.HASTE, 100, 1, false, false));

        // Частицы молнии
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY(), player.getZ(),
            30, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * Water Mirror - отражение атак
     */
    public static void applyWaterMirror(ServerPlayerEntity player, ServerWorld world) {
        // Временный бафф отражения (реализуется через StatusEffect или атрибут)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.RESISTANCE, 60, 1, false, false));

        // Голубые частицы вокруг игрока
        world.spawnParticles(ParticleTypes.DROPLET, player.getX(), player.getY(), player.getZ(),
            40, 0.8, 0.8, 0.8, 0.05);
    }

    /**
     * Создать снаряд
     */
    private static void spawnProjectile(ServerPlayerEntity player, ServerWorld world, 
                                         Vec3d pos, Vec3d velocity, float damage, 
                                         net.minecraft.particle.ParticleType<?> particle) {
        // Простая реализация через raycast
        Vec3d end = pos.add(velocity.multiply(20)); // Дальность 20 блоков

        HitResult hit = world.raycast(new RaycastContext(
            pos, end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        ));

        if (hit instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity target) {
                target.damage(world.getDamageSources().playerAttack(player), damage);
            }
        }

        // Частицы полёта
        world.spawnParticles(particle, pos.x, pos.y, pos.z, 10, 
            velocity.x * 0.1, velocity.y * 0.1, velocity.z * 0.1, 0.1);
    }

    /**
     * Применить замедление от ICE_LANCE
     */
    public static void applySlower(LivingEntity target, int duration, int amplifier) {
        target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SLOWNESS, duration, amplifier, false, false));
    }
}
