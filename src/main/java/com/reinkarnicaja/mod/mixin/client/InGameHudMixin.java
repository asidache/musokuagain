package com.reinkarnicaja.mod.mixin.client;

import com.reinkarnicaja.mod.client.ManaHudRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Клиентский mixin для рендеринга HUD маны.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * Рендеринг HUD маны после основного интерфейса.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        ManaHudRenderer.renderManaHUD(context);
    }
}
