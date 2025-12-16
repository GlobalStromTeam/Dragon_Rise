package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;
import com.redabysslucia.dragonrise_reforge.entities.T90MHEntity;


public class T90MHModel extends VehicleModel<T90MHEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

}
