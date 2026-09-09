package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.entities.special.StarShellEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

import java.lang.reflect.Field;

/**
 * 照明弹动态光源的「强接管」兜底（机制同 HandheldMoon 的 SodiumDynamicLightsMixin）。
 *
 * 背景：实体光源的 luminance 字段每 tick 由 reforged 的 EntityMixin#sdl$dynamicLightTick
 * 按 handler 表重算，随后 onTick 内还可能按 canLightUp 清零 —— 实测照明弹 luminance 恒为 0、
 * enabled 恒 false，导致永不进入光源集合、永不触发光照烘焙。
 *
 * 此处仿 HandheldMoon：注入 SodiumDynamicLights.updateTracking（每个实体每 tick 必经），
 * 对本 mod 照明弹实体：用反射把 mixin 注入到 Entity 上的 sodiumdynamiclights$luminance 字段
 * 强制写 15，并确保它已加入 dynamicLightSources 集合。
 * target 是第三方 mod 类，必须 remap=false。
 */
@Mixin(value = SodiumDynamicLights.class, remap = false)
public abstract class DynamicLightsUpdateMixin {

    @Inject(method = "updateTracking", at = @At("HEAD"))
    private static void dragonrise_forceStarShellLight(DynamicLightSource lightSource, CallbackInfo ci) {
        if (!(lightSource instanceof StarShellEntity)) {
            return;
        }
        Entity entity = (Entity) lightSource;
        try {
            Field field = Entity.class.getDeclaredField("sodiumdynamiclights$luminance");
            field.setAccessible(true);
            field.setInt(entity, 15);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 字段不存在说明 reforged mixin 未注入，静默即可（本身已无效果）
        }
        SodiumDynamicLights dl = SodiumDynamicLights.get();
        if (dl != null && !dl.containsLightSource(lightSource)) {
            dl.addLightSource(lightSource);
        }
    }
}
