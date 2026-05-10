package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.PershingEntity;

public class PershingModel extends DragonriseVehicleModel<PershingEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}
