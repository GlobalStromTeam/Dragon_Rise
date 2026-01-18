package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.CM34Entity;
import org.jetbrains.annotations.Nullable;

public class CM34Model extends VehicleModel<CM34Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

    @Override
    public @Nullable TransformContext<CM34Entity> collectTransform(String boneName) {

        return super.collectTransform(boneName);
    }
}