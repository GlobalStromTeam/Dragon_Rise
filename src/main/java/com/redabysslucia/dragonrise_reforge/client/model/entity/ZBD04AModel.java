package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;

public class ZBD04AModel extends VehicleModel<ZBD04AEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}