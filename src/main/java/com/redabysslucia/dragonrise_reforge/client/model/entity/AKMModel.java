package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import com.redabysslucia.dragonrise_reforge.entities.AKMEEntity;
import org.jetbrains.annotations.Nullable;

public class AKMModel extends DragonriseVehicleModel<AKMEEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

    @Override
    public @Nullable TransformContext<AKMEEntity> collectTransform(String boneName) {

        if (boneName.equals("missile1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("missile2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("missile3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
        }

        if (boneName.equals("missile4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
        }

        if (boneName.equals("missile5")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 5));
        }

        if (boneName.equals("missile6")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 6));
        }
        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("DriverAAMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}