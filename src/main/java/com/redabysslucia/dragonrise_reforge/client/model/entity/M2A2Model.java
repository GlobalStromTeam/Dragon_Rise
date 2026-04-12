package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.M2A2Entity;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;

public class M2A2Model extends VehicleModel<M2A2Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
