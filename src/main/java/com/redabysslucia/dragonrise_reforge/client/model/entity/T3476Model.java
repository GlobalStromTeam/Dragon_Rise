package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.T3476Entity;

public class T3476Model extends VehicleModel<T3476Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}