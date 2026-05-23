package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.M3StuartEntity;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;

public class M3StuartModel extends DragonriseVehicleModel<M3StuartEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
