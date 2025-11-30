package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.Mi28Entity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.KA50Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class KA50Model extends VehicleModel<KA50Entity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<KA50Entity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.propellerRotO, vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.propellerRotO, vehicle.getPropellerRot()));
        }

        if (boneName.equals("missile1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("missile2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("SeekMissile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}
