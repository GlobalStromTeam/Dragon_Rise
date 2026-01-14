package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.TYPE100Entity;


public class TYPE100Model extends VehicleModel<TYPE100Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

}
