package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.type97Entity;
import com.redabysslucia.dragonrise_reforge.entities.type97QEntity;

public class type97QModel extends VehicleModel<type97QEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
