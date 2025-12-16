package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.S2S38Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;

public class S2S38Model extends VehicleModel<S2S38Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}
