package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.BMPT72Entity;
import com.redabysslucia.dragonrise_reforge.entities.CV90Entity;
import org.jetbrains.annotations.Nullable;

public class BMPT72Model extends VehicleModel<BMPT72Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }


        @Override
        public @Nullable TransformContext<BMPT72Entity> collectTransform(String boneName) {

            if (boneName.equals("missile1")) {
                return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
            }

            if (boneName.equals("missile2")) {
                return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
            }

            if (boneName.equals("missile3")) {
                return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
            }

            if (boneName.equals("missile4")) {
                return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
            }

            if (boneName.equals("9m120")) {
                return (bone, vehicle, state) -> bone.setRotX(getAnimationProcessor().getBone("barrel").getRotX());
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