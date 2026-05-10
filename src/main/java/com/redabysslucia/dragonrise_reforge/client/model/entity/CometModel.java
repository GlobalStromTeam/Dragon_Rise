package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.CometEntity;

public class CometModel extends DragonriseVehicleModel<CometEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}
