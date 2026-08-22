package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.lighting.ProjectileLightHelper;
import com.redabysslucia.dragonrise_reforge.entities.projectile.GuidedBombEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 制导炸弹（GBU-12 / GB-500 / GBU-24 / JDAM-ER）飞行过程中不发光：
 * superb 的 ProjectileLightHelper 把所有 MissileProjectile 判为"火箭发动机"
 * 并挂上最亮的动态尾迹光（15 级 + 径向光晕），同时投放瞬间还有发射闪光。
 * 炸弹没有火箭发动机，飞行时不应照亮周围——在判定处直接返回 null 即可。
 */
@Mixin(value = ProjectileLightHelper.class, remap = false)
public class ProjectileLightHelperMixin {

    /** 尾迹光：制导炸弹返回 null（不发光） */
    @Inject(method = "getTrailLight", at = @At("HEAD"), cancellable = true)
    private static void dragonrise$noTrailLightForGuidedBombs(
            Entity entity, CallbackInfoReturnable<Object> cir) {
        if (entity instanceof GuidedBombEntity) {
            cir.setReturnValue(null);
        }
    }

    /** 投放闪光：制导炸弹返回 null（无发射闪光） */
    @Inject(method = "getLaunchFlash", at = @At("HEAD"), cancellable = true)
    private static void dragonrise$noLaunchFlashForGuidedBombs(
            Entity entity, CallbackInfoReturnable<Object> cir) {
        if (entity instanceof GuidedBombEntity) {
            cir.setReturnValue(null);
        }
    }
}
