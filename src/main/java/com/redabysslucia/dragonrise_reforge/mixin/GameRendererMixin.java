package com.redabysslucia.dragonrise_reforge.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F", cancellable = true)
    private static void getNightVisionScale(LivingEntity living, float num, CallbackInfoReturnable<Float> callback) {
        callback.setReturnValue(0f);
    }
}
