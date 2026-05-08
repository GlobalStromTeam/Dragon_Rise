package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.NPDS514Entity;
import com.redabysslucia.dragonrise_reforge.entities.NPDS810Entity;

public class NPDS810Model extends DragonriseVehicleModel<NPDS810Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
