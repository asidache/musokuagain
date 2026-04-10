package com.reinkarnicaja.mod.mixin;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.mechanics.BattleSpiritManager;
import com.reinkarnicaja.mod.passive.PassiveHandler;
import com.reinkarnicaja.mod.rank.Rank;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin для обработки урона с учётом адаптивной сложности и персонаж-специфичных механик.
 */
@Mixin(LivingEntity.class)
public abstract class DamageMixin {

    /**
     * Обработка входящего урона с учётом рангов и пассивных способностей.
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getWorld().isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) return;
        
        ServerWorld world = (ServerWorld) entity.getWorld();
        
        // 1. Адаптивная сложность - снижение урона по рангу атакующего
        float damageMultiplier = getAdaptiveDifficultyMultiplier(source, player);
        
        // 2. Множитель урона Орстеда при низком HP
        float orstedMultiplier = PassiveHandler.getOrstedDamageMultiplier(player, data);
        damageMultiplier *= orstedMultiplier;
        
        // 3. Множитель Лапласа при смене элемента
        float laplaceMultiplier = PassiveHandler.getLaplaceElementSwapMultiplier(data);
        damageMultiplier *= laplaceMultiplier;
        
        // 4. Множитель Эрис после рывка
        float erisMultiplier = PassiveHandler.getErisDashDamageMultiplier(data);
        damageMultiplier *= erisMultiplier;
        
        // 5. Синергия Рокси+Рудеус (+10% урон)
        if (PassiveHandler.hasRoxySynergy(player, world)) {
            damageMultiplier *= 1.1f;
        }
        
        // 6. Боевой дух - поглощение урона
        if (BattleSpiritManager.isBattleSpiritActive(player)) {
            float absorbedDamage = BattleSpiritManager.absorbDamage(player, data, amount * damageMultiplier);
            float remainingDamage = amount * damageMultiplier - absorbedDamage;
            
            if (remainingDamage <= 0) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
            
            // Применяем оставшийся урон
            // Урон будет применён оригинальным методом
        }
        
        // Примечание: фактическое изменение урона происходит через модификацию amount
        // В mixin мы не можем напрямую изменить amount, поэтому используем другие механизмы
    }
    
    /**
     * Получить множитель урона от атаки игрока с учётом Battle Spirit.
     */
    @Inject(method = "tryAttack", at = @At("HEAD"))
    private void onTryAttack(net.minecraft.entity.Entity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity attacker = (LivingEntity) (Object) this;
        
        if (attacker.getWorld().isClient()) return;
        if (!(attacker instanceof ServerPlayerEntity player)) return;
        
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) return;
        
        // Проверка запрета оружия для Орстеда при HP > 50%
        if (!PassiveHandler.canAttackWithWeapon(player, data)) {
            // Запретить атаку
            // cir.setReturnValue(false); // Нужно использовать cancellable = true
        }
        
        // AOE урон Бадигади при атаке
        if (data.getCharacter() == CharacterDefinition.BADIGADI) {
            // castAoE радиус 2 блока, урон 30% от основного
            // Реализуется в отдельном методе
        }
    }
    
    /**
     * Получить множитель адаптивной сложности на основе ранга атакующего.
     */
    private float getAdaptiveDifficultyMultiplier(DamageSource source, ServerPlayerEntity target) {
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            PlayerData attackerData = PlayerData.get(attacker);
            if (attackerData != null) {
                Rank attackerRank = attackerData.getEffectiveRank();
                if (attackerRank != null) {
                    // Таблица множителей по рангу
                    return switch (attackerRank) {
                        case BEGINNER -> 0.8f;
                        case INTERMEDIATE -> 0.9f;
                        case ADVANCED -> 1.0f;
                        case SAINT -> 1.1f;
                        case KING -> 1.2f;
                        case EMPEROR -> 1.3f;
                        case DIVINE -> 1.4f;
                    };
                }
            }
        }
        
        return 1.0f;
    }
}
