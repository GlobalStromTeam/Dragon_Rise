package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.vehicle.DefaultVehicleData;
import com.atsuishio.superbwarfare.data.vehicle.subdata.VehicleContainerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把所有超过 21 格的载具容器类型（Medium/Large/Huge）统一路由为 Small，
 * 配合 {@link VehicleContainerTypeMixin} 与 {@link SmallVehicleContainerMenuMixin}
 * 使这些载具的物品栏统一为 3×7=21 格。
 */
@Mixin(value = DefaultVehicleData.class, remap = false)
public abstract class DefaultVehicleDataMixin {

    @Inject(method = "getVehicleContainerType", at = @At("RETURN"), cancellable = true)
    private void dragonrise$capContainerType(CallbackInfoReturnable<VehicleContainerType> cir) {
        VehicleContainerType type = cir.getReturnValue();
        if (type == VehicleContainerType.MEDIUM
                || type == VehicleContainerType.LARGE
                || type == VehicleContainerType.HUGE) {
            cir.setReturnValue(VehicleContainerType.SMALL);
        }
    }
}
