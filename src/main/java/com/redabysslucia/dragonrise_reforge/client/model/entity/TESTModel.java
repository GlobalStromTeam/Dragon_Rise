package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.TESTEntity;

public class TESTModel extends DragonriseVehicleModel<TESTEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}
