package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;
import net.minecraft.resources.ResourceLocation;


public class T80Model extends VehicleModel<T80Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

}
