package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.Project640Entity;

public class Project640Model extends VehicleModel<Project640Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }
}
