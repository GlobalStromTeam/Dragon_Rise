package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.M4A2105Entity;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;

public class M4A2105Model extends DragonriseVehicleModel<M4A2105Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
