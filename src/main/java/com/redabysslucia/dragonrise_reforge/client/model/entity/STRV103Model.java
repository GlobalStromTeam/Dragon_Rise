package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.STRV103Entity;
import org.jetbrains.annotations.Nullable;

public class STRV103Model extends VehicleModel<STRV103Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

    @Override
    public @Nullable TransformContext<STRV103Entity> collectTransform(String boneName) {

        if (boneName.equals("missile3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("missile4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("Missile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}