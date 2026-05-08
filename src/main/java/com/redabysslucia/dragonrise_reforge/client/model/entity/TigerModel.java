package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.TigerEntity;

public class TigerModel extends DragonriseVehicleModel<TigerEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}