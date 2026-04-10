package com.reinkarnicaja.mod.mixin;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для обработки отбрасывания - сопротивление Бадигади.
 */
@Mixin(LivingEntity.class)
public abstract class KnockbackMixin {

    /**
     * Сопротивление отбрасыванию для Бадигади (множитель 0.0).
     */
    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void onTakeKnockback(double velocityX, double velocityZ, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (entity.getWorld().isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() != CharacterDefinition.BADIGADI) return;
        
        // Бадигади полностью игнорирует отбрасывание
        ci.cancel();
    }
}
