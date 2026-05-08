package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.AH64Entity;
import com.redabysslucia.dragonrise_reforge.entities.EC665Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class AH64Model extends DragonriseVehicleModel<AH64Entity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<AH64Entity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
        }

        if (boneName.equals("4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
        }

        if (boneName.equals("5")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 5));
        }

        if (boneName.equals("6")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 6));
        }

        if (boneName.equals("7")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 7));
        }

        if (boneName.equals("8")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 8));
        }



        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("PassengerMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

}