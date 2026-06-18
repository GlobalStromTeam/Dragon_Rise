package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.HumveeEntity;

public class HumveeModel extends DragonriseVehicleModel<HumveeEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}