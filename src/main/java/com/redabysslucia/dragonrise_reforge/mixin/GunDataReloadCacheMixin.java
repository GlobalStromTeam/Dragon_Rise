package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.gun.GunData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修卓越前线载具武器「装填时装不进、也不扣弹」的上游缺陷（HJ-8 / 9M133 / 官方 TOW 等固定发射器都会中招）。
 * <p>
 * 原因：{@code GunData.countBackupAmmo(Entity)} 的结果带一个 <b>10 tick、且不区分来源实体</b> 的缓存
 * （{@code cachedBackupAmmo} / {@code cachedBackupAmmoTick} / {@code BACKUP_AMMO_CACHE_TICKS = 10}）。
 * 载具侧（HUD 同步 {@code VehicleEntity.updateBackupAmmoCount}、切换弹药 {@code changeAmmoConsumer}）
 * 会带着「载具自身 / null」这样的来源去查询，把缓存写成 0；
 * 玩家随后右键装填时 {@code reloadAmmo(player)} 在同一缓存窗口内读到那个 0，
 * 于是 {@code ammoToAdd = min(需求, 0) = 0} —— 弹匣不进弹、备份弹药也不扣，
 * 但调用方仍然把「已装填」标记置为 true，看起来就是"装上了却没消耗、之后还卡死"。
 * <p>
 * 卓越前线自己在 {@code consumeBackupAmmo} 结尾和 {@code updateBackupAmmoCount} 里都手工清过这个缓存，
 * 唯独 {@code reloadAmmo} 入口漏了。这里在入口补上同款清缓存，语义与上游一致、只是让它重新扫描真实来源。
 * <p>
 * 注意：{@code reloadAmmo} 是卓越前线自己的方法（Kotlin {@code @JvmOverloads} 生成的完整实现），
 * 所以必须 {@code remap = false}。
 */
@Mixin(value = GunData.class, remap = false)
public abstract class GunDataReloadCacheMixin {

    @Inject(method = "reloadAmmo(Lnet/minecraft/world/entity/Entity;Z)V", at = @At("HEAD"))
    private void dragonrise$invalidateBackupAmmoCache(Entity entity, boolean extraOne, CallbackInfo ci) {
        // 让这次装填按传入的来源实体重新清点弹药，而不是复用别的来源留下的缓存
        ((GunData) (Object) this).cachedBackupAmmo = -1;
    }
}
