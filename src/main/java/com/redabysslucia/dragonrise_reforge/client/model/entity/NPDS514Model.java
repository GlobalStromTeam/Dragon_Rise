package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.NPDS114Entity;
import com.redabysslucia.dragonrise_reforge.entities.NPDS514Entity;

public class NPDS514Model extends DragonriseVehicleModel<NPDS514Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
