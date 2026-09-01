package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.gun.SeekWeaponInfo;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 双模式制导武器（OnlyLockEntity + OnlyLockBlock 同时开启，如 JDAM-ER）的锁定输入：
 * superb 的 seek 输入是严格 if/else——onlyLockBlock 为真时只走地面锁定分支，
 * 实体锁定完全失效。本 Mixin 重定向分支判定：
 * 搜索范围内有实体（nearestEntityVehicle 非空）→ 走实体锁定分支；
 * 无实体 → 走地面锁定分支（保持 onlyLockBlock 行为）。
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEventHandlerDualSeekMixin {

    @Redirect(
            method = "vehicleWeaponSeeking",
            at = @At(value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/data/gun/SeekWeaponInfo;getOnlyLockBlock()Z")
    )
    private boolean dragonrise$dualModeSeek(SeekWeaponInfo info) {
        if (info.getOnlyLockEntity() && info.getOnlyLockBlock()) {
            // 有实体在搜索范围内 → 实体锁定分支（返回 false 走 else-if onlyLockEntity）
            // 无实体 → 地面锁定分支
            return ClientEventHandler.nearestEntityVehicle == null;
        }
        return info.getOnlyLockBlock();
    }
}
