package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.HYR0Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTQ15Entity;

public class HYR0Model extends VehicleModel<HYR0Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
