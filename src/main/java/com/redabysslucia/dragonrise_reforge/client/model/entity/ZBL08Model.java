package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.ZBL08Entity;

public class ZBL08Model extends VehicleModel<ZBL08Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}