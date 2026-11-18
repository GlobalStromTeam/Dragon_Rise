package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.sound.VehicleSoundInstance;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.client.sound.SquadEngineProfiles;
import com.redabysslucia.dragonrise_reforge.entity.vehicle.engine.EngineControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {VehicleSoundInstance.TrackSound.class, VehicleSoundInstance.SwimSound.class}, remap = false)
public abstract class VehicleTrackSoundMixin {
    @Inject(method = "getVolume", at = @At("HEAD"), cancellable = true)
    private void dragonrise$useSquadMovement(VehicleEntity vehicle, CallbackInfoReturnable<Float> cir) {
        var profile = SquadEngineProfiles.get(EngineControl.id(vehicle));
        if (profile == null) return;
        String kind = (Object) this instanceof VehicleSoundInstance.SwimSound ? "water" : "land";
        for (var layer : profile.layers) {
            if (layer.movement.equals(kind)) {
                cir.setReturnValue(0f);
                return;
            }
        }
    }
}
