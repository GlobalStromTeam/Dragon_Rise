package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.HumveetowEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class HumveetowModel extends DragonriseVehicleModel<HumveetowEntity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }

    @Override
    public @Nullable TransformContext<HumveetowEntity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        return super.collectTransform(boneName);
    }
}
