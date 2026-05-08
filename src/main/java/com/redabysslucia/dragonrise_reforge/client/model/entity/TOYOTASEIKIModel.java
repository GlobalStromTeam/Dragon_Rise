package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.TOYOTASEIKIEntity;
import com.redabysslucia.dragonrise_reforge.entities.motuoEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class TOYOTASEIKIModel extends DragonriseVehicleModel<TOYOTASEIKIEntity> {


        @Override
        public @Nullable TransformContext<TOYOTASEIKIEntity> collectTransform(String boneName) {
                if (boneName.equals("control")) {
                        return (control, vehicle, state) -> control.setRotY(2 * Mth.lerp(state.getPartialTick(), vehicle.getRudderRotO(), vehicle.getRudderRot()));
                }

                return super.collectTransform(boneName);
        }

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}