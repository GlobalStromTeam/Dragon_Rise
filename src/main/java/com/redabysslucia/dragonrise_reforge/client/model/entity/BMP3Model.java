package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.BMP3Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;

public class BMP3Model extends VehicleModel<BMP3Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}