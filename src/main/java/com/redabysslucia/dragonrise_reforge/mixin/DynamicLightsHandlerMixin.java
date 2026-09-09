package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.entities.special.StarShellEntity;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 dynamiclights-reforged（LambDynamicLights API 层）把照明弹实体当作「亮度 15 的动态光源」。
 * 机制同 HandheldMoon 的 DynamicLightHandlersMixin：canLightUp 放行 + luminance 固定 15。
 * target 是第三方 API 类，必须 remap=false。
 */
@Mixin(value = DynamicLightHandlers.class, remap = false)
public class DynamicLightsHandlerMixin {

    @Inject(method = "canLightUp(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private static void dragonrise_canLightUp(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof StarShellEntity) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getLuminanceFrom(Lnet/minecraft/world/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    private static void dragonrise_getLuminance(Entity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof StarShellEntity) {
            cir.setReturnValue(15);
        }
    }
}
