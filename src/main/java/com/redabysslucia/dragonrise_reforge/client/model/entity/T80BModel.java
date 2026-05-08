package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.T80BEntity;


public class T80BModel extends DragonriseVehicleModel<T80BEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

}
