package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4BEntity;

public class VT4BModel extends VehicleModel<VT4BEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}
