package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.BMD4MEntity;
import com.redabysslucia.dragonrise_reforge.entities.BMP3Entity;

public class BMD4MModel extends VehicleModel<BMD4MEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}