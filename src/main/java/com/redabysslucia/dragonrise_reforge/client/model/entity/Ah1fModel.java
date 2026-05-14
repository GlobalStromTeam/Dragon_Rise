package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.Ah1fEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Ah1fModel extends DragonriseVehicleModel<Ah1fEntity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<Ah1fEntity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> bone.setRotY(Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        if (boneName.equals("tailPropeller")) {
            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
        }

        return super.collectTransform(boneName);
    }
}
