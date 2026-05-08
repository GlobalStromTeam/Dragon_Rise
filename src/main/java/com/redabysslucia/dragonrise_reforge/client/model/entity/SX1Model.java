package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.SX1Entity;

public class SX1Model extends DragonriseVehicleModel<SX1Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}