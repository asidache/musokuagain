package com.reinkarnicaja.mod.mixin;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.PlayerData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin для инвентаря игрока - запрет снятия Брони Бога Битв.
 */
@Mixin(PlayerInventory.class)
public abstract class InventoryMixin {

    /**
     * Запрет снятия Брони Бога Битв без смерти от истощения.
     */
    @Inject(method = "setStack", at = @At("HEAD"), cancellable = true)
    private void onSetStack(int slot, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        
        if (!(inventory.player instanceof ServerPlayerEntity player)) return;
        if (player.getWorld().isClient()) return;
        
        PlayerData data = PlayerData.get(player);
        if (data == null || data.getCharacter() != CharacterDefinition.BADIGADI) return;
        
        // Проверка слота брони
        if (slot >= 36 && slot <= 38) { // Слоты брони (ноги, тело, голова)
            ItemStack currentStack = inventory.getStack(slot);
            
            // Если текущий предмет - Броня Бога Битв
            if (!currentStack.isEmpty() && 
                currentStack.getItem().toString().contains("battle_god_armor")) {
                
                // Проверка можно ли снять (смерть от истощения)
                if (!data.isBattleArmorCanRemove()) {
                    // Запретить снятие
                    cir.setReturnValue(currentStack);
                    cir.cancel();
                }
            }
        }
    }
}
