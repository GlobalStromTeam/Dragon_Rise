package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.vehicle.subdata.SeatInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {

    @Shadow(remap = false)
    public abstract void passengerPitchOnTurret(Entity entity, float turretMinPitch, float turretMaxPitch) ;

    @Shadow(remap = false)
    public abstract void passengerYawOnTurret(Entity entity, float minYaw, float maxYaw, float passengerRot, boolean rotateWithTurret);

    @Inject(
            method = "clampRotation",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    protected void onClampRotation(Entity entity, CallbackInfo ci, int index, List<SeatInfo> seats, SeatInfo seat) {
        if (seat.transform.equals("WeaponStation") && !seat.canRotateBody) {
            passengerPitchOnTurret(entity, seat.minPitch, seat.maxPitch);
            passengerYawOnTurret(entity, seat.minYaw, seat.maxYaw, seat.orientation, false);
        }
    }
}
