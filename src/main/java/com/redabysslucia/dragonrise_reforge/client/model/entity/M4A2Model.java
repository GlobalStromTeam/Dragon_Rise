package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;
import com.redabysslucia.dragonrise_reforge.entities.PANZER4Entity;

public class M4A2Model extends DragonriseVehicleModel<M4A2Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
