package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.CV90Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;

public class CV90Model extends VehicleModel<CV90Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}