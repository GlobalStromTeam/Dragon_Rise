package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.LVTEntity;

public class LVTModel extends DragonriseVehicleModel<LVTEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
