package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.VehicleMainWeaponHudOverlay;
import com.atsuishio.superbwarfare.data.gun.SeekWeaponInfo;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 双模式制导武器（OnlyLockEntity + OnlyLockBlock，如 JDAM-ER / AGM-65）的锁定 UI：
 * superb 的实体锁定框（白框 + 锁定动画）在 onlyLockBlock 为真时被抑制
 * （render 内 `!seekInfo.onlyLockBlock` 条件），且分支结构为
 * `if (onlyLockEntity && entities != null) {实体UI} else if (onlyLockBlock) {地面UI}`——
 * 双模式武器 onlyLockEntity 恒真，地面 UI 分支（含锁定动画）永远走不到。
 *
 * 本 Mixin 两个重定向：
 * 1. getOnlyLockEntity（line 204 分支判定）：双模式武器无实体在搜索范围内时返回 false，
 *    让渲染走地面锁定 UI 分支（锁定动画 IND_1-4 / FRAME 正常显示）；
 * 2. getOnlyLockBlock（ordinal=0，实体框判定处）：双模式武器返回 false，
 *    让实体白框 + 锁定动画照常渲染。
 * 单模式武器行为完全不变。
 */
@Mixin(value = VehicleMainWeaponHudOverlay.class, remap = false)
public class VehicleMainWeaponHudOverlayDualSeekMixin {

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lcom/atsuishio/superbwarfare/data/gun/SeekWeaponInfo;getOnlyLockEntity()Z")
    )
    private boolean dragonrise$dualSeekGroundUi(SeekWeaponInfo info) {
        if (info.getOnlyLockEntity() && info.getOnlyLockBlock()) {
            // 双模式：搜索范围内无实体 → 走地面锁定 UI 分支（含锁定动画）
            return ClientEventHandler.nearestEntityVehicle != null;
        }
        return info.getOnlyLockEntity();
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lcom/atsuishio/superbwarfare/data/gun/SeekWeaponInfo;getOnlyLockBlock()Z")
    )
    private boolean dragonrise$showEntityFramesForDualSeek(SeekWeaponInfo info) {
        if (info.getOnlyLockEntity() && info.getOnlyLockBlock()) {
            return false;
        }
        return info.getOnlyLockBlock();
    }
}
