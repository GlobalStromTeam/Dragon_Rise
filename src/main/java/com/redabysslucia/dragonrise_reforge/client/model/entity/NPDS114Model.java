package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.NPDS114Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ59AEntity;

public class NPDS114Model extends DragonriseVehicleModel<NPDS114Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
