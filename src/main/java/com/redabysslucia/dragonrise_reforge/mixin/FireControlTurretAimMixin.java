package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.vehicle.IndirectFireVehicleBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 当火控激活且接管开启时，取消 SBW 的玩家视角炮塔瞄准。
 * 防止 SBW 的 adjustTurretAngle 与火控的 turretAutoAimFromVector 冲突。
 */
@Mixin(value = VehicleEntity.class, remap = false)
public abstract class FireControlTurretAimMixin {

    @Inject(method = "adjustTurretAngle", at = @At("HEAD"), cancellable = true)
    private void dragonrise$skipLookAimWhenFireControlActive(CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        // 仅当火控激活且接管开启时取消炮塔自动瞄准
        if (self instanceof IndirectFireVehicleBase vehicle
                && vehicle.isFireControlActive()
                && vehicle.isFireControlTakeoverEnabled()) {
            ci.cancel();
        }
    }
}
