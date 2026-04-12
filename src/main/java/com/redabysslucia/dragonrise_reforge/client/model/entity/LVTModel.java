package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.LVTEntity;
import com.redabysslucia.dragonrise_reforge.entities.M2A2Entity;

public class LVTModel extends VehicleModel<LVTEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
