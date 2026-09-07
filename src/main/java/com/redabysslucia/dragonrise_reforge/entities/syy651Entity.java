package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class syy651Entity extends DragonriseVehicleBase {

    public syy651Entity(EntityType<syy651Entity> type, Level world) {
        super(type, world);
    }

//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }


}
