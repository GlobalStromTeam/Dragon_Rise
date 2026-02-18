package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.TruckEntity;
import com.redabysslucia.dragonrise_reforge.entities.WLSCEntity;
import com.redabysslucia.dragonrise_reforge.entities.motuoEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class motuoModel extends VehicleModel<motuoEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

    @Override
    public @Nullable TransformContext<motuoEntity> collectTransform(String boneName) {
        if (boneName.equals("control")) {
            return (control, vehicle, state) -> control.setRotY(1 * Mth.lerp(state.getPartialTick(), vehicle.rudderRotO, vehicle.getRudderRot()));
        }

        return super.collectTransform(boneName);
    }
}

