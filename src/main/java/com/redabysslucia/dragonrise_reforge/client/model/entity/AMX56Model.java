package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.AMX56Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;

public class AMX56Model extends VehicleModel<AMX56Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
