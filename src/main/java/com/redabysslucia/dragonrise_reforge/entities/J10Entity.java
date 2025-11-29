package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class J10Entity extends GeoVehicleEntity {

        public J10Entity(EntityType<J10Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public boolean useAircraftCamera(int seatIndex) {
                return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
        }

        @Override
        public double getMouseSensitivity() {
                return 0.25;
        }
}
