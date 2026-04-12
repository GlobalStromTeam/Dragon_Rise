package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.type3Entity;
import com.redabysslucia.dragonrise_reforge.entities.type97Entity;

public class type3Model extends VehicleModel<type3Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
