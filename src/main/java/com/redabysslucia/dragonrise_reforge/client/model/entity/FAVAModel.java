package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.FAVAEntity;

public class FAVAModel extends DragonriseVehicleModel<FAVAEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}