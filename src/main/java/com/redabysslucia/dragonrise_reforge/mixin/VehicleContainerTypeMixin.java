package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.vehicle.subdata.VehicleContainerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把 Small 容器从 3×9=27 格改为 3×7=21 格，
 * 槽位布局对应改动见 {@link SmallVehicleContainerMenuMixin}。
 */
@Mixin(value = VehicleContainerType.class, remap = false)
public abstract class VehicleContainerTypeMixin {

    @Inject(method = "getSize", at = @At("RETURN"), cancellable = true)
    private void dragonrise$smallSize(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == VehicleContainerType.SMALL) {
            cir.setReturnValue(21);
        }
    }

    @Inject(method = "getCol", at = @At("RETURN"), cancellable = true)
    private void dragonrise$smallCol(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == VehicleContainerType.SMALL) {
            cir.setReturnValue(7);
        }
    }
}
