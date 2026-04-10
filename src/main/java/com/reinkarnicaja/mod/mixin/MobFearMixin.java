package com.reinkarnicaja.mod.mixin;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для тика мобов - страх перед Орстедом.
 */
@Mixin(MobEntity.class)
public abstract class MobFearMixin {

    /**
     * Мобы убегают от Орстеда в радиусе 10 блоков.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        
        if (mob.getWorld().isClient()) return;
        
        // Поиск игроков-Орстедов в радиусе 10 блоков
        Box searchBox = new Box(mob.getBlockPos()).expand(10.0);
        var nearbyPlayers = mob.getWorld().getEntitiesByClass(ServerPlayerEntity.class, searchBox, p -> true);
        
        for (ServerPlayerEntity player : nearbyPlayers) {
            PlayerData data = PlayerData.get(player);
            if (data != null && data.getCharacter() == CharacterDefinition.ORSTED) {
                // Применить эффект страха - моб должен убегать
                applyFearEffect(mob, player);
                break;
            }
        }
    }
    
    /**
     * Применить эффект страха к мобу.
     */
    private void applyFearEffect(MobEntity mob, ServerPlayerEntity player) {
        // Вектор от игрока к мобу
        var direction = mob.getPos().subtract(player.getPos()).normalize();
        
        // Установить скорость движения прочь от Орстеда
        mob.setVelocity(direction.multiply(0.5));
        
        // Дополнительно можно установить цель движения
        if (mob.getNavigation() != null) {
            var fleePos = mob.getPos().add(direction.multiply(20));
            mob.getNavigation().startMovingTo(fleePos.getX(), fleePos.getY(), fleePos.getZ(), 1.5);
        }
    }
}
