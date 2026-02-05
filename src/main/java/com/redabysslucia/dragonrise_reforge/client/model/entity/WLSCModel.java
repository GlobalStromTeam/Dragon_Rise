package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.WLSCEntity;

public class WLSCModel extends VehicleModel<WLSCEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}