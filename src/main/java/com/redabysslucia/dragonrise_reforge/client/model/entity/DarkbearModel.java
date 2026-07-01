package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.DarkbearEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class DarkbearModel extends DragonriseVehicleModel<DarkbearEntity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<DarkbearEntity> collectTransform(String boneName) {
        if (boneName.equals("subcannon")) {
            return(bone, vehicle, state) -> bone.setRotX(getAnimationProcessor().getBone("barrel").getRotX());
        }

        return super.collectTransform(boneName);
    }
}
