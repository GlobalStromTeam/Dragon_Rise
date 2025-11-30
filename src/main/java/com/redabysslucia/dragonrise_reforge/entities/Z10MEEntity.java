package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Z10MEEntity extends GeoVehicleEntity {

        public Z10MEEntity(EntityType<Z10MEEntity> type, Level world) {
                super(type, world);
        }

        @Override
        public double getMouseSensitivity() {
                return 0.25;
        }
}
