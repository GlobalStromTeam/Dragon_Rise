package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTQ15Entity;

public class ZTQ15Model extends DragonriseVehicleModel<ZTQ15Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
