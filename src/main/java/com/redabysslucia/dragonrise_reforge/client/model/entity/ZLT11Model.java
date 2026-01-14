package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.ZLT11Entity;

public class ZLT11Model extends VehicleModel<ZLT11Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}