package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;

public class VT4A1Model extends VehicleModel<VT4A1Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
