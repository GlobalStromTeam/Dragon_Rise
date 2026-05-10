package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.KV1Entity;

public class KV1Model extends DragonriseVehicleModel<KV1Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}
