package com.reinkarnicaja.mod.mixin;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.ui.CharacterSelectScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для ServerPlayerEntity - применение скинов и атрибутов при подключении.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityMixin {

    /**
     * Применение атрибутов персонажа при подключении игрока к серверу.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onPlayerInit(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (player.getWorld().isClient()) return;
        
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() == null) return;
        
        applyCharacterAttributes(player, data);
    }
    
    /**
     * Применение модификаторов атрибутов на основе выбранного персонажа.
     */
    private void applyCharacterAttributes(ServerPlayerEntity player, PlayerData data) {
        CharacterDefinition character = data.getCharacter();
        
        // MAX_HEALTH: character.getBaseHP() / 20 * базовые сердца
        float baseHealth = character.getBaseHP();
        player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH)
            .ifPresent(attr -> {
                attr.setBaseValue(baseHealth);
                player.setHealth(baseHealth);
            });
        
        // ATTACK_DAMAGE: character.getBaseDamage() множитель
        float baseDamage = character.getBaseDamage();
        player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE)
            .ifPresent(attr -> {
                attr.setBaseValue(baseDamage);
            });
        
        // MOVEMENT_SPEED: Орстед +0.03, Рокси -0.01, Бадигади -0.02
        float speedModifier = switch (character) {
            case ORSTED -> 0.03f;
            case ROXY -> -0.01f;
            case BADIGADI -> -0.02f;
            default -> 0.0f;
        };
        
        if (speedModifier != 0.0f) {
            player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .ifPresent(attr -> {
                    attr.setBaseValue(0.1f + speedModifier);
                });
        }
    }
}
