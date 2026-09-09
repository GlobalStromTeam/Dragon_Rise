package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.entities.special.StarShellEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

/**
 * 照明弹大范围补光核心：在 dynamiclights-reforged 计算「某格受某动态光源照亮的等级」时
 * 整段接管（HEAD + cancellable），对本 mod 照明弹实体返回 72 格全向平滑衰减亮度。
 *
 * 机制同 HandheldMoon 的 DirectionalLightMixin（拦截 SodiumDynamicLights.maxDynamicLightLevel），
 * 但盈月是「玩家手持锥形光 + 每 tick 强制重建区块」；照明弹是全向且悬停不动，
 * 光值在区块烘焙后保持静止，只有移动/消失时（配合 StarShellLightManager）才触发重排。
 *
 * 衰减曲线 1-t³（t=距离/半径）：前 70% 距离几乎不掉光，贴近边缘才骤降 —— 大范围照亮的观感。
 * target 是第三方 mod 类，必须 remap=false。
 */
@Mixin(value = SodiumDynamicLights.class, remap = false)
public abstract class DynamicLightsLevelMixin {

    @Inject(method = "maxDynamicLightLevel", at = @At("HEAD"), cancellable = true)
    private static void dragonrise_starShellLight(BlockPos pos, DynamicLightSource lightSource,
                                                 double currentLightLevel, CallbackInfoReturnable<Double> cir) {
        if (!(lightSource instanceof StarShellEntity)) {
            return;
        }
        // sdl$* 是 DynamicLightSource 接口方法（框架运行时注入到实体上），须用接口引用调用
        double R = StarShellEntity.LIGHT_RADIUS;
        double dx = pos.getX() + 0.5 - lightSource.sdl$getDynamicLightX();
        double dy = pos.getY() + 0.5 - lightSource.sdl$getDynamicLightY();
        double dz = pos.getZ() + 0.5 - lightSource.sdl$getDynamicLightZ();
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq > R * R) {
            return; // 超出半径：交给框架默认（无贡献）
        }
        double t = Math.sqrt(distSq) / R;
        double attenuation = 1.0 - t * t * t;
        double lightLevel = 15.0 * attenuation;
        if (lightLevel > currentLightLevel) {
            cir.setReturnValue(Math.max(lightLevel, currentLightLevel));
        }
    }
}
