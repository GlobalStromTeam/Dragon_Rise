package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.MausEntity;

public class MausModel extends DragonriseVehicleModel<MausEntity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}
