package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.IS2Entity;

public class IS2Model extends VehicleModel<IS2Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}