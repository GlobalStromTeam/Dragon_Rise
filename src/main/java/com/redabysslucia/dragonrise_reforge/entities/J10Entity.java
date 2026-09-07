package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class J10Entity extends DragonriseVehicleBase {

    public J10Entity(EntityType<J10Entity> type, Level world) {
        super(type, world);
    }

//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }


}
