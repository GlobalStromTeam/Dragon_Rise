package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class AC130Entity extends DragonriseVehicleBase {

    public AC130Entity(EntityType<AC130Entity> type, Level world) {
        super(type, world);
    }


    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.02 : 0.05;
    }
}
