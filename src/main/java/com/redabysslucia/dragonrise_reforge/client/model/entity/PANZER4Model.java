package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.PANZER4Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;

public class PANZER4Model extends VehicleModel<PANZER4Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
