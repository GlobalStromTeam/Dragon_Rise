package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.Lav150Entity;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import com.atsuishio.superbwarfare.entity.vehicle.Mi28Entity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class AA625EModel extends VehicleModel<AA625EEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }
}