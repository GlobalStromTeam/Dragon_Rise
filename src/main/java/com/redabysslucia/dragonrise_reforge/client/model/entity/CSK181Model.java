package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.CSK181Entity;
import com.redabysslucia.dragonrise_reforge.entities.TOYOTASEIKIEntity;

public class CSK181Model extends VehicleModel<CSK181Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}