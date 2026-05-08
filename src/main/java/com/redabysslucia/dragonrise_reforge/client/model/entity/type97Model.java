package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.M2A2Entity;
import com.redabysslucia.dragonrise_reforge.entities.type97Entity;

public class type97Model extends DragonriseVehicleModel<type97Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
