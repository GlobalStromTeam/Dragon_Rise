package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.PZBJYEntity;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;


public class PZBJYModel extends DragonriseVehicleModel<PZBJYEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

}
