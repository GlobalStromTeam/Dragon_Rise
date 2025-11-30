package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.resources.ResourceLocation;

public class ZTZ99AModel extends VehicleModel<ZTZ99AEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}
